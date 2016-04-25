package colburnsoftworks.quspmusic;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import colburnsoftworks.quspmusic.Debug;

public class NeuroService extends Service {

    private final IBinder neuroBind = new NeuroBinder();

    public static final String REQUEST_TAG = "Neuroscale";
    String instanceID = "";
    String endpointWrite = "";
    String endpointRead = "";
    int loopCount = 0;
    private RequestQueue mQueue;
    static Boolean neuroReady = false;
    final MqttRead reader = new MqttRead(this);
    final MqttWrite writer = new MqttWrite(this);
    final static Debug debug = new Debug();

    public NeuroService() {
    }

    public void onCreate() {
        // create the service
        super.onCreate();

        // setup neuroscale
        neuroSetup();

        // connect to neuroscale
        neuroConnect();
    }

    public void neuroSetup() {
        System.out.println("Neuroscale setup started");
        mQueue = CustomVolleyRequestQueue.getInstance(this.getApplicationContext())
                .getRequestQueue();

        checkRunningInstances();
    }

    private void checkRunningInstances() {
        // Check if there pipelines are currently running to save resources
        String url = "https://api.neuroscale.io/v1/instances" ;
        final CustomJSONObjectRequest jsonRequest = new CustomJSONObjectRequest(Request.Method
                .GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                int previousInstance = -1;
                try {
                    previousInstance = response.getInt("count");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (previousInstance == -1) {
                    System.out.println("Error checking for previous instances");
                } else if (previousInstance == 0) {
                    // No instances currently running, create new ones
                    getPipelines();
                } else {
                    // Pipeline does exist
                    useCurrentPipeline(response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.toString());
            }
        });
        jsonRequest.setTag(REQUEST_TAG );

        mQueue.add(jsonRequest);
    }

    private void useCurrentPipeline(JSONObject response) {
        // A current pipeline exists so use it
        System.out.println("A current pipeline exists");
        try {
            JSONArray data = response.getJSONArray("data");
            JSONObject instance = data.getJSONObject(0);
            instanceID = getInstanceID(instance);
            endpointWrite = getEndpoint("write", instance) + "/in";
            endpointRead = getEndpoint("read", instance) + "/out";
            prepareMQTT(instanceID, endpointWrite, endpointRead);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getPipelines() {
        // A current pipeline does not exist so it must be built
        String url = "https://api.neuroscale.io/v1/pipelines";

        final CustomJSONObjectRequest jsonRequest = new CustomJSONObjectRequest(Request.Method
                .GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // Get the ID for the pipeline of interest
                String pipelineID = getPipelineID(response, "Echo");

                // Build the post message body as a JSON object
                JSONObject jsonRequestBody = null;
                try {
                    jsonRequestBody = buildJsonBody(pipelineID);
                    debug.printLine("Metadata: " + jsonRequestBody.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Post to pipeline to initialize
                postPipelines(jsonRequestBody);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.getMessage());
            }
        });
        jsonRequest.setTag(REQUEST_TAG);

        mQueue.add(jsonRequest);
    }

    private void postPipelines(JSONObject requestBody) {
        // Post to /instances to create the new pipeline
        String url = "https://api.neuroscale.io/v1/instances";

        final CustomJSONObjectRequest jsonRequest = new CustomJSONObjectRequest(Request.Method
                .POST, url, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                instanceID = getInstanceID(response);
                endpointWrite = getEndpoint("write", response) + "/in";
                endpointRead = getEndpoint("read", response) + "/out";
                pipelineReady();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.toString());
            }
        });
        jsonRequest.setTag(REQUEST_TAG);
        mQueue.add(jsonRequest);
    }

    private void pipelineReady() {
        // Wait until the pipeline is running before proceeding
        loopCount++;
        String text = "pipelineReady called; count: " + String.valueOf(loopCount);
        System.out.println(text);

        String url = "https://api.neuroscale.io/v1/instances/" + instanceID;

        final CustomJSONObjectRequest jsonRequest = new CustomJSONObjectRequest(Request.Method
                .GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // If state is running, proceed else recheck
                String state = "configuring";
                try {
                    state = response.getString("state");
                } catch (JSONException e) {
                }

                if (state.equals("running")) {
                    prepareMQTT(instanceID, endpointWrite, endpointRead);
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    pipelineReady();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.toString());
            }
        });
        jsonRequest.setTag(REQUEST_TAG);
        mQueue.add(jsonRequest);
    }

    private void prepareMQTT(String instanceID, final String endpointWrite, String endpointRead) {
        debug.printLine("The pipeline is ready\nHas instanceID: " + instanceID + "\nHas write: " + endpointWrite + "\nHas read: " + endpointRead);

        // Connect and Subscribe to the MQTT out broker
        reader.connectMqtt(endpointRead);

        // Connect and Publish to the MQTT in broker
        writer.connectMqtt(endpointWrite);

        // Neuroscale is ready
        neuroReady = true;
    }

    public void subscribe(String topic, Integer qosLevel) {
        reader.subscribe(topic, qosLevel);
    }

    public void publish(String topic, String payload, Integer qosLevel, Boolean retained) {
        writer.publish(topic, payload, qosLevel, retained);
    }

    public void neuroConnect() {
        System.out.println("Neuroscale connect started");
    }

    public void neuroDisconnect() {
        System.out.println("Neuroscale disconnect started");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return neuroBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        neuroDisconnect();
        return false;
    }

    public class NeuroBinder extends Binder {
        NeuroService getService() {
            return NeuroService.this;
        }
    }

    // END OF PRIMARY CODE /////////////////////////////////
    // These functions should be placed in a helper class
    public String getPipelineID(JSONObject response, String pipelineName) {
        // Gets the unique ID for the pipeline of interest
        try {
            JSONArray pipelines = response.getJSONArray("data");
            for (int i=0; i<pipelines.length(); i++) {
                JSONObject pipeline = pipelines.getJSONObject(i);
                String name = pipeline.getString("name");
                String id = pipeline.getString("id");
                if (name.equals(pipelineName)) {
                    return id;
                }
            }
        } catch (JSONException e) {
            return "Error occurred parsing JSON in getPipelineID";
        }
        return "No pipeline with that name";
    }

    private JSONObject buildJsonBody(String pipelineID) throws JSONException {
        // Setup variables
        String streamName = "quspMusic";
        String modality = "ECG";
        int samplingRate = 0;

        JSONArray channelValues = getJsonChannelValues();

        // Build input stream
        JSONObject ecgStream = populateJsonStream(streamName, modality,
                samplingRate, channelValues);

        // Build marker stream
        JSONObject markerStream = buildJsonMarkerStream();

        // Build stream message
        JSONArray streams = new JSONArray();
        streams.put(ecgStream);

        // No marker streams next time
        //streams.put(markerStream);

        // Build node descriptors
        JSONObject nodeIn = buildJsonNodeDescriptor(streams);
        JSONObject nodeOut = nodeIn;

        // Build metadata
        JSONObject metadata = buildJsonMetadata(nodeIn, nodeOut);

        // msgpack or json?
        String encoding = "json";

        // time for pipleine to live in seconds without input
        int timeToLive = 3600;

        // Return request body
        return buildJsonParams(pipelineID, metadata, encoding, timeToLive);
    }

    public JSONArray getJsonChannelValues() throws JSONException {
        JSONArray jsonChannelValues = new JSONArray();

        // i < 3 contains heartRate; makes it 2 channels
        for (int i=1; i<2; i++) {
            JSONObject value = new JSONObject();
            value.put("label", "ch" + String.valueOf(i));
            jsonChannelValues.put(value);
        }
        return jsonChannelValues;
    }

    private JSONObject populateJsonStream(String streamName, String modality, int samplingRate, JSONArray channelValues) throws JSONException {
        JSONObject output = new JSONObject();
        output.put("name", streamName);
        output.put("type", modality);
        output.put("sampling_rate", samplingRate);
        output.put("channels", channelValues);
        return output;
    }

    private JSONObject buildJsonMarkerStream() throws JSONException {
        JSONObject output = new JSONObject();
        output.put("name", "mymarkers");
        output.put("type", "Markers");
        output.put("sampling_rate", 0);

        JSONObject value = new JSONObject();
        value.put("label", "value");
        JSONArray array = new JSONArray();
        array.put(value);
        output.put("channels", array);

        return output;
    }

    private JSONObject buildJsonNodeDescriptor(JSONArray streams) throws JSONException {
        JSONObject output = new JSONObject();
        output.put("name", "default");
        output.put("streams", streams);
        return output;
    }

    private JSONObject buildJsonMetadata(JSONObject nodeIn, JSONObject nodeOut) throws JSONException {
        JSONObject output = new JSONObject();
        JSONObject value = new JSONObject();
        JSONArray node1 = new JSONArray();
        JSONArray node2 = new JSONArray();

        node1.put(nodeIn);
        node2.put(nodeOut);
        value.put("in", node1);
        value.put("out", node2);
        output.put("nodes", value);
        return output;
    }

    private JSONObject buildJsonParams(String echoID, JSONObject metadata, String encoding, int timeToLive) {
        JSONObject output = new JSONObject();
        try {
            output.put("pipeline", echoID);
            output.put("metadata", metadata);
            output.put("encoding", encoding);
            output.put("time_to_live", timeToLive);
            return output;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getInstanceID(JSONObject response) {
        try {
            return response.getString("id");
        } catch (JSONException e) {
            return "Error occurred";
        }
    }

    private String getEndpoint(String mode, JSONObject response) {
        try {
            JSONObject endpoints = response.getJSONObject("endpoints");
            JSONArray data = endpoints.getJSONArray("data");
            for (int i=0; i<data.length(); i++) {
                JSONObject endpoint = data.getJSONObject(i);
                String name = endpoint.getString("mode");
                String url = endpoint.getString("url");
                if (name.equals(mode)) {
                    // Split url into tokens separated by /
                    String delims = "[/]+";
                    String[] tokens = url.split(delims);
                    // Return the topic name
                    return tokens[2];
                }
            }
        } catch (JSONException e) {
            return "Error occurred";
        }
        return null;
    }
}
