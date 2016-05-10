package colburnsoftworks.quspmusic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created by Admin on 1/31/2016.
 */
public class Debug {
    Boolean debug = false;
    long timeIn=0;
    int heartRate=0;
    float playbackSpeed=1;
    private List<Integer> heartBeats;
    private Double HRV;

    public void printLine(String message) {
        if (debug) {
            System.out.println(message);
        }
    }

    public void setTimeIn(long time) {
        timeIn = time;
    }

    public void logTime() {
        long time = System.currentTimeMillis() - timeIn;
        int rate = heartRate;
        appendLog(time, rate);

    }

    public void recordData() {
        File logFile = new File("sdcard/QuspData.file");
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
            // BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            // Timestamp    Heart Rate   Current Playback Speed
            //String text = Long.toString(System.currentTimeMillis()) + "\t" + heartRate + "\t"
            //        + playbackSpeed;
            //String text = Long.toString(System.currentTimeMillis()) + "\t" + heartBeats;
            String text = Long.toString(System.currentTimeMillis()) + "\t" + HRV;
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void appendLog(long time, int rate)
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
            String text = Long.toString(time) + "\t" + rate;
            //buf.append(Long.toString(time));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public void setPlaybackSpeed(float playbackSpeed) {
        this.playbackSpeed = playbackSpeed;
    }

    public void setHeartBeats(List<Integer> heartBeats) {
        this.heartBeats = heartBeats;
    }

    public void setHRV(Double HRV) {
        this.HRV = HRV;
    }
}
