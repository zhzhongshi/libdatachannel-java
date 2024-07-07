package tel.schich.libdatachannel;

import static tel.schich.libdatachannel.Util.parseAddress;
import static tel.schich.libdatachannel.Util.wrapError;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PeerConnection implements AutoCloseable {
    private final int peerHandle;
    private final ConcurrentMap<Integer, DataChannelState> channels;
    private final ConcurrentMap<Integer, Track> tracks;
    final PeerConnectionListener listener;

    private PeerConnection(int peerHandle) {
        this.peerHandle = peerHandle;
        this.channels = new ConcurrentHashMap<>();
        this.tracks = new ConcurrentHashMap<>();
        this.listener = new PeerConnectionListener(this);
    }

    private static byte[] iceUrisToCStrings(Collection<URI> uris) {
        if (uris == null || uris.isEmpty()) {
            return null;
        }
        ByteArrayOutputStream iceServers = new ByteArrayOutputStream();
        for (URI server : uris) {
            iceServers.writeBytes(server.toASCIIString().getBytes(StandardCharsets.US_ASCII));
            iceServers.write(0);
        }
        return iceServers.toByteArray();
    }

    /**
     * Creates a Peer Connection.
     *
     * Remember to {@link #close()} when done.
     *
     * @param config the peer configuration
     * @return the peer connection
     */
    public static PeerConnection createPeer(PeerConnectionConfiguration config) {
        String proxyServer = null;
        if (config.proxyServer != null) {
            proxyServer = config.proxyServer.toASCIIString();
        }
        String bindAddress = null;
        if (config.bindAddress != null) {
            bindAddress = config.bindAddress.toString();
        }
        int result = LibDataChannelNative.rtcCreatePeerConnection(
                iceUrisToCStrings(config.iceServers),
                proxyServer,
                bindAddress,
                config.certificateType.state,
                config.iceTransportPolicy.state,
                config.enableIceTcp,
                config.enableIceUdpMux,
                config.disableAutoNegotiation,
                config.forceMediaTransport,
                config.portRangeBegin,
                config.portRangeEnd,
                config.mtu,
                config.maxMessageSize);

        final PeerConnection peer = new PeerConnection(wrapError("rtcCreatePeerConnection", result));
        LibDataChannelNative.setupPeerConnectionListener(peer.peerHandle, peer.listener);

        return peer;
    }

    DataChannelState channelState(int channelHandle) {
        return channels.get(channelHandle);
    }

    void dropChannelState(int channelHandle) {
        channels.remove(channelHandle);
    }

    Track trackState(int trackHandle) {
        return tracks.get(trackHandle);
    }

    public void dropTrackState(int trackHandle) {
        tracks.remove(trackHandle);
    }

    /**
     * If it is not already closed, the Peer Connection is implicitly closed before being deleted. After this function has been called, pc must not be
     * used in a function call anymore. This function will block until all scheduled callbacks of pc return (except the one this function might be
     * called in) and no other callback will be called for pc after it returns.
     */
    @Override
    public void close() {
        closeChannels();
        wrapError("rtcClosePeerConnection", LibDataChannelNative.rtcClosePeerConnection(peerHandle));
        wrapError("rtcDeletePeerConnection", LibDataChannelNative.rtcDeletePeerConnection(peerHandle));
    }

    /**
     * Closes all Data Channels.
     */
    public void closeChannels() {
        List<DataChannelState> channels = new ArrayList<>(this.channels.values());
        this.channels.clear();
        for (final DataChannelState value : channels) {
            value.channel.close();
        }
    }

    public void onLocalDescription(PeerConnectionCallback.LocalDescription cb) {
        listener.registerHandler(cb);
    }

    public void onLocalCandidate(PeerConnectionCallback.LocalCandidate cb) {
        listener.registerHandler(cb);
    }


    /**
     * Initiates the handshake process.
     * <p>
     * Following this call, the local description callback will be called with the local description, which must be sent to the remote peer by the
     * user's method of choice.
     * <p>
     * Note this call is implicit after {@link #setRemoteDescription} and {@link #createDataChannel} if
     * disableAutoNegotiation was not set on Peer Connection creation.
     *
     * @param type (optional): type of the description ("offer", "answer", "pranswer", or "rollback") or NULL for autodetection.
     */
    public void setLocalDescription(String type) {
        wrapError("rtcSetLocalDescription", LibDataChannelNative.rtcSetLocalDescription(peerHandle, type));
    }

    /**
     * Retrieves the current local description in SDP format.
     *
     * @return the current local description
     */
    public String localDescription() {
        return LibDataChannelNative.rtcGetLocalDescription(peerHandle);
    }

    /**
     * Retrieves the current local description type as string. See {@link #setLocalDescription}.
     *
     * @return the current local description type
     */
    public String localDescriptionType() {
        return LibDataChannelNative.rtcGetLocalDescriptionType(peerHandle);
    }

    /**
     * Sets the remote description received from the remote peer by the user's method of choice. The remote description may have candidates or not.
     * <p>
     * If the remote description is an offer and disableAutoNegotiation was not set in rtcConfiguration, the library will automatically answer by
     * calling rtcSetLocalDescription internally. Otherwise, the user must call it to answer the remote description.
     *
     * @param sdp  the remote Session Description Protocol
     * @param type (optional): type of the description ("offer", "answer", "pranswer", or "rollback") or NULL for autodetection.
     */
    public void setRemoteDescription(String sdp, String type) {
        wrapError("rtcSetRemoteDescription", LibDataChannelNative.rtcSetRemoteDescription(peerHandle, sdp, type));
    }

    /**
     * Retrieves the current remote description in SDP format.
     *
     * @return the current remote description
     */
    public String remoteDescription() {
        return LibDataChannelNative.rtcGetRemoteDescription(peerHandle);
    }


    /**
     * Retrieves the current remote description type as string.
     *
     * @return the current remote description type
     */
    public String remoteDescriptionType() {
        return LibDataChannelNative.rtcGetRemoteDescriptionType(peerHandle);
    }

    /**
     * Adds a trickled remote candidate received from the remote peer by the user's method of choice.
     * The Peer Connection must have a remote description set.
     *
     * @param candidate a null-terminated SDP string representing the candidate (with or without the "a=" prefix)
     */
    public void addRemoteCandidate(String candidate) {
        addRemoteCandidate(candidate, null);
    }

    /**
     * Adds a trickled remote candidate received from the remote peer by the user's method of choice.
     * The Peer Connection must have a remote description set.
     *
     * @param candidate a null-terminated SDP string representing the candidate (with or without the "a=" prefix)
     * @param mid       (optional): a null-terminated string representing the mid of the candidate in the remote SDP description or NULL for
     *                  autodetection
     */
    public void addRemoteCandidate(String candidate, String mid) {
        wrapError("rtcAddRemoteCandidate", LibDataChannelNative.rtcAddRemoteCandidate(peerHandle, candidate, mid));
    }

    /**
     * Retrieves the current local address, i.e. the network address of the currently selected local candidate. The address will have the format
     * "IP_ADDRESS:PORT", where IP_ADDRESS may be either IPv4 or IPv6. The call might fail if the PeerConnection is not in state RTC_CONNECTED, and
     * the address might change after connection.
     *
     * @return the local address
     */
    public InetSocketAddress localAddress() {
        return parseAddress(LibDataChannelNative.rtcGetLocalAddress(peerHandle));
    }

    /**
     * Retrieves the current remote address, i.e. the network address of the currently selected remote candidate. The address will have the format
     * "IP_ADDRESS:PORT", where IP_ADDRESS may be either IPv4 or IPv6. The call may fail if the state is not RTC_CONNECTED, and the address might
     * change after connection.
     *
     * @return the remote address
     */
    public InetSocketAddress remoteAddress() {
        return parseAddress(LibDataChannelNative.rtcGetRemoteAddress(peerHandle));
    }

    /**
     * Retrieves the currently selected candidate pair. The call may fail if the state is not RTC_CONNECTED, and the selected candidate pair might
     * change after connection.
     */
    public CandidatePair selectedCandidatePair() {
        return LibDataChannelNative.rtcGetSelectedCandidatePair(peerHandle);
    }


    /**
     * Retrieves the maximum stream ID a Data Channel may use. It is useful to create user-negotiated Data Channels with negotiated=true and
     * manualStream=true. The maximum is negotiated during connection, therefore the final value after connection might be lower than before
     * connection if the remote maximum is lower.
     *
     * @return maximum stream ID
     */
    public int maxDataChannelStream() {
        return LibDataChannelNative.rtcGetMaxDataChannelStream(peerHandle);
    }

    /**
     * Retrieves the maximum message size for data channels on the peer connection as negotiated with the remote peer.
     *
     * @return the maximum message size
     */
    public int remoteMaxMessageSize() {
        return LibDataChannelNative.rtcGetRemoteMaxMessageSize(peerHandle);
    }

    public void onStateChange(PeerConnectionCallback.StateChange cb) {
        listener.registerHandler(cb);
    }

    public void onIceStateChange(PeerConnectionCallback.IceStateChange cb) {
        listener.registerHandler(cb);
    }

    public void onGatheringStateChange(PeerConnectionCallback.GatheringStateChange cb) {
        listener.registerHandler(cb);
    }

    public void onSignalingStateChange(PeerConnectionCallback.SignalingStateChange cb) {
        listener.registerHandler(cb);
    }

    public void onDataChannel(PeerConnectionCallback.DataChannel cb) {
        listener.registerHandler(cb);
    }

    public void onTrack(PeerConnectionCallback.Track cb) {
        listener.registerHandler(cb);
    }


    public void setAnswer(String sdp) {
        this.setRemoteDescription(sdp, "answer");
    }


    /**
     * Adds a Data Channel on a Peer Connection. The Peer Connection does not need to be connected, however, the Data Channel will be open only when
     * the Peer Connection is connected.
     * <p>
     * rtcDataChannel() is equivalent to rtcDataChannelEx() with settings set to ordered, reliable, non-negotiated, with automatic stream ID selection
     * (all flags set to false), and protocol set to an empty string.
     *
     * @param label a user-defined UTF-8 string representing the Data Channel name
     * @return the created data channel
     */
    public DataChannel createDataChannel(String label) {
        return createDataChannel(label, DataChannelInitSettings.DEFAULT);
    }

    /**
     * Adds a Data Channel on a Peer Connection. The Peer Connection does not need to be connected, however, the Data Channel will be open only when
     * the Peer Connection is connected.
     *
     * @param label a user-defined UTF-8 string representing the Data Channel name
     * @param init  a structure of initialization settings
     * @return the created data channel
     */
    public DataChannel createDataChannel(String label, DataChannelInitSettings init) {
        final DataChannelReliability reliability = init.reliability();
        int stream = init.stream().orElse(0);
        boolean manualStream = init.stream().isPresent();
        final int channelHandle = wrapError("rtcCreateDataChannelEx", LibDataChannelNative.rtcCreateDataChannelEx(peerHandle, label, reliability.isUnordered(), reliability.isUnreliable(), reliability.maxPacketLifeTime().toMillis(), reliability.maxRetransmits(), init.protocol(), init.isNegotiated(), stream, manualStream));
        final DataChannel channel = new DataChannel(this, channelHandle);
        this.channels.put(channelHandle, new DataChannelState(channel));
        return channel;
    }

    // Adds a new Track on a Peer Connection. The Peer Connection does not need to be connected, however, the Track will be open only when the Peer Connection is connected.
    // sdp: a null-terminated string specifying the corresponding media SDP. It must start with a m-line and include a mid parameter.
    public Track addTrack(String sdp) {
        final int trackHandle = wrapError("rtcAddTrack", LibDataChannelNative.rtcAddTrack(peerHandle, sdp));
        return new Track(this, trackHandle);
    }

    public Track addTrack(TrackInit init) {
        final int trackHandle = wrapError("rtcAddTrackEx", LibDataChannelNative.rtcAddTrackEx(peerHandle, init.direction().direction, init.codec().codec));
        return new Track(this, trackHandle);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PeerConnection)) return false;
        PeerConnection that = (PeerConnection) o;
        return peerHandle == that.peerHandle;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(peerHandle);
    }
}
