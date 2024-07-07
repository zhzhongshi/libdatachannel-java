package tel.schich.libdatachannel;

import static tel.schich.libdatachannel.Util.ensureDirect;
import static tel.schich.libdatachannel.Util.wrapError;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

/**
 * An RTC data channel, created from a {@link PeerConnection}.
 */
public class DataChannel implements AutoCloseable {

    private final PeerConnection peer;
    private final int channelHandle;

    DataChannel(final PeerConnection peer, final int channelHandle) {
        this.peer = peer;
        this.channelHandle = channelHandle;
    }

    /**
     * Returns the {@link PeerConnection} this channel belongs to
     *
     * @return the peer connection
     */
    public PeerConnection peer() {
        return peer;
    }

    /**
     * Registers a {@link DataChannelCallback.Open}
     *
     * @param cb the callback or null to remove it
     */
    public void onOpen(DataChannelCallback.Open cb) {
        peer.listener.registerHandler(channelHandle, cb);
    }

    /**
     * Registers a {@link DataChannelCallback.Closed}
     *
     * @param cb the callback or null to remove it
     */
    public void onClose(DataChannelCallback.Closed cb) {
        peer.listener.registerHandler(channelHandle, cb);
    }

    /**
     * Registers a {@link DataChannelCallback.Error}
     *
     * @param cb the callback or null to remove it
     */
    public void onError(DataChannelCallback.Error cb) {
        peer.listener.registerHandler(channelHandle, cb);
    }

    /**
     * Registers a {@link DataChannelCallback.Message}
     *
     * @param cb the callback or null to remove it
     */
    public void onMessage(DataChannelCallback.Message cb) {
        peer.listener.registerHandler(channelHandle, cb);
    }

    /**
     * Registers a {@link DataChannelCallback.Message}
     *
     * @param cb the callback or null to remove it
     */
    public void onMessage(DataChannelCallback.TextMessage cb) {
        peer.listener.registerHandler(channelHandle, new DataChannelCallback.Message() {
            @Override
            public void onText(DataChannel channel, String text) {
                cb.onText(channel, text);
            }

            @Override
            public void onBinary(DataChannel channel, ByteBuffer buffer) {

            }
        });
    }

    /**
     * Registers a {@link DataChannelCallback.Message}
     *
     * @param cb the callback or null to remove it
     */
    public void onMessage(DataChannelCallback.BinaryMessage cb) {
        peer.listener.registerHandler(channelHandle, new DataChannelCallback.Message() {
            @Override
            public void onText(DataChannel channel, String text) {

            }

            @Override
            public void onBinary(DataChannel channel, ByteBuffer buffer) {
                onBinary(channel, buffer);
            }
        });
    }

    /**
     * Registers a {@link DataChannelCallback.BufferedAmountLow}
     *
     * @param cb the callback or null to remove it
     */
    public void onBufferedAmountLow(DataChannelCallback.BufferedAmountLow cb) {
        peer.listener.registerHandler(channelHandle, cb);
    }

    /**
     * Registers a {@link DataChannelCallback.Available}
     *
     * @param cb the callback or null to remove it
     */
    public void onAvailable(DataChannelCallback.Available cb) {
        peer.listener.registerHandler(channelHandle, cb);
    }

    private void sendMessage(ByteBuffer data, int offset, int length) {
        wrapError("sendMessage", LibDataChannelNative.rtcSendMessage(channelHandle, data, offset, length));
    }

    /**
     * Sends a binary message in the channel
     *
     * @param data the data
     */
    public void sendMessage(ByteBuffer data) {
        ensureDirect(data);
        sendMessage(data, data.position(), data.remaining());
    }

    /**
     * Sends a text message in the channel
     *
     * @param message the message
     */
    public void sendMessage(String message) {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        ByteBuffer data = ByteBuffer.allocateDirect(bytes.length + 1);
        data.put(bytes);
        data.put((byte)0);
        data.flip();
        sendMessage(data, data.position(), -1);
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
        wrapError("rtcClose", LibDataChannelNative.rtcClose(channelHandle));
        wrapError("rtcDeleteDataChannel", LibDataChannelNative.rtcDeleteDataChannel(channelHandle));
        peer.dropChannelState(channelHandle);
    }

    /**
     * Returns whether the channel exists and is closed (not open and not connecting), false otherwise
     *
     * @return true if closed
     */
    public boolean isClosed() {
        return LibDataChannelNative.rtcIsClosed(channelHandle);
    }

    /**
     * Returns whether the channel exists and is open, false otherwise
     *
     * @return true if open
     */
    public boolean isOpen() {
        return LibDataChannelNative.rtcIsOpen(channelHandle);
    }

    /**
     * Returns the maximum message size for data channels on the peer connection as negotiated with the remote peer.
     *
     * @return the maximum message size
     */
    public int maxMessageSize() {
        return wrapError("rtcMaxMessageSize", LibDataChannelNative.rtcMaxMessageSize(channelHandle));
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
        wrapError("rtcSetBufferedAmountLowThreshold", LibDataChannelNative.rtcSetBufferedAmountLowThreshold(channelHandle, amount));
    }

    /**
     * Returns a pending message if available.
     * <p>
     * The may only be called if the {@link #onMessage} callback is not set.
     * </p>
     *
     * @return the received message
     */
    public Optional<ByteBuffer> receiveMessage() {
        return Optional.of(LibDataChannelNative.rtcReceiveMessage(channelHandle));
    }

    /**
     * Returns a pending message if available.
     * <p>
     * The may only be called if the {@link #onMessage} callback is not set.
     * </p>
     *
     * @return the bytes received or the negative size of the message if the buffer was too small
     */
    public int receiveMessage(ByteBuffer buffer) {
        ensureDirect(buffer);
        return LibDataChannelNative.rtcReceiveMessageInto(channelHandle, buffer, buffer.position(), buffer.remaining());
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
        return wrapError("rtcGetAvailableAmount", LibDataChannelNative.rtcGetAvailableAmount(channelHandle));
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
        return wrapError("rtcGetBufferedAmount", LibDataChannelNative.rtcGetBufferedAmount(channelHandle));
    }

    /**
     * Returns the stream ID of this channel
     *0
     * @return the stream id
     */
    public int streamId() {
        return wrapError("rtcGetDataChannelStream", LibDataChannelNative.rtcGetDataChannelStream(channelHandle));
    }

    /**
     * Returns the label of this channel
     *
     * @return the label
     */
    public String label() {
        return LibDataChannelNative.rtcGetDataChannelLabel(channelHandle);
    }

    /**
     * Returns the protocol of this channel
     *
     * @return the protocol
     */
    public String protocol() {
        return LibDataChannelNative.rtcGetDataChannelProtocol(channelHandle);
    }

    /**
     * Returns the configured {@link DataChannelReliability} of this channel
     *
     * @return the reliability
     */
    public DataChannelReliability reliability() {
        return LibDataChannelNative.rtcGetDataChannelReliability(channelHandle);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataChannel)) return false;
        DataChannel channel = (DataChannel) o;
        return channelHandle == channel.channelHandle;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(channelHandle);
    }
}
