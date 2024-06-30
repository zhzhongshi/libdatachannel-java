package tel.schich.libdatachannel;

import generated.rtcReliability;

/**
 * The {@link DataChannel} reliability settings.
 */
public class DataChannelReliability {

    rtcReliability innerReliability;

    DataChannelReliability(final rtcReliability reliability) {
        this.innerReliability = reliability;
    }

    public DataChannelReliability() {
        this.innerReliability = new rtcReliability();
    }

    /**
     * if true, the Data Channel will not enforce message ordering, else it will be ordered
     */
    public boolean unordered() {
        return this.innerReliability.unordered == 1;
    }

    /**
     * if true, the Data Channel will not enforce strict reliability, else it will be reliable
     */
    public boolean unreliable() {
        return this.innerReliability.unreliable == 1;
    }

    /**
     * if unreliable, time window in milliseconds during which transmissions and retransmissions may occur
     */
    public int maxPacketLifeTime() {
        return this.innerReliability.maxPacketLifeTime;
    }

    /**
     * if unreliable and maxPacketLifeTime is 0, maximum number of attempted retransmissions (0 means no retransmission)
     */
    public int maxRetransmits() {
        return this.innerReliability.maxRetransmits;
    }


}
