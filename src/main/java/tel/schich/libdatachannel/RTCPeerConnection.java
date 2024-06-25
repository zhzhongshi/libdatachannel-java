package tel.schich.libdatachannel;

import static generated.DatachannelLibrary.INSTANCE;

import com.sun.jna.ptr.PointerByReference;
import generated.rtcConfiguration;
import generated.rtcDataChannelInit;

import java.util.HashMap;
import java.util.Map;

public class RTCPeerConnection implements AutoCloseable {

    private Integer peer;
    private Map<Integer, RTCDataChannel> channels = new HashMap<>();


    public static RTCPeerConnection createPeer(RTCConfiguration config) {
        final var peer = new RTCPeerConnection();
        peer.peer = INSTANCE.rtcCreatePeerConnection(config.config);
        return peer;
    }


    // TODO rtcSetLocalDescription

    public void setRemoteDescription(String sdp, String type) {
        // type (optional): type of the description ("offer", "answer", "pranswer", or "rollback") or NULL for autodetection.
        final var code = INSTANCE.rtcSetRemoteDescription(this.peer, sdp, "answer");
    }

    public void setAnswer(String sdp) {
        this.setRemoteDescription(sdp, "answer");
    }

    // TODO rtcAddRemoteCandidate
    // TODO rtcGetLocalDescription
    // TODO rtcGetRemoteDescription
    // TODO rtcGetLocalDescriptionType
    // TODO rtcGetLocalAddress
    // TODO rtcGetRemoteAddress
    // TODO rtcGetSelectedCandidatePair
    // TODO rtcGetRemoteMaxMessageSize


    public RTCDataChannel createDataChannel(String label) {
        final var dc = INSTANCE.rtcCreateDataChannel(this.peer, label);
        final var channel = new RTCDataChannel(this, dc);
        this.channels.put(dc, channel);
        return channel;
    }


    // TODO
    public RTCDataChannel createDataChannelEx(String label) {
        rtcDataChannelInit init = new rtcDataChannelInit();
        /*
rtcReliability reliability;
unordered: if true, the Data Channel will not enforce message ordering, else it will be ordered
unreliable: if true, the Data Channel will not enforce strict reliability, else it will be reliable
maxPacketLifeTime: if unreliable, time window in milliseconds during which transmissions and retransmissions may occur
maxRetransmits: if unreliable and maxPacketLifeTime is 0, maximum number of attempted retransmissions (0 means no retransmission)
protocol (optional): a user-defined UTF-8 string representing the Data Channel protocol, empty if NULL
negotiated: if true, the Data Channel is assumed to be negotiated by the user and won't be negotiated by the WebRTC layer
manualStream: if true, the Data Channel will use stream as stream ID, else an available id is automatically selected
stream: if manualStream is true, the Data Channel will use it as stream ID, else it is ignored
         */
        final var dc = INSTANCE.rtcCreateDataChannelEx(this.peer, label, init);
        final var channel = new RTCDataChannel(this, dc);
        this.channels.put(dc, channel);
        return channel;
    }


    @Override
    public void close() {

        // TODO close channels

        // Blocks until all callbacks have returned (except a callback calling this)
        final var closed = INSTANCE.rtcClosePeerConnection(this.peer);
        if (closed < 0) {
            throw new IllegalStateException("Error closing peer connection: " + closed);
        }
        final var deleted = INSTANCE.rtcDeletePeerConnection(this.peer);
        if (deleted < 0) {
            throw new IllegalStateException("Error deleting peer connection: " + deleted);
        }
        this.peer = null;
    }

    public void onLocalDescription(PeerCallbacks.LocalDescription cb) {
        INSTANCE.rtcSetLocalDescriptionCallback(this.peer, (pc, sdp, type, ptr) -> {
            cb.handleDescription(this, sdp.getString(0), type.getString(0));
        });
    }

    public void onLocalCandidate(PeerCallbacks.LocalCandidate cb) {
        INSTANCE.rtcSetLocalCandidateCallback(this.peer, (pc, cand, mid, ptr) -> {
            cb.handleCandidate(this, cand.getString(0), mid.getString(0));
        });
    }

    public void onStateChange(PeerCallbacks.StateChange cb) {
        INSTANCE.rtcSetStateChangeCallback(this.peer, (pc, state, ptr) -> {
            cb.handleChange(this, PeerState.of(state));
        });
    }

    public void onGatheringStateChange(PeerCallbacks.GatheringStateChange cb) {
        INSTANCE.rtcSetGatheringStateChangeCallback(this.peer, (pc, state, ptr) -> {
            cb.handleGatherChange(this, RTCPeerConnection.GatheringState.of(state));
        });
    }

    public void onDataChannel(PeerCallbacks.DataChannel cb) {
        INSTANCE.rtcSetDataChannelCallback(this.peer, (pc, dc, ptr) -> {
            cb.handleDC(this, channels.get(dc));
        });
    }

    // TODO track callback?

    // TODO rtcAddTrack
    // TODO rtcDeleteTrack? on Track obj
    // TODO rtcGetTrackDescription? on Track obj
    // TODO rtcGetTrackMid? on Track obj
    // TODO rtcGetTrackDirection? on Track obj
    // TODO rtcGetTrackDirection? on Track obj


    // TODO rtcCreateWebSocket
    // TODO rtcDeleteWebSocket on WebSocket obj.
    // TODO rtcGetWebSocketRemoteAddress on WebSocket obj.
    // TODO rtcGetWebSocketPath on WebSocket obj.
    // TODO rtcGetWebSocketPath on WebSocket obj.

    // TODO rtcCreateWebSocketServer
    // TODO rtcDeleteWebSocketServer on WebSocketServer obj
    // TODO rtcGetWebSocketServerPort on WebSocketServer obj


    public boolean isClosed() {
        return this.peer == null;
    }

    public interface PeerCallbacks {

        @FunctionalInterface
        interface LocalDescription {

            void handleDescription(RTCPeerConnection peer, String sdp, String type);
        }

        @FunctionalInterface
        interface LocalCandidate {

            void handleCandidate(RTCPeerConnection peer, String candidate, String mediaId);
        }

        @FunctionalInterface
        interface StateChange {

            void handleChange(RTCPeerConnection peer, PeerState state);
        }

        @FunctionalInterface
        interface GatheringStateChange {

            void handleGatherChange(RTCPeerConnection peer, RTCPeerConnection.GatheringState state);
        }

        @FunctionalInterface
        interface DataChannel {

            void handleDC(RTCPeerConnection peer, RTCDataChannel channel);
        }
    }

    public enum PeerState {
        RTC_CONNECTING(1),
        RTC_CONNECTED(2),
        RTC_DISCONNECTED(3),
        RTC_FAILED(4),
        RTC_CLOSED(5),
        ;

        private static final Map<Integer, PeerState> MAP = Main.mappedEnum(PeerState.values(), s -> s.state);

        private final int state;

        PeerState(int state) {
            this.state = state;
        }

        public static PeerState of(final int state) {
            return MAP.get(state);
        }
    }

    public enum GatheringState {
        RTC_GATHERING_INPROGRESS(1),
        RTC_GATHERING_COMPLETE(2),
        ;

        private static final Map<Integer, GatheringState> MAP = Main.mappedEnum(RTCPeerConnection.GatheringState.values(), s -> s.state);
        private final int state;

        GatheringState(int state) {
            this.state = state;
        }

        public static GatheringState of(final int state) {
            return MAP.get(state);
        }
    }

    public static class RTCConfiguration {

        /*
            iceServers (optional): an array of pointers on null-terminated ICE server URIs (NULL if unused)
            iceServersCount (optional): number of URLs in the array pointed by iceServers (0 if unused)
            proxyServer (optional): if non-NULL, specifies the proxy server URI to use for TURN relaying (ignored with libjuice as ICE backend)
            bindAddress (optional): if non-NULL, bind only to the given local address (ignored with libnice as ICE backend)
            certificateType (optional): certificate type, either RTC_CERTIFICATE_ECDSA or RTC_CERTIFICATE_RSA (0 or RTC_CERTIFICATE_DEFAULT if
            default)
            iceTransportPolicy (optional): ICE transport policy, if set to RTC_TRANSPORT_POLICY_RELAY, the PeerConnection will emit only relayed
            candidates (0 or RTC_TRANSPORT_POLICY_ALL if default)
            enableIceTcp: if true, generate TCP candidates for ICE (ignored with libjuice as ICE backend)
            enableIceUdpMux: if true, connections are multiplexed on the same UDP port (should be combined with portRangeBegin and portRangeEnd,
            ignored with libnice as ICE backend)
            disableAutoNegotiation: if true, the user is responsible for calling rtcSetLocalDescription after creating a Data Channel and after
            setting the remote description
            forceMediaTransport: if true, the connection allocates the SRTP media transport even if no tracks are present (necessary to add tracks
            during later renegotiation)
            portRangeBegin (optional): first port (included) of the allowed local port range (0 if unused)
            portRangeEnd (optional): last port (included) of the allowed local port (0 if unused)
            mtu (optional): manually set the Maximum Transfer Unit (MTU) for the connection (0 if automatic)
            maxMessageSize (optional): manually set the local maximum message size for Data Channels (0 if default)
         */
        public rtcConfiguration config = new rtcConfiguration();

        public static RTCConfiguration of(String iceServer) {
            final var cfg = new RTCConfiguration();
            cfg.config.iceServers = new PointerByReference(Main.toPointer(iceServer));
            cfg.config.forceMediaTransport = 0;
            cfg.config.iceServersCount = 1;
            cfg.config.disableAutoNegotiation = 0;
            return cfg;
        }
    }
}
