package tel.schich.libdatachannel;

import tel.schich.jniaccess.JNIAccess;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

class PeerConnectionListener {
    private final PeerConnection peer;
    private final List<PeerConnectionCallback.LocalDescription> localDescription = new CopyOnWriteArrayList<>();
    private final List<PeerConnectionCallback.LocalCandidate> localCandidate = new CopyOnWriteArrayList<>();
    private final List<PeerConnectionCallback.StateChange> stateChange = new CopyOnWriteArrayList<>();
    private final List<PeerConnectionCallback.IceStateChange> iceStateChange = new CopyOnWriteArrayList<>();
    private final List<PeerConnectionCallback.GatheringStateChange> gatheringStateChange = new CopyOnWriteArrayList<>();
    private final List<PeerConnectionCallback.SignalingStateChange> signalingStateChange = new CopyOnWriteArrayList<>();
    private final List<PeerConnectionCallback.DataChannel> dataChannel = new CopyOnWriteArrayList<>();
    private final List<PeerConnectionCallback.Track> track = new CopyOnWriteArrayList<>();
    private final ConcurrentMap<Integer, List<DataChannelCallback.Open>> channelOpen = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, List<DataChannelCallback.Closed>> channelClosed = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, List<DataChannelCallback.Error>> channelError = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, List<DataChannelCallback.Message>> channelMessage = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, List<DataChannelCallback.BufferedAmountLow>> channelBufferedAmountLow = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, List<DataChannelCallback.Available>> channelAvailable = new ConcurrentHashMap<>();

    public PeerConnectionListener(PeerConnection peer) {
        this.peer = peer;
    }

    void registerHandler(PeerConnectionCallback.LocalDescription handler) {
        localDescription.add(handler);
    }

    void registerHandler(PeerConnectionCallback.LocalCandidate handler) {
        localCandidate.add(handler);
    }

    void registerHandler(PeerConnectionCallback.StateChange handler) {
        stateChange.add(handler);
    }

    void registerHandler(PeerConnectionCallback.IceStateChange handler) {
        iceStateChange.add(handler);
    }

    void registerHandler(PeerConnectionCallback.GatheringStateChange handler) {
        gatheringStateChange.add(handler);
    }

    void registerHandler(PeerConnectionCallback.SignalingStateChange handler) {
        signalingStateChange.add(handler);
    }

    void registerHandler(PeerConnectionCallback.DataChannel handler) {
        dataChannel.add(handler);
    }

    void registerHandler(PeerConnectionCallback.Track handler) {
        track.add(handler);
    }

    private static <T> void addChannelHandler(int handle, ConcurrentMap<Integer, List<T>> handlers, T handler) {
        handlers.computeIfAbsent(handle, (ignored) -> new CopyOnWriteArrayList<>()).add(handler);
    }

    void registerHandler(int channelHandle, DataChannelCallback.Open handler) {
        addChannelHandler(channelHandle, channelOpen, handler);
    }

    void registerHandler(int channelHandle, DataChannelCallback.Closed handler) {
        addChannelHandler(channelHandle, channelClosed, handler);
    }

    void registerHandler(int channelHandle, DataChannelCallback.Error handler) {
        addChannelHandler(channelHandle, channelError, handler);
    }

    void registerHandler(int channelHandle, DataChannelCallback.Message handler) {
        addChannelHandler(channelHandle, channelMessage, handler);
    }

    void registerHandler(int channelHandle, DataChannelCallback.BufferedAmountLow handler) {
        addChannelHandler(channelHandle, channelBufferedAmountLow, handler);
    }

    void registerHandler(int channelHandle, DataChannelCallback.Available handler) {
        addChannelHandler(channelHandle, channelAvailable, handler);
    }

    @JNIAccess
    void onLocalDescription(String sdp, String type) {
        for (PeerConnectionCallback.LocalDescription handler : localDescription) {
            handler.handleDescription(peer, sdp, type);
        }
    }

    @JNIAccess
    void onLocalCandidate(String candidate, String mediaId) {
        for (PeerConnectionCallback.LocalCandidate handler : localCandidate) {
            handler.handleCandidate(peer, candidate, mediaId);
        }
    }

    @JNIAccess
    void onStateChange(int state) {
        final PeerState s = PeerState.of(state);
        for (PeerConnectionCallback.StateChange handler : stateChange) {
            handler.handleChange(peer, s);
        }
    }

    @JNIAccess
    void onIceStateChange(int iceState) {
        final IceState s = IceState.of(iceState);
        for (PeerConnectionCallback.IceStateChange handler : iceStateChange) {
            handler.handleChange(peer, s);
        }
    }

    @JNIAccess
    void onGatheringStateChange(int gatheringState) {
        final GatheringState s = GatheringState.of(gatheringState);
        for (PeerConnectionCallback.GatheringStateChange handler : gatheringStateChange) {
            handler.handleChange(peer, s);
        }
    }

    @JNIAccess
    void onSignalingStateChange(int signalingState) {
        final SignalingState s = SignalingState.of(signalingState);
        for (PeerConnectionCallback.SignalingStateChange handler : signalingStateChange) {
            handler.handleChange(peer, s);
        }
    }

    @JNIAccess
    void onDataChannel(int channelHandle) {
        final DataChannel channel = peer.getChannel(channelHandle);
        for (PeerConnectionCallback.DataChannel handler : dataChannel) {
            handler.handleChannel(peer, channel);
        }
    }

    @JNIAccess
    void onTrack(int trackHandle) {
        final Track track = peer.getTrack(trackHandle);
        for (PeerConnectionCallback.Track handler : this.track) {
            handler.handleTrack(peer, track);
        }
    }

    @JNIAccess
    void onChannelOpen(int channelHandle) {
        final DataChannel channel = peer.getChannel(channelHandle);
        for (DataChannelCallback.Open handler : channelOpen.get(channelHandle)) {
            handler.onOpen(channel);
        }
    }

    @JNIAccess
    void onChannelClosed(int channelHandle) {
        final DataChannel channel = peer.getChannel(channelHandle);
        for (DataChannelCallback.Closed handler : channelClosed.get(channelHandle)) {
            handler.onClose(channel);
        }
    }

    @JNIAccess
    void onChannelError(int channelHandle, String error) {
        final DataChannel channel = peer.getChannel(channelHandle);
        for (DataChannelCallback.Error handler : channelError.get(channelHandle)) {
            handler.onError(channel, error);
        }
    }

    @JNIAccess
    void onChannelTextMessage(int channelHandle, String message) {
        final DataChannel channel = peer.getChannel(channelHandle);
        for (DataChannelCallback.Message handler : channelMessage.get(channelHandle)) {
            handler.onText(channel, message);
        }
    }

    @JNIAccess
    void onChannelBinaryMessage(int channelHandle, ByteBuffer message) {
        final DataChannel channel = peer.getChannel(channelHandle);
        for (DataChannelCallback.Message handler : channelMessage.get(channelHandle)) {
            handler.onBinary(channel, message);
        }
    }

    @JNIAccess
    void onChannelBufferedAmountLow(int channelHandle) {
        final DataChannel channel = peer.getChannel(channelHandle);
        for (DataChannelCallback.BufferedAmountLow handler : channelBufferedAmountLow.get(channelHandle)) {
            handler.onBufferedAmountLow(channel);
        }
    }

    @JNIAccess
    void onChannelAvailable(int channelHandle) {
        final DataChannel channel = peer.getChannel(channelHandle);
        for (DataChannelCallback.Available handler : channelAvailable.get(channelHandle)) {
            handler.onAvailable(channel);
        }
    }
}
