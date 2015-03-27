package realmrelay.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class StatData implements IData {
    public int obf0;
    public int obf1;
    public String obf2;

    public boolean isUTFData() {
        int i = this.obf0;
        return i == 31 || i == 62 || i == 82 || i == 38 || i == 54;
    }

    @Override
    public void parseFromInput(DataInput in) throws IOException {
        this.obf0 = in.readUnsignedByte();
        if (this.isUTFData()) {
            this.obf2 = in.readUTF();
        } else {
            this.obf1 = in.readInt();
        }
    }

    @Override
    public void writeToOutput(DataOutput out) throws IOException {
        out.writeByte(this.obf0);
        if (this.isUTFData()) {
            out.writeUTF(this.obf2);
        } else {
            out.writeInt(this.obf1);
        }
    }
}
