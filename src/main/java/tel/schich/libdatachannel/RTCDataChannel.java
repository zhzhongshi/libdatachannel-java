package tel.schich.libdatachannel;

import static generated.DatachannelLibrary.INSTANCE;

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

    public String getLabel() {
        final var buffer = ByteBuffer.allocate(1024);
        final var code = INSTANCE.rtcGetDataChannelLabel(this.channel, buffer, 1024);
        if (code == 0) {
            return new String(buffer.array());
        }
        throw new IllegalStateException("Error: " + code);
    }

    // TODO rtcGetDataChannelProtocol
    // TODO rtcGetDataChannelReliability

    static interface ChannelCallbacks {

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

        interface MessageHandler {

            void handle(int channel, String message);
        }
    }
}
