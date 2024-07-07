package tel.schich.libdatachannel;

import org.w3c.dom.Text;

import java.nio.ByteBuffer;

/**
 * Callback interfaces for {@link DataChannel}
 */
interface DataChannelCallback {

    /**
     * Called when the channel was previously connecting and is now open.
     */
    @FunctionalInterface
    interface Open {

        void onOpen(DataChannel channel);
    }

    /**
     * Called when the channel was previously open and is now closed.
     */
    @FunctionalInterface
    interface Closed {

        void onClose(DataChannel channel);
    }

    /**
     * Called when the channel experiences an error, either while connecting or open.
     */
    @FunctionalInterface
    interface Error {

        void onError(DataChannel channel, final String error);
    }

    /**
     * Called when the channel receives a message.
     * <p>
     * While set, messages can't be received with {@link DataChannel#receiveMessage()}.
     * </p>
     */
    interface Message extends TextMessage, BinaryMessage {
    }

    /**
     * Called when the channel receives a message.
     * <p>
     * While set, messages can't be received with {@link DataChannel#receiveMessage()}.
     * </p>
     */
    @FunctionalInterface
    interface TextMessage {
        void onText(DataChannel channel, String text);
    }

    /**
     * Called when the channel receives a message.
     * <p>
     * While set, messages can't be received with {@link DataChannel#receiveMessage()}.
     * </p>
     */
    @FunctionalInterface
    interface BinaryMessage {
        void onBinary(DataChannel channel, ByteBuffer buffer);
    }

    /**
     * Called when the buffered amount was strictly higher than the threshold (see {@link DataChannel#bufferedAmountLowThreshold}) and is now lower or
     * equal than the threshold.
     */
    @FunctionalInterface
    interface BufferedAmountLow {

        void onBufferedAmountLow(DataChannel channel);
    }

    /**
     * Called when messages are now available to be received with {@link DataChannel#receiveMessage()}.
     */
    @FunctionalInterface
    interface Available {

        void onAvailable(DataChannel channel);
    }

}
