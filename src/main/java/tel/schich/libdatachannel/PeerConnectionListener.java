package tel.schich.libdatachannel;

import tel.schich.jniaccess.JNIAccess;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;
import java.util.function.Function;

class PeerConnectionListener {
    private final PeerConnection peer;

    public PeerConnectionListener(final PeerConnection peer) {
        this.peer = peer;
    }

    @JNIAccess
    void onLocalDescription(String sdp, String type) {
        peer.onLocalDescription.invoke(h -> h.handleDescription(peer, sdp, type));
    }

    @JNIAccess
    void onLocalCandidate(String candidate, String mediaId) {
        peer.onLocalCandidate.invoke(h -> h.handleCandidate(peer, candidate, mediaId));
    }

    @JNIAccess
    void onStateChange(int state) {
        final PeerState s = PeerState.of(state);
        peer.onStateChange.invoke(h -> h.handleChange(peer, s));
    }

    @JNIAccess
    void onIceStateChange(int iceState) {
        final IceState s = IceState.of(iceState);
        peer.onIceStateChange.invoke(h -> h.handleChange(peer, s));
    }

    @JNIAccess
    void onGatheringStateChange(int gatheringState) {
        final GatheringState s = GatheringState.of(gatheringState);
        peer.onGatheringStateChange.invoke(h -> h.handleChange(peer, s));
    }

    @JNIAccess
    void onSignalingStateChange(int signalingState) {
        final SignalingState s = SignalingState.of(signalingState);
        peer.onSignalingStateChange.invoke(h -> h.handleChange(peer, s));
    }

    @JNIAccess
    void onDataChannel(int channelHandle) {
        final DataChannel channel = peer.channel(channelHandle);
        peer.onDataChannel.invoke(h -> h.handleChannel(peer, channel));
    }

    @JNIAccess
    void onTrack(int trackHandle) {
        final Track state = peer.trackState(trackHandle);
        peer.onTrack.invoke(h -> h.handleTrack(peer, state));
    }

    private <T> void invokeWithChannel(int handle, Function<DataChannel, EventListenerContainer<T>> listeners, BiConsumer<T, DataChannel> consumer) {
        final DataChannel channel = peer.channel(handle);
        listeners.apply(channel).invoke(h -> consumer.accept(h, channel));
    }

    @JNIAccess
    void onChannelOpen(int channelHandle) {
        invokeWithChannel(channelHandle, s -> s.onOpen, DataChannelCallback.Open::onOpen);
    }

    @JNIAccess
    void onChannelClosed(int channelHandle) {
        invokeWithChannel(channelHandle, s -> s.onClosed, DataChannelCallback.Closed::onClosed);
    }

    @JNIAccess
    void onChannelError(int channelHandle, String error) {
        invokeWithChannel(channelHandle, s -> s.onError, (h, ch) -> h.onError(ch, error));
    }

    @JNIAccess
    void onChannelTextMessage(int channelHandle, String message) {
        invokeWithChannel(channelHandle, s -> s.onMessage, (h, ch) -> h.onText(ch, message));
    }

    @JNIAccess
    void onChannelBinaryMessage(int channelHandle, ByteBuffer message) {
        invokeWithChannel(channelHandle, s -> s.onMessage, (h, ch) -> h.onBinary(ch, message));
    }

    @JNIAccess
    void onChannelBufferedAmountLow(int channelHandle) {
        invokeWithChannel(channelHandle, s -> s.onBufferedAmountLow, DataChannelCallback.BufferedAmountLow::onBufferedAmountLow);
    }

    @JNIAccess
    void onChannelAvailable(int channelHandle) {
        invokeWithChannel(channelHandle, s -> s.onAvailable, DataChannelCallback.Available::onAvailable);
    }
}
