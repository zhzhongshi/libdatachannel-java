package tel.schich.libdatachannel;

import tel.schich.jniaccess.JNIAccess;

import java.time.Duration;

/**
 * The {@link DataChannel} reliability settings.
 */
public class DataChannelReliability {
    public static final DataChannelReliability DEFAULT = new DataChannelReliability(false, false, 0, 0);

    private final boolean unordered;
    private final boolean unreliable;
    private final long maxPacketLifeTime;
    private final int maxRetransmits;

    @JNIAccess
    public DataChannelReliability(boolean unordered, boolean unreliable, long maxPacketLifeTime, int maxRetransmits) {
        this.unordered = unordered;
        this.unreliable = unreliable;
        this.maxPacketLifeTime = maxPacketLifeTime;
        this.maxRetransmits = maxRetransmits;
    }

    /**
     * if true, the Data Channel will not enforce message ordering, else it will be ordered
     */
    public boolean isUnordered() {
        return unordered;
    }

    public DataChannelReliability withUnordered(boolean unordered) {
        return new DataChannelReliability(unordered, unordered, maxPacketLifeTime, maxRetransmits);
    }

    /**
     * if true, the Data Channel will not enforce strict reliability, else it will be reliable
     */
    public boolean isUnreliable() {
        return unreliable;
    }

    public DataChannelReliability withUnreliable(boolean unreliable) {
        return new DataChannelReliability(unordered, unreliable, maxPacketLifeTime, maxRetransmits);
    }

    /**
     * if unreliable, time window in milliseconds during which transmissions and retransmissions may occur
     */
    public Duration maxPacketLifeTime() {
        return Duration.ofMillis(maxPacketLifeTime);
    }

    public DataChannelReliability withMaxPacketLifeTime(Duration maxPacketLifeTime) {
        return new DataChannelReliability(unordered, unordered, maxPacketLifeTime.toMillis(), maxRetransmits);
    }

    /**
     * if unreliable and maxPacketLifeTime is 0, maximum number of attempted retransmissions (0 means no retransmission)
     */
    public int maxRetransmits() {
        return maxRetransmits;
    }

    public DataChannelReliability withMaxRetransmits(int maxRetransmits) {
        return new DataChannelReliability(unordered, unordered, maxPacketLifeTime, maxRetransmits);
    }
}
