package tel.schich.libdatachannel;

interface DataChannelCallbacks {

    /**
     * It is called when the channel was previously connecting and is now open.
     */
    @FunctionalInterface
    interface Open {

        void onOpen(RTCDataChannel channel);
    }

    /**
     * It is called when the channel was previously open and is now closed.
     */
    @FunctionalInterface
    interface Close {

        void onClose(RTCDataChannel channel);
    }

    /**
     * It is called when the channel experience an error, either while connecting or open.
     */
    @FunctionalInterface
    interface Error {

        void onError(RTCDataChannel channel, final String error);
    }

    /**
     * It is called when the channel receives a message. While it is set, messages can't be received with rtcReceiveMessage.
     */
    @FunctionalInterface
    interface Message {

        void onMessage(RTCDataChannel channel, final byte[] message, final int size);
    }

    /**
     * It is called when the buffered amount was strictly higher than the threshold (see rtcSetBufferedAmountLowThreshold) and is now lower or equal than the threshold.
     */
    @FunctionalInterface
    interface BufferedAmountLow {

        void onBufferedAmountLow(RTCDataChannel channel);
    }

    /**
     * It is called when messages are now available to be received with rtcReceiveMessage.
     */
    @FunctionalInterface
    interface Available {

        void onAvailable(RTCDataChannel channel);
    }

}
