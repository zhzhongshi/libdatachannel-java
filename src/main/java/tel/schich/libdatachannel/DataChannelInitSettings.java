package tel.schich.libdatachannel;

import java.util.OptionalInt;

/**
 * The {@link DataChannel} initialization settings.
 */
public class DataChannelInitSettings {
    public static final DataChannelInitSettings DEFAULT = new DataChannelInitSettings(DataChannelReliability.DEFAULT, null, false, null);

    private final DataChannelReliability reliability;
	private final String protocol;
    private final boolean negotiated;
    private final Integer stream; // numeric ID 0-65534, ignored if manualStream is false

    private DataChannelInitSettings(DataChannelReliability reliability, String protocol, boolean negotiated, Integer stream) {
        this.reliability = reliability;
        this.protocol = protocol;
        this.negotiated = negotiated;
        this.stream = stream;
    }

    public DataChannelReliability reliability() {
        return reliability;
    }

    public DataChannelInitSettings withReliability(DataChannelReliability reliability) {
        return new DataChannelInitSettings(reliability, protocol, negotiated, stream);
    }

    /**
     * (optional): a user-defined UTF-8 string representing the Data Channel protocol, empty if NULL
     */
    public DataChannelInitSettings withProtocol(String protocol) {
        return new DataChannelInitSettings(reliability, protocol, negotiated, stream);
    }

    public String protocol() {
        return protocol;
    }

    /**
     * if true, the Data Channel is assumed to be negotiated by the user and won't be negotiated by the WebRTC layer
     */
    public DataChannelInitSettings withNegotiated(boolean negotiated) {
        return new DataChannelInitSettings(reliability, protocol, negotiated, stream);
    }

    public boolean isNegotiated() {
        return negotiated;
    }

    /**
     * if manualStream is true, the Data Channel will use it as stream ID, else it is ignored
     */
    public DataChannelInitSettings withStream(Integer stream) {
        return new DataChannelInitSettings(reliability, protocol, negotiated, stream);
    }

    public OptionalInt stream() {
        if (stream == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(stream);
    }
}
