package tel.schich.libdatachannel;

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
    interface Close {

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
    @FunctionalInterface
    interface Message {

        void onMessage(DataChannel channel, final byte[] message, final int size);
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
