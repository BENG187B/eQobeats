package colburnsoftworks.quspmusic;

import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import zephyr.android.HxMBT.*;

/**
 * Created by Admin on 1/8/2016.
 */
public class NewConnectedListener extends ConnectListenerImpl{

    private Handler _OldHandler;
    private Handler _aNewHandler;
    private int GP_MSG_ID = 0x20;
    private int GP_HANDLER_ID = 0x20;
    private int HR_SPD_DIST_PACKET =0x26;

    private final int HEART_RATE = 0x100;
    private final int INSTANT_SPEED = 0x101;
    private final int BATTERY_LEVEL = 0x102;
    private final int HEART_BEATS = 0x103;
    private HRSpeedDistPacketInfo HRSpeedDistPacket = new HRSpeedDistPacketInfo();

    public NewConnectedListener(Handler handler,Handler _NewHandler) {
        super(handler, null);
        _OldHandler = handler;
        _aNewHandler = _NewHandler;
    }

    public void Connected(ConnectedEvent<BTClient> eventArgs) {
        System.out.println(String.format("Connected to BioHarness %s.", eventArgs.getSource().getDevice().getName()));

        ZephyrProtocol _protocol = new ZephyrProtocol(eventArgs.getSource().getComms());
        _protocol.addZephyrPacketEventListener(new ZephyrPacketListener() {
            @Override
            public void ReceivedPacket(ZephyrPacketEvent eventArgs) {
                ZephyrPacketArgs msg = eventArgs.getPacket();
                byte CRCFailStatus;
                byte RcvdBytes;

                CRCFailStatus = msg.getCRCStatus();
                RcvdBytes = msg.getNumRvcdBytes();

                // Check if the message is a standard HxM message
                if (HR_SPD_DIST_PACKET==msg.getMsgID()) {
                    byte [] DataArray = msg.getBytes();

                    int heartRate = HRSpeedDistPacket.GetHeartRate(DataArray);
                    int[] heartBeats = HRSpeedDistPacket.GetHeartBeatTS(DataArray);
                    int beatNumber = HRSpeedDistPacket.GetHeartBeatNum(DataArray);
                    int batteryLevel = HRSpeedDistPacket.GetBatteryChargeInd(DataArray);

                    Message text1 = _aNewHandler.obtainMessage(HEART_RATE);
                    Bundle b1 = new Bundle();
                    b1.putInt("HeartRate", heartRate);          // Heart Rate
                    b1.putInt("HeartBeatNumber", beatNumber);   // Number of new heart beats
                    b1.putIntArray("HeartBeats", heartBeats);   // Array of heart beat times
                    b1.putInt("BatteryLevel", batteryLevel);    // Battery of device
                    text1.setData(b1);
                    _aNewHandler.sendMessage(text1);
                }
            }
        });
    }
}
