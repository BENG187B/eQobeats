package colburnsoftworks.quspmusic;

import org.msgpack.MessagePack;
import org.msgpack.annotation.Message;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by dalex on 1/2/2016.
 */
public class MsgPak {
    @Message
    public static class MyMessage {
        public String name;
        public double version;
        public int heartRate;
        public int targetHeartRate;
        public List<Integer> heartBeats;
        public int mode;
    }

    public static void main(String[] args) throws Exception {
        MyMessage src1 = new MyMessage();
        src1.name = "msgpack";
        src1.version = 0.6;
        MyMessage src2 = new MyMessage();
        src2.name = "muga";
        src2.version = 10.0;
        MyMessage src3 = new MyMessage();
        src3.name = "frsyukik";
        src3.version = 1.0;

        MessagePack msgpack = new MessagePack();
        //
        // Serialize
        //
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Packer packer = msgpack.createPacker(out);
        packer.write(src1);
        packer.write(src2);
        packer.write(src3);
        byte[] bytes = out.toByteArray();

        //
        // Deserialize
        //
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        Unpacker unpacker = msgpack.createUnpacker(in);
        MyMessage dst1 = unpacker.read(MyMessage.class);
        MyMessage dst2 = unpacker.read(MyMessage.class);
        MyMessage dst3 = unpacker.read(MyMessage.class);
    }

    // Used for testing
    /*public static String packMessage(String msgOne, String msgTwo) throws Exception{
        MyMessage src1 = new MyMessage();
        src1.name = msgOne;
        MyMessage src2 = new MyMessage();
        src2.name = msgTwo;

        MessagePack msgpack = new MessagePack();
        //
        // Serialize
        //
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Packer packer = msgpack.createPacker(out);
        packer.write(src1);
        packer.write(src2);
        byte[] bytes = out.toByteArray();
        return Arrays.toString(bytes);
    }*/

    public static byte[] packMessage(int heartRate, List<Integer> heartBeats,
                                        int targetHeartRate, int mode) throws Exception{
        MyMessage src = new MyMessage();
        src.heartRate = heartRate;
        src.heartBeats = heartBeats;
        src.targetHeartRate = targetHeartRate;
        src.mode = mode;

        MessagePack msgpack = new MessagePack();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Packer packer = msgpack.createPacker(out);
        packer.write(src);
        return out.toByteArray();
    }

    public static String unpackMessage(byte[] bytes) throws Exception{
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        MessagePack msgpack = new MessagePack();

        Unpacker unpacker = msgpack.createUnpacker(in);
        MyMessage message = unpacker.read(MyMessage.class);

        return String.valueOf(message.heartRate);
    }

    /*public static String unpackMessage(String msgOne, String msgTwo) throws Exception{
        MyMessage src1 = new MyMessage();
        src1.name = msgOne;
        MyMessage src2 = new MyMessage();
        src2.name = msgTwo;

        // Pack message
        //List<String> src = new ArrayList<String>();
        //src.add(msgOne);
        //src.add(msgTwo);

        MessagePack msgpack = new MessagePack();
        //
        // Serialize
        //
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Packer packer = msgpack.createPacker(out);
        packer.write(src1);
        packer.write(src2);
        byte[] bytes = out.toByteArray();
        //byte[] raw = msgpack.write(src);


        //
        // Deserialize
        //
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        Unpacker unpacker = msgpack.createUnpacker(in);
        MyMessage dst1 = unpacker.read(MyMessage.class);
        MyMessage dst2 = unpacker.read(MyMessage.class);


        return dst1.name+ " " + dst2.name;
    }*/
}
