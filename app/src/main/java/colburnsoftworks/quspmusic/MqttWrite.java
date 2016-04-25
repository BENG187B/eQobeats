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

/**
 * Created by Admin on 1/23/2016.
 */

public class MqttWrite {
    private MqttAndroidClient client;
    private Context context = null;

    public MqttWrite(Context context) {
        this.context = context;
    }

    public void connectMqtt(final String writeTopic) {
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
                        System.out.println("Connection to the write node was lost");
                        System.out.println("Reconnecting");

                        MqttConnectOptions options = new MqttConnectOptions();
                        options.setCleanSession(true);
                        try {
                            client.connect(options, this, new IMqttActionListener() {
                                // If connection to the broker is successful
                                @Override
                                public void onSuccess(IMqttToken iMqttToken) {
                                    // Publish a message to the node
                                    //publish("/" + writeTopic, "Reconnected message", 2, false);
                                    System.out.println("Reconnected to the write node");
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
                        System.out.println("ERROR: Message from the write node received");
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                        System.out.println("Delivery to the write node successful!");
                    }
                });

                client.connect(options, this, new IMqttActionListener() {
                    // If connection to the broker is successful
                    @Override
                    public void onSuccess(IMqttToken iMqttToken) {
                        // Publish a message to the node
                        System.out.println("Publishing here; /" + writeTopic);
                        publish("/" + writeTopic, "This is a test message", 2, false);
                    }

                    @Override
                    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                        System.out.println("ERROR: Failure to connect to the write node!");
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void publish(String topic, String payload, Integer qosLevel, Boolean retained) {
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(payload.getBytes());
            message.setQos(qosLevel);
            message.setRetained(retained);

            //NeuroService.debug.setTimeIn(System.currentTimeMillis());

            client.publish(topic, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
