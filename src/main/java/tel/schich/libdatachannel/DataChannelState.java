package tel.schich.libdatachannel;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetAvailableCallback;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetBufferedAmountLowCallback;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetClosedCallback;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetErrorCallback;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetMessageCallback;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetOpenCallback;

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
        rtcSetOpenCallback(channel.channelHandle);
        openHandlers.add(handler);
    }

    void registerHandler(DataChannelCallback.Closed handler) {
        rtcSetClosedCallback(channel.channelHandle);
        closedHandlers.add(handler);
    }

    void registerHandler(DataChannelCallback.Error handler) {
        rtcSetErrorCallback(channel.channelHandle);
        errorHandlers.add(handler);
    }

    void registerHandler(DataChannelCallback.Message handler) {
        rtcSetMessageCallback(channel.channelHandle);
        messageHandlers.add(handler);
    }

    void registerHandler(DataChannelCallback.BufferedAmountLow handler) {
        rtcSetBufferedAmountLowCallback(channel.channelHandle);
        bufferedAmountLowHandlers.add(handler);
    }

    void registerHandler(DataChannelCallback.Available handler) {
        rtcSetAvailableCallback(channel.channelHandle);
        availableHandlers.add(handler);
    }

}
