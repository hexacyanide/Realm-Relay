package realmrelay.packets.server;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import realmrelay.packets.Packet;

public class AccountListPacket extends Packet {
    public int accountListId;
    public String[] accountIds = new String[0];
    public int lockAction;

    @Override
    public void parseFromInput(DataInput in) throws IOException {
        this.accountListId = in.readInt();
        this.accountIds = new String[in.readShort()];
        for (int i = 0; i < this.accountIds.length; i++) this.accountIds[i] = in.readUTF();
        this.lockAction = in.readInt();
    }

    @Override
    public void writeToOutput(DataOutput out) throws IOException {
        out.writeInt(this.accountListId);
        out.writeShort(this.accountIds.length);
        for (String accountId : this.accountIds) out.writeUTF(accountId);
        out.writeInt(this.lockAction);
    }
}