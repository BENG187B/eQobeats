package colburnsoftworks.quspmusic;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.msgpack.util.json.JSON;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import zephyr.android.HxMBT.BTClient;
import zephyr.android.HxMBT.ZephyrProtocol;

public class HxMService extends Service {

    private final IBinder hxmBind = new HxMBinder();

    BluetoothAdapter adapter = null;
    BTClient _bt;
    NewConnectedListener _NConnListener;
    private final int HEART_RATE = 0x100;

    private int heartRate;
    private int batteryLevel;
    private int _beatNumber=0;
    private List<Integer> heartBeats = new ArrayList<>();
    private int hxmCounter=0;

    public HxMService() {
    }

    public void onCreate(){
        // create the service
        super.onCreate();

        // Setup hxm
        hxmSetup();

        // connect to hxm
        hxmConnect();
    }

    public void hxmSetup(){
        System.out.println("hxmSetup started");
        IntentFilter filter = new IntentFilter("android.bluetooth.device.action.PAIRING_REQUEST");
        this.getApplicationContext().registerReceiver(new BTBroadcastReceiver(), filter);
        IntentFilter filter2 = new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED");
        this.getApplicationContext().registerReceiver(new BTBondReceiver(), filter2);
    }

    public void hxmConnect(){
        System.out.println("hxmConnect started");
        String BhMacID = "00:07:80:9D:8A:E8";
        adapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().startsWith("HXM")) {
                    BhMacID = device.getAddress();
                    break;
                }
            }
        }

        BluetoothDevice Device = adapter.getRemoteDevice(BhMacID);
        String DeviceName = Device.getName();
        _bt = new BTClient(adapter, BhMacID);
        _NConnListener = new NewConnectedListener(Newhandler, Newhandler);
        _bt.addConnectedEventListener(_NConnListener);

        if (_bt.IsConnected()) {
            _bt.start();
            System.out.println("Connected to HxM " + DeviceName);
        } else {
            System.out.println("Unable to connect!");
        }
    }

    public void hxmDisconnect(){
        System.out.println("hxmDisconnect started");
        _bt.removeConnectedEventListener(_NConnListener);
        _bt.Close();
    }

    public int getBatteryLevel(){
        return batteryLevel;
    }

    public int getHeartRate(){
        return heartRate;
    }

    public List<Integer> getHeartBeats(){
        return heartBeats;
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
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return hxmBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        hxmDisconnect();
        return false;
    }

    public class HxMBinder extends Binder {
        HxMService getService(){
            return HxMService.this;
        }
    }

    private class BTBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();
            BluetoothDevice device = adapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
            Log.d("Bond state", "BOND_STATED = " + device.getBondState());
        }
    }

    private class BTBondReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BTIntent", intent.getAction());
            Bundle b = intent.getExtras();
            Log.d("BTIntent", b.get("android.bluetooth.device.extra.DEVICE").toString());
            Log.d("BTIntent", b.get("android.bluetooth.device.extra.PAIRING_VARIANT").toString());
            try {
                BluetoothDevice device = adapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
                Method m = BluetoothDevice.class.getMethod("convertPinToBytes", new Class[] {String.class} );
                byte[] pin = (byte[])m.invoke(device, "1234");
                m = device.getClass().getMethod("setPin", new Class [] {pin.getClass()});
                Object result = m.invoke(device, pin);
                Log.d("BTTest", result.toString());
            } catch (SecurityException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (NoSuchMethodException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    final Handler Newhandler = new Handler() {
        public void handleMessage(Message msg) {
            // Increment counter to control message size
            hxmCounter++;

            // Prepare message
            byte[] packedData = new byte[0];
            String testText = "";

            switch (msg.what){
                case HEART_RATE:
                    heartRate = msg.getData().getInt("HeartRate");
                    if (heartRate<0) {
                        heartRate += 256;
                    }
                    //System.out.println("Got heart rate, it's " + String.valueOf(heartRate));

                    batteryLevel = msg.getData().getInt("BatteryLevel");
                    //System.out.println("Got battery, it's " + String.valueOf(batteryLevel));

                    // Update heart beat array with only the new timestamps
                    int beatNumber = msg.getData().getInt("HeartBeatNumber");
                    int[] newBeats = msg.getData().getIntArray("HeartBeats");
                    appendHeartBeats(beatNumber, newBeats);
                    //System.out.println(Arrays.toString(newBeats));

                    // after hxmCounter seconds, pack and send message
                    if (hxmCounter==1){
                        //System.out.println("15 packets received");
                        //appendLog(Arrays.toString(heartBeats));
                        //System.out.println("The RR times to send are: " + heartBeats.toString());

                        // The Neuroservice has been connected, begin sending the data
                        try {
                            if(NeuroService.neuroReady){
                                //packedData = MsgPak.packMessage(heartRate, heartBeats, 0, 0);
                                // publish the message to neuroscale
                                //System.out.println("Endpoint write: " + "/" + MainActivity.neuroSrv.endpointWrite);
                                String payload = buildPayload();
                                System.out.println("Payload:");
                                System.out.println(payload);

                                // Update to the new heart rate and then record a new data point
                                NeuroService.debug.setHeartRate(heartRate);
                                NeuroService.debug.recordData();

                                MainActivity.neuroSrv.publish("/" +
                                                MainActivity.neuroSrv.endpointWrite,
                                        payload, 2, false);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        heartBeats.clear();
                        hxmCounter=0;
                    }
                    break;
            }
        }
    };

    private void appendHeartBeats(int beatNumber, int[] newBeats){
        // determine the number of heartbeats that occurred
        int beatCount = beatNumber - _beatNumber;
        if (beatCount < 0) {
            beatCount += 256;
        }

        // if first packet, add entire list
        /*if(heartBeats.isEmpty()) {
            // Adds newBeats[0] ..... to newBeats[length - 1]
            for (int i = 0; i < newBeats.length; i++) {
                heartBeats.add(newBeats[i]);
            }
        } else {
            // add only new times to the front of the heartBeats array
            for (int i=0; i<beatCount; i++){
                heartBeats.add(i, newBeats[i]);
            }
        }*/

        // if first packet, add the entire list in reverse order
        if(heartBeats.isEmpty()) {
            // Adds newBeats[length - 1] ....... to newBeats[0]
            for (int i = newBeats.length - 1; i >= 0; i--) {
                heartBeats.add(newBeats[i]);
            }
        } else {
            // add only new times to the end of the heartBeats array in reverse order
            for (int i = beatCount - 1; i >= 0; i--) {
                heartBeats.add(newBeats[i]);
            }
        }

        _beatNumber = beatNumber;
    }

    private String buildPayload() {
        try {
            JSONObject output = new JSONObject();
            JSONArray streams = new JSONArray();
            JSONObject ecgChunk = new JSONObject();
            JSONArray samples = new JSONArray();
            JSONArray data = new JSONArray();
            //JSONArray timestamps = new JSONArray();

            JSONArray marker_data = new JSONArray();
            JSONArray markers = new JSONArray();
            JSONArray markerStamps = new JSONArray();
            JSONObject markerChunk = new JSONObject();

            /*
            //-----------------------
            JSONArray heartBeatsArray = new JSONArray(heartBeats);
            data.put(heartBeatsArray);
            //------------------

            //data.put(heartBeats);
            data.put(heartRate);
            samples.put(data);

            timestamps.put(System.currentTimeMillis());*/

            // Pipeline READY!

            // Add each heartBeat as a new timestamp
            JSONArray timestamps = new JSONArray(heartBeats);
            // For each timestamp, there must be corresponding junk data aka the zephyr heartrate
            for(int i = 0; i < timestamps.length(); i++) {
                data = new JSONArray();
                data.put(heartRate);
                samples.put(data);
            }


            ecgChunk.put("name", "quspMusic");
            ecgChunk.put("samples", samples);
            ecgChunk.put("timestamps", timestamps);
            streams.put(ecgChunk);
            output.put("streams", streams);

            return output.toString();
        } catch (Exception e) {
        }
        return null;
    }
}
