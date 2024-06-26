package tel.schich.libdatachannel;

import generated.rtcDataChannelInit;
import generated.rtcReliability;

public class DataChannelInitSettings {

    final rtcDataChannelInit innerInit;

    public DataChannelInitSettings() {
        innerInit = new rtcDataChannelInit();
        innerInit.reliability = new rtcReliability();
    }

    public DataChannelInitSettings(final rtcReliability reliability) {
        this(); // TODO init
    }

    /**
     * if true, the Data Channel will not enforce message ordering, else it will be ordered
     */
    public DataChannelInitSettings unordered(boolean unordered) {
        this.innerInit.reliability.unordered = (byte) (unordered ? 1 : 0);
        return this;
    }

    /**
     * if true, the Data Channel will not enforce strict reliability, else it will be reliable
     */
    public DataChannelInitSettings unreliable(boolean unreliable) {
        this.innerInit.reliability.unreliable = (byte) (unreliable ? 1 : 0);
        return this;
    }

    /**
     * if unreliable, time window in milliseconds during which transmissions and retransmissions may occur
     */
    public DataChannelInitSettings maxPacketLifeTime(int maxPacketLifeTime) {
        this.innerInit.reliability.maxPacketLifeTime = maxPacketLifeTime;
        return this;
    }

    /**
     * if unreliable and maxPacketLifeTime is 0, maximum number of attempted retransmissions (0 means no retransmission)
     */
    public DataChannelInitSettings maxRetransmits(int maxRetransmits) {
        this.innerInit.reliability.maxRetransmits = maxRetransmits;
        return this;
    }

    /**
     * (optional): a user-defined UTF-8 string representing the Data Channel protocol, empty if NULL
     */
    public DataChannelInitSettings protocol(String protocol) {
        this.innerInit.protocol = JNAUtil.toPointer(protocol);
        return this;
    }

    /**
     * if true, the Data Channel is assumed to be negotiated by the user and won't be negotiated by the WebRTC layer
     */
    public DataChannelInitSettings negotiated(boolean negotiated) {
        this.innerInit.negotiated = (byte) (negotiated ? 1 : 0);
        return this;
    }

    /**
     * if true, the Data Channel will use stream as stream ID, else an available id is automatically selected
     */
    public DataChannelInitSettings manualStream(boolean manualStream) {
        this.innerInit.manualStream = (byte) (manualStream ? 1 : 0);
        return this;
    }

    /**
     * if manualStream is true, the Data Channel will use it as stream ID, else it is ignored
     */
    public DataChannelInitSettings stream(short stream) {
        this.innerInit.stream = stream;
        return this;
    }
}
