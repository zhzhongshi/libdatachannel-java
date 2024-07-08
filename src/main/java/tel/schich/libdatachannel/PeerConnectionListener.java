package tel.schich.libdatachannel;

import tel.schich.jniaccess.JNIAccess;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetAvailableCallback;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetBufferedAmountLowCallback;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetClosedCallback;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetDataChannelCallback;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetErrorCallback;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetGatheringStateChangeCallback;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetIceStateChangeCallback;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetLocalCandidateCallback;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetLocalDescriptionCallback;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetMessageCallback;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetOpenCallback;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetSignalingStateChangeCallback;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetStateChangeCallback;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetTrackCallback;

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

    public PeerConnectionListener(PeerConnection peer) {
        this.peer = peer;
    }

    void registerHandler(PeerConnectionCallback.LocalDescription handler) {
        rtcSetLocalDescriptionCallback(peer.peerHandle);
        localDescription.add(handler);
    }

    void registerHandler(PeerConnectionCallback.LocalCandidate handler) {
        rtcSetLocalCandidateCallback(peer.peerHandle);
        localCandidate.add(handler);
    }

    void registerHandler(PeerConnectionCallback.StateChange handler) {
        rtcSetStateChangeCallback(peer.peerHandle);
        stateChange.add(handler);
    }

    void registerHandler(PeerConnectionCallback.IceStateChange handler) {
        rtcSetIceStateChangeCallback(peer.peerHandle);
        iceStateChange.add(handler);
    }

    void registerHandler(PeerConnectionCallback.GatheringStateChange handler) {
        rtcSetGatheringStateChangeCallback(peer.peerHandle);
        gatheringStateChange.add(handler);
    }

    void registerHandler(PeerConnectionCallback.SignalingStateChange handler) {
        rtcSetSignalingStateChangeCallback(peer.peerHandle);
        signalingStateChange.add(handler);
    }

    void registerHandler(PeerConnectionCallback.DataChannel handler) {
        rtcSetDataChannelCallback(peer.peerHandle);
        dataChannel.add(handler);
    }

    void registerHandler(PeerConnectionCallback.Track handler) {
        rtcSetTrackCallback(peer.peerHandle);
        track.add(handler);
    }

    private <T> void addChannelHandler(int handle, BiConsumer<DataChannelState, T> consumer, T handler) {
        final DataChannelState state = peer.channelState(handle);
        if (state != null) {
            consumer.accept(state, handler);
        }
    }

    void registerHandler(int channelHandle, DataChannelCallback.Open handler) {
        addChannelHandler(channelHandle, DataChannelState::registerHandler, handler);
    }

    void registerHandler(int channelHandle, DataChannelCallback.Closed handler) {
        addChannelHandler(channelHandle, DataChannelState::registerHandler, handler);
    }

    void registerHandler(int channelHandle, DataChannelCallback.Error handler) {
        addChannelHandler(channelHandle, DataChannelState::registerHandler, handler);
    }

    void registerHandler(int channelHandle, DataChannelCallback.Message handler) {
        addChannelHandler(channelHandle, DataChannelState::registerHandler, handler);
    }

    void registerHandler(int channelHandle, DataChannelCallback.BufferedAmountLow handler) {
        addChannelHandler(channelHandle, DataChannelState::registerHandler, handler);
    }

    void registerHandler(int channelHandle, DataChannelCallback.Available handler) {
        addChannelHandler(channelHandle, DataChannelState::registerHandler, handler);
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
        final DataChannelState state = peer.channelState(channelHandle);
        for (PeerConnectionCallback.DataChannel handler : dataChannel) {
            handler.handleChannel(peer, state.channel);
        }
    }

    @JNIAccess
    void onTrack(int trackHandle) {
        final Track track = peer.trackState(trackHandle);
        for (PeerConnectionCallback.Track handler : this.track) {
            handler.handleTrack(peer, track);
        }
    }

    private void withChannel(int handle, Consumer<DataChannelState> consumer) {
        final DataChannelState state = peer.channelState(handle);
        if (state != null) {
            consumer.accept(state);
        }
    }

    @JNIAccess
    void onChannelOpen(int channelHandle) {
        withChannel(channelHandle, s -> {
            for (DataChannelCallback.Open handler : s.openHandlers) {
                handler.onOpen(s.channel);
            }
        });
    }

    @JNIAccess
    void onChannelClosed(int channelHandle) {
        withChannel(channelHandle, s -> {
            for (DataChannelCallback.Closed handler : s.closedHandlers) {
                handler.onClose(s.channel);
            }
        });
    }

    @JNIAccess
    void onChannelError(int channelHandle, String error) {
        withChannel(channelHandle, s -> {
            for (DataChannelCallback.Error handler : s.errorHandlers) {
                handler.onError(s.channel, error);
            }
        });
    }

    @JNIAccess
    void onChannelTextMessage(int channelHandle, String message) {
        withChannel(channelHandle, s -> {
            for (DataChannelCallback.Message handler : s.messageHandlers) {
                handler.onText(s.channel, message);
            }
        });
    }

    @JNIAccess
    void onChannelBinaryMessage(int channelHandle, ByteBuffer message) {
        withChannel(channelHandle, s -> {
            for (DataChannelCallback.Message handler : s.messageHandlers) {
                handler.onBinary(s.channel, message);
            }
        });
    }

    @JNIAccess
    void onChannelBufferedAmountLow(int channelHandle) {
        withChannel(channelHandle, s -> {
            for (DataChannelCallback.BufferedAmountLow handler : s.bufferedAmountLowHandlers) {
                handler.onBufferedAmountLow(s.channel);
            }
        });
    }

    @JNIAccess
    void onChannelAvailable(int channelHandle) {
        withChannel(channelHandle, s -> {
            for (DataChannelCallback.Available handler : s.availableHandlers) {
                handler.onAvailable(s.channel);
            }
        });
    }
}
