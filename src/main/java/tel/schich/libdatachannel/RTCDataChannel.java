package tel.schich.libdatachannel;

import static generated.DatachannelLibrary.INSTANCE;

import generated.rtcReliability;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Optional;

public class RTCDataChannel implements AutoCloseable {

    private final RTCPeerConnection peer;
    private Integer channel;

    public RTCDataChannel(final RTCPeerConnection peer, final int channel) {
        this.peer = peer;
        this.channel = channel;
    }

    public void onOpen(ChannelCallbacks.Open cb) {
        INSTANCE.rtcSetOpenCallback(this.channel, (id, ptr) -> {
            cb.handle(this);
        });
    }

    public void onClose(ChannelCallbacks.Close cb) {
        INSTANCE.rtcSetClosedCallback(this.channel, (id, ptr) -> {
            cb.handle(this);
        });
    }

    public void onError(ChannelCallbacks.Error cb) {
        INSTANCE.rtcSetErrorCallback(this.channel, (id, error, ptr) -> {
            cb.handle(this, error.getString(0));
        });
    }

    public void onMessage(ChannelCallbacks.Message cb) {
        INSTANCE.rtcSetMessageCallback(this.channel, (id, message, size, ptr) -> {
            cb.handle(this, message.getString(0));
        });
    }
    // TODO rtcSetBufferedAmountLowCallback
    // TODO rtcSetAvailableCallback

    public void sendMessage(ByteBuffer buffer) {
        // TODO sendMessage binary
    }

    public void sendMessage(String message) {
        INSTANCE.rtcSendMessage(this.channel, message, -1);
    }

    /**
     * Close the channel.
     *
     * After this function has been called, dc must not be used in a function call anymore.
     * This function will block until all scheduled callbacks of dc return (except the one this function might be called in)
     * and no other callback will be called for dc after it returns.
     */
    @Override
    public void close() {
        INSTANCE.rtcClose(this.channel);
        INSTANCE.rtcDelete(this.channel);
        this.channel = null;
    }

    public boolean isClosed() {
        return this.channel != null && INSTANCE.rtcIsClosed(this.channel) == 1;
    }

    public boolean isOpen() {
        return this.channel != null && INSTANCE.rtcIsOpen(this.channel) == 1;
    }

    public int maxMessageSize() {
        return INSTANCE.rtcMaxMessageSize(this.channel);
    }

    // TODO rtcGetBufferedAmount
    // TODO rtcSetBufferedAmountLowThreshold

    // TODO buffer variant
    public Optional<String> receiveMessage() {
        final var size = this.maxMessageSize();
        final var buffer = ByteBuffer.allocate(size);
        final var code = INSTANCE.rtcReceiveMessage(this.channel, buffer, IntBuffer.wrap(new int[]{size}));
        if (code == 0) {
            return Optional.of(new String(buffer.array()));
        } else if (code == -3) {
            return Optional.empty();
        }
        throw new IllegalStateException("Error: " + code);
    }

    // TODO rtcGetAvailableAmount

    /**
     * Retrieves the stream ID of the Data Channel.
     *
     * @return the stream id
     */
    public int streamId()
    {
        return INSTANCE.rtcGetDataChannelStream(this.channel);
    }

    /**
     * Retrieves the label of a Data Channel.
     *
     * @return the label
     */
    public String label() {
        return JNAUtil.readStringWithBuffer(((buff, size) -> INSTANCE.rtcGetDataChannelLabel(this.channel, buff, size)));
    }

    /**
     * Retrieves the protocol of a Data Channel.
     *
     * @return the protocol
     */
    public String protocol()
    {
        return JNAUtil.readStringWithBuffer(((buff, size) -> INSTANCE.rtcGetDataChannelProtocol(this.channel, buff, size)));
    }

    // TODO rtcAddTrack
    // TODO rtcDeleteTrack? on Track obj
    // TODO rtcGetTrackDescription? on Track obj
    // TODO rtcGetTrackMid? on Track obj
    // TODO rtcGetTrackDirection? on Track obj

    public DataChannelInitSettings reliability()
    {
        final var inner = new rtcReliability();
        INSTANCE.rtcGetDataChannelReliability(this.channel, inner);
        // TODO separate class & fill it
        return new DataChannelInitSettings(inner);
    }

    // TODO rtcGetDataChannelReliability

    // TODO rtcGetDataChannelStream



    interface ChannelCallbacks {

        @FunctionalInterface
        interface Open {

            void handle(RTCDataChannel channel);
        }

        @FunctionalInterface
        interface Close {

            void handle(RTCDataChannel channel);
        }

        @FunctionalInterface
        interface Error {

            void handle(RTCDataChannel channel, final String error);
        }

        @FunctionalInterface
        interface Message {

            void handle(RTCDataChannel channel, final String message);
        }

    }
}
