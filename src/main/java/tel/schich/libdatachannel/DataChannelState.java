package tel.schich.libdatachannel;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class DataChannelState {
    final DataChannel channel;

    final List<DataChannelCallback.Open> openHandlers = new CopyOnWriteArrayList<>();
    final List<DataChannelCallback.Closed> closedHandlers = new CopyOnWriteArrayList<>();
    final List<DataChannelCallback.Error> errorHandlers = new CopyOnWriteArrayList<>();
    final List<DataChannelCallback.Message> messageHandlers = new CopyOnWriteArrayList<>();
    final List<DataChannelCallback.BufferedAmountLow> bufferedAmountLowHandlers = new CopyOnWriteArrayList<>();
    final List<DataChannelCallback.Available> availableHandlers = new CopyOnWriteArrayList<>();

    DataChannelState(DataChannel channel) {
        this.channel = channel;
    }

    void registerHandler(DataChannelCallback.Open handler) {
        openHandlers.add(handler);
    }

    void registerHandler(DataChannelCallback.Closed handler) {
        closedHandlers.add(handler);
    }

    void registerHandler(DataChannelCallback.Error handler) {
        errorHandlers.add(handler);
    }

    void registerHandler(DataChannelCallback.Message handler) {
        messageHandlers.add(handler);
    }

    void registerHandler(DataChannelCallback.BufferedAmountLow handler) {
        bufferedAmountLowHandlers.add(handler);
    }

    void registerHandler(DataChannelCallback.Available handler) {
        availableHandlers.add(handler);
    }

}
