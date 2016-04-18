package app.gotway.euc.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

public class Data0x00 {
    public long time;
    public int distance;
    public float speed;
    public short voltageInt;
    public short currentInt;
    public float temperature;

    public int energe;
    public float totalDistance;

    public void readExternal(DataInput input) throws IOException {
        time = input.readLong();
        distance = input.readInt();
        speed = input.readFloat();
        voltageInt = input.readShort();
        currentInt = input.readShort();
        temperature = input.readFloat();
    }

    public void writeExternal(DataOutput output) throws IOException {
        output.writeLong(time);
        output.writeInt(distance);
        output.writeFloat(speed);
        output.writeShort(voltageInt);
        output.writeShort(currentInt);
        output.writeFloat(temperature);
    }
}
