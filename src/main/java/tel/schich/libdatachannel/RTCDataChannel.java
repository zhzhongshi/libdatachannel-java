package tel.schich.libdatachannel;

import static generated.DatachannelLibrary.INSTANCE;
import static generated.DatachannelLibrary.RTC_ERR_NOT_AVAIL;

import generated.rtcReliability;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class RTCDataChannel implements AutoCloseable {

    private final RTCPeerConnection peer;
    private Integer channel;

    public RTCDataChannel(final RTCPeerConnection peer, final int channel) {
        this.peer = peer;
        this.channel = channel;
    }

    public RTCPeerConnection peer() {
        return this.peer;
    }

    private <C, A> void registerCallback(final BiFunction<Integer, C, Integer> cbRegister, final A apiCb, final C cb) {
        if (apiCb == null) {
            cbRegister.apply(this.channel, null);
        } else {
            cbRegister.apply(this.channel, cb);
        }
    }

    public void onOpen(ChannelCallbacks.Open cb) {
        this.registerCallback(INSTANCE::rtcSetOpenCallback, cb, (id, ptr) -> cb.onOpen(this));
    }

    public void onClose(ChannelCallbacks.Close cb) {
        this.registerCallback(INSTANCE::rtcSetClosedCallback, cb, (id, ptr) -> cb.onClose(this));
    }

    public void onError(ChannelCallbacks.Error cb) {
        this.registerCallback(INSTANCE::rtcSetErrorCallback, cb, (id, error, ptr) -> cb.onError(this, error.getString(0)));
    }

    public void onMessage(ChannelCallbacks.Message cb) {
        // TODO binary message?
        this.registerCallback(INSTANCE::rtcSetMessageCallback, cb, (id, message, size, ptr) -> {
            cb.onMessage(this, size < 0 ? message.getString(0).getBytes() : message.getByteArray(0, size), size);
        });
    }

    public void onBufferedAmountLow(ChannelCallbacks.BufferedAmountLow cb) {
        this.registerCallback(INSTANCE::rtcSetBufferedAmountLowCallback, cb, (id, ptr) -> cb.onBufferedAmountLow(this));
    }

    public void onAvailable(ChannelCallbacks.Available cb) {
        this.registerCallback(INSTANCE::rtcSetAvailableCallback, cb, (id, ptr) -> cb.onAvailable(this));
    }

    /**
     * Sends a binary message in the channel.
     *
     * @param data the data
     */
    public void sendMessage(byte[] data) {
        INSTANCE.rtcSendMessage(this.channel, JNAUtil.toPointer(data), data.length);
    }

    /**
     * Sends a text message in the channel.
     *
     * @param message the message
     */
    public void sendMessage(String message) {
        INSTANCE.rtcSendMessage(this.channel, message, -1);
    }

    /**
     * Close the channel.
     * <p>
     * After this function has been called, dc must not be used in a function call anymore. This function will block until all scheduled callbacks of
     * dc return (except the one this function might be called in) and no other callback will be called for dc after it returns.
     */
    @Override
    public void close() {
        INSTANCE.rtcClose(this.channel);
        INSTANCE.rtcDelete(this.channel);
        this.channel = null;
    }

    /**
     * true if the channel exists and is open, false otherwise
     *
     * @return
     */
    public boolean isClosed() {
        return this.channel != null && INSTANCE.rtcIsClosed(this.channel) == 1;
    }

    /**
     * true if the channel exists and is closed (not open and not connecting), false otherwise
     *
     * @return
     */
    public boolean isOpen() {
        return this.channel != null && INSTANCE.rtcIsOpen(this.channel) == 1;
    }

    public int maxMessageSize() {
        return INSTANCE.rtcMaxMessageSize(this.channel);
    }

    /**
     * Changes the buffered amount threshold under which BufferedAmountLowCallback is called. The callback is called when the buffered amount was
     * strictly superior and gets equal to or lower than the threshold when a message is sent. The initial threshold is 0, meaning the callback is
     * called each time the buffered amount goes back to zero after being non-zero.
     *
     * @param amount the amount
     * @return
     */
    public void bufferedAmountLowThreshold(int amount) {
        var code = INSTANCE.rtcSetBufferedAmountLowThreshold(this.channel, amount);
    }

    /**
     * Receives a pending message if possible. The function may only be called if {@link #onMessage MessageCallback} is not set.
     *
     * @return the received message
     */
    public Optional<String> receiveMessage() {
        // TODO buffer variant?
        final var size = this.maxMessageSize();
        final var buffer = ByteBuffer.allocate(size);
        final var code = INSTANCE.rtcReceiveMessage(this.channel, buffer, IntBuffer.wrap(new int[]{size}));
        if (code == 0) {
            return Optional.of(new String(buffer.array()));
        } else if (code == RTC_ERR_NOT_AVAIL) {
            return Optional.empty();
        }
        throw new IllegalStateException("Error: " + code);
    }

    /**
     * Retrieves the available amount, i.e. the total size of messages pending reception with rtcReceiveMessage. The function may only be called if
     * {@link #onMessage MessageCallback} is not set.
     *
     * @return the available amount
     */
    public int availableAmount() {
        return INSTANCE.rtcGetAvailableAmount(this.channel);
    }

    /**
     * Retrieves the current buffered amount, i.e. the total size of currently buffered messages waiting to be actually sent in the channel. This does
     * not account for the data buffered at the transport level.
     *
     * @return the buffered amount
     */
    public int bufferedAmount() {
        return INSTANCE.rtcGetBufferedAmount(this.channel);
    }


    /**
     * Retrieves the stream ID of the Data Channel.
     *
     * @return the stream id
     */
    public int streamId() {
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
    public String protocol() {
        return JNAUtil.readStringWithBuffer(((buff, size) -> INSTANCE.rtcGetDataChannelProtocol(this.channel, buff, size)));
    }

    // TODO rtcAddTrack
    // TODO rtcDeleteTrack? on Track obj
    // TODO rtcGetTrackDescription? on Track obj
    // TODO rtcGetTrackMid? on Track obj
    // TODO rtcGetTrackDirection? on Track obj

    public DataChannelReliability reliability() {
        final var inner = new rtcReliability();
        INSTANCE.rtcGetDataChannelReliability(this.channel, inner);
        return new DataChannelReliability(inner);
    }


    interface ChannelCallbacks {

        @FunctionalInterface
        interface Open {
            void onOpen(RTCDataChannel channel);
        }

        @FunctionalInterface
        interface Close {
            void onClose(RTCDataChannel channel);
        }

        @FunctionalInterface
        interface Error {
            void onError(RTCDataChannel channel, final String error);
        }

        @FunctionalInterface
        interface Message {
            void onMessage(RTCDataChannel channel, final byte[] message, final int size);
        }

        @FunctionalInterface
        interface BufferedAmountLow {
            void onBufferedAmountLow(RTCDataChannel channel);
        }

        @FunctionalInterface
        interface Available {
            void onAvailable(RTCDataChannel channel);
        }

    }
}
