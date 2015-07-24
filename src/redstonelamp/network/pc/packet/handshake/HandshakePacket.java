package redstonelamp.network.pc.packet.handshake;

import redstonelamp.network.packet.DataPacket;
import redstonelamp.network.pc.PCNetworkInfo;
import redstonelamp.utils.DynamicByteBuffer;

/**
 * HandshakePacket (0x00)
 */
public class HandshakePacket extends DataPacket{
    public final static byte ID = PCNetworkInfo.HANDHSAKE_HANDSHAKE;

    public final static int STATE_STATUS = 1;
    public final static int STATE_LOGIN = 2;

    public int protocolVersion;
    public String serverAddress;
    public int serverPort;
    public int nextState;

    @Override
    public byte getPID() {
        return PCNetworkInfo.HANDHSAKE_HANDSHAKE;
    }

    @Override
    protected void _encode(DynamicByteBuffer bb) {

    }

    @Override
    protected void _decode(DynamicByteBuffer bb) {
        protocolVersion = bb.getVarInt();
        serverAddress = bb.getPCString();
        serverPort = bb.getUnsignedShort();
        nextState = bb.getVarInt();
    }
}
