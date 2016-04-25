package colburnsoftworks.quspmusic;

import android.content.Context;
import android.view.View;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Admin on 1/23/2016.
 */
public class MqttRead {
    private MqttAndroidClient client;
    private Context context = null;

    public MqttRead(Context context) {
        this.context = context;
    }

    public void connectMqtt(final String readTopic) {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            String clientId = MqttClient.generateClientId();

            client = new MqttAndroidClient(this.context,
                    "tcp://streaming.neuroscale.io:443", clientId);

            if (!client.isConnected()){
                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable throwable) {
                        System.out.println("Connection to the read node was lost");
                        System.out.println("Reconnecting");

                        MqttConnectOptions options = new MqttConnectOptions();
                        options.setCleanSession(true);
                        try {
                            client.connect(options, this, new IMqttActionListener() {
                                // If connection to the broker is successful
                                @Override
                                public void onSuccess(IMqttToken iMqttToken) {
                                    // Subscribe to all topics from this node
                                    // QoS level 2 ensures only 1 message transferred
                                    subscribe("/" + readTopic + "/#", 2);
                                }

                                @Override
                                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                                    System.out.println("ERROR: Failure to connect to the read node!");
                                }
                            });
                        } catch (MqttException e) {
                            e.printStackTrace();
                            System.out.println("ERROR: MqttException trying to reconnect to read!");
                        }

                    }

                    @Override
                    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                        //System.out.println("Message arrived from read node!\nMessage Contents: " +
                        //        new String(mqttMessage.getPayload()));
                        // Log the time it took for a response and then handle the message
                        //NeuroService.debug.logTime();
                        handleMessage(new String(mqttMessage.getPayload()));
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                        System.out.println("ERROR: Delivery to the read node!");
                    }
                });

                client.connect(options, this, new IMqttActionListener() {
                    // If connection to the broker is successful
                    @Override
                    public void onSuccess(IMqttToken iMqttToken) {
                        // Subscribe to all topics from this node
                        // QoS level 2 ensures only 1 message transferred
                        System.out.println("Subscribing here: /" + readTopic + "/#");
                        subscribe("/" + readTopic + "/#", 2);
                    }

                    @Override
                    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                        System.out.println("ERROR: Failure to connect to the read node!");
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(String s) {
        try {
            // Retrieve the heart rate
            int heartRate = (new JSONObject(s)).getJSONArray("streams").getJSONObject(0).
                    getJSONArray("samples").getJSONArray(0).getInt(0);

            // Convert heart rate to playback speed
            float playbackSpeed = calculateSpeed(heartRate);

            // Record data point
            NeuroService.debug.setPlaybackSpeed(playbackSpeed);
            NeuroService.debug.recordData();

            // Pass playback speed to phase vocoder
            MainActivity.musicSrv.changeSpeed(playbackSpeed);
        } catch (Exception e) {
        }
    }

    public float calculateSpeed(int heartRate) {
        return heartRate*0.5f/60f;
    }

    public void subscribe(String topic, Integer qosLevel) {
        try {
            client.subscribe(topic, qosLevel, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    System.out.println("Successfully subscribing to: " + iMqttToken);
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    System.out.println("Failure subscribing to: " + iMqttToken);
                    throwable.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void appendLog(String text)
    {
        File logFile = new File("sdcard/qusplog.file");
        if (!logFile.exists())
        {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append("Time: " + System.currentTimeMillis() + "; RR Events: " + text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
