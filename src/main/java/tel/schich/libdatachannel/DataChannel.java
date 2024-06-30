package tel.schich.libdatachannel;

import static generated.DatachannelLibrary.INSTANCE;
import static generated.DatachannelLibrary.RTC_ERR_NOT_AVAIL;
import static generated.DatachannelLibrary.RTC_ERR_SUCCESS;

import generated.rtcReliability;
import tel.schich.libdatachannel.util.JNAUtil;
import tel.schich.libdatachannel.util.Util;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Optional;

/**
 * An RTC data channel, created from a {@link PeerConnection}.
 */
public class DataChannel implements AutoCloseable {

    private final PeerConnection peer;
    private Integer channel;

    DataChannel(final PeerConnection peer, final int channel) {
        this.peer = peer;
        this.channel = channel;
    }

    /**
     * Returns the {@link PeerConnection} this channel belongs to
     *
     * @return the peer connection
     */
    public PeerConnection peer() {
        return this.peer;
    }

    /**
     * Registers a {@link DataChannelCallback.Open}
     *
     * @param cb the callback or null to remove it
     */
    public void onOpen(DataChannelCallback.Open cb) {
        Util.registerCallback(INSTANCE::rtcSetOpenCallback, cb, (id, ptr) -> cb.onOpen(this), this.channel);
    }

    /**
     * Registers a {@link DataChannelCallback.Close}
     *
     * @param cb the callback or null to remove it
     */
    public void onClose(DataChannelCallback.Close cb) {
        Util.registerCallback(INSTANCE::rtcSetClosedCallback, cb, (id, ptr) -> cb.onClose(this), this.channel);
    }

    /**
     * Registers a {@link DataChannelCallback.Error}
     *
     * @param cb the callback or null to remove it
     */
    public void onError(DataChannelCallback.Error cb) {
        Util.registerCallback(INSTANCE::rtcSetErrorCallback, cb, (id, error, ptr) -> cb.onError(this, error.getString(0)), this.channel);
    }

    /**
     * Registers a {@link DataChannelCallback.Message}
     *
     * @param cb the callback or null to remove it
     */
    public void onMessage(DataChannelCallback.Message cb) {
        // TODO binary message?
        Util.registerCallback(INSTANCE::rtcSetMessageCallback, cb, (id, message, size, ptr) -> {
            cb.onMessage(this, size < 0 ? message.getString(0).getBytes() : message.getByteArray(0, size), size);
        }, this.channel);
    }

    /**
     * Registers a {@link DataChannelCallback.BufferedAmountLow}
     *
     * @param cb the callback or null to remove it
     */
    public void onBufferedAmountLow(DataChannelCallback.BufferedAmountLow cb) {
        Util.registerCallback(INSTANCE::rtcSetBufferedAmountLowCallback, cb, (id, ptr) -> cb.onBufferedAmountLow(this), this.channel);
    }

    /**
     * Registers a {@link DataChannelCallback.Available}
     *
     * @param cb the callback or null to remove it
     */
    public void onAvailable(DataChannelCallback.Available cb) {
        Util.registerCallback(INSTANCE::rtcSetAvailableCallback, cb, (id, ptr) -> cb.onAvailable(this), this.channel);
    }

    /**
     * Sends a binary message in the channel
     *
     * @param data the data
     */
    public void sendMessage(byte[] data) {
        INSTANCE.rtcSendMessage(this.channel, JNAUtil.toPointer(data), data.length);
    }

    /**
     * Sends a text message in the channel
     *
     * @param message the message
     */
    public void sendMessage(String message) {
        INSTANCE.rtcSendMessage(this.channel, message, -1);
    }

    /**
     * Closes and deletes the channel
     * <p>
     * This function will block until all scheduled callbacks of this channel return (except the one this function might be called in)
     * </p>
     * No other callback will be called for channel after it returns.
     */
    @Override
    public void close() {
        Util.wrapError(INSTANCE.rtcClose(this.channel));
        Util.wrapError(INSTANCE.rtcDelete(this.channel));
        this.channel = null;
    }

    /**
     * Returns whether the channel exists and is closed (not open and not connecting), false otherwise
     *
     * @return true if closed
     */
    public boolean isClosed() {
        return this.channel != null && INSTANCE.rtcIsClosed(this.channel) == 1;
    }

    /**
     * Returns whether the channel exists and is open, false otherwise
     *
     * @return true if open
     */
    public boolean isOpen() {
        return this.channel != null && INSTANCE.rtcIsOpen(this.channel) == 1;
    }

    /**
     * Returns the maximum message size for data channels on the peer connection as negotiated with the remote peer.
     *
     * @return the maximum message size
     */
    public int maxMessageSize() {
        return Util.wrapError(INSTANCE.rtcMaxMessageSize(this.channel));
    }

    /**
     * Changes the buffered amount threshold under which {@link DataChannelCallback.BufferedAmountLow} is called.
     * <p>
     * The callback is called when the buffered amount was strictly superior and gets equal to or lower than the threshold when a message is sent.
     * </p>
     * <p>
     * The initial threshold is 0, meaning the callback is called each time the buffered amount goes back to zero after being non-zero.
     * </p>
     *
     * @param amount the amount
     */
    public void bufferedAmountLowThreshold(int amount) {
        Util.wrapError(INSTANCE.rtcSetBufferedAmountLowThreshold(this.channel, amount));
    }

    /**
     * Returns a pending message if available.
     * <p>
     * The may only be called if the {@link #onMessage} callback is not set.
     * </p>
     *
     * @return the received message
     */
    public Optional<String> receiveMessage() {
        // TODO buffer variant?
        final var size = this.maxMessageSize();
        final var buffer = ByteBuffer.allocate(size);
        final var code = INSTANCE.rtcReceiveMessage(this.channel, buffer, IntBuffer.wrap(new int[]{size}));
        if (code == RTC_ERR_SUCCESS) {
            return Optional.of(new String(buffer.array()));
        } else if (code == RTC_ERR_NOT_AVAIL) {
            return Optional.empty();
        }
        Util.wrapError(code);
        throw new IllegalStateException("Error: " + code);
    }

    /**
     * Returns the available amount, i.e. the total size of messages pending reception with {@link #receiveMessage}.
     * <p>
     * This may only be called if the {@link #onMessage} callback is not set.
     * </p>
     *
     * @return the available amount
     */
    public int availableAmount() {
        return Util.wrapError(INSTANCE.rtcGetAvailableAmount(this.channel));
    }

    /**
     * Returns the current buffered amount, i.e. the total size of currently buffered messages waiting to be actually sent in the channel.
     * <p>
     * This does not account for the data buffered at the transport level.
     * </p>
     *
     * @return the buffered amount
     */
    public int bufferedAmount() {
        return Util.wrapError(INSTANCE.rtcGetBufferedAmount(this.channel));
    }

    /**
     * Returns the stream ID of this channel
     *0
     * @return the stream id
     */
    public int streamId() {
        return Util.wrapError(INSTANCE.rtcGetDataChannelStream(this.channel));
    }

    /**
     * Returns the label of this channel
     *
     * @return the label
     */
    public String label() {
        return JNAUtil.readStringWithBuffer(((buff, size) -> INSTANCE.rtcGetDataChannelLabel(this.channel, buff, size)));
    }

    /**
     * Returns the protocol of this channel
     *
     * @return the protocol
     */
    public String protocol() {
        return JNAUtil.readStringWithBuffer(((buff, size) -> INSTANCE.rtcGetDataChannelProtocol(this.channel, buff, size)));
    }

    /**
     * Returns the configured {@link DataChannelReliability} of this channel
     *
     * @return the reliability
     */
    public DataChannelReliability reliability() {
        final var inner = new rtcReliability();
        Util.wrapError(INSTANCE.rtcGetDataChannelReliability(this.channel, inner));
        return new DataChannelReliability(inner);
    }


    // Adds a new Track on a Peer Connection. The Peer Connection does not need to be connected, however, the Track will be open only when the Peer Connection is connected.
    // sdp: a null-terminated string specifying the corresponding media SDP. It must start with a m-line and include a mid parameter.
    @SuppressWarnings("deprecation")
    public Track addTrack(String sdp) {
        // TODO implement me
        final var track = Util.wrapError(INSTANCE.rtcAddTrack(this.channel, sdp));
        // TODO final var track = Util.wrapError(INSTANCE.rtcAddTrackEx(this.channel, sdp));
        return new Track(track, this);
    }


}
