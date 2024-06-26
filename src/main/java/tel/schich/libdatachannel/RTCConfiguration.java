package tel.schich.libdatachannel;

import com.sun.jna.ptr.PointerByReference;
import generated.rtcConfiguration;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RTCConfiguration {

    rtcConfiguration innerCfg = new rtcConfiguration();


    /**
     * (optional): an array of pointers on null-terminated ICE server URIs (NULL if unused)
     *
     * Each entry in iceServers must match the format [("stun"|"turn"|"turns") (":"|"://")][username ":" password "@"]hostname[":" port]["?transport=" ("udp"|"tcp"|"tls")]. The default scheme is STUN, the default port is 3478 (5349 over TLS), and the default transport is UDP. For instance, a STUN server URI could be mystunserver.org, and a TURN server URI could be turn:myuser:12345678@turnserver.org. Note transports TCP and TLS are only available for a TURN server with libnice as ICE backend and govern only the TURN control connection, meaning relaying is always performed over UDP.
     */
    RTCConfiguration iceServers(String iceServce) {
        // TODO multiple servers?
        this.innerCfg.iceServers = new PointerByReference(JNAUtil.toPointer(iceServce));
        this.innerCfg.iceServersCount = 1;
        return this;
    }

    List<String> iceServers() {
        if (this.innerCfg.iceServersCount == 1) {
            final var server = this.innerCfg.iceServers.getValue().getString(0);
        } else {
            return List.of();
        }
        // TODO multiple servers?
        throw new UnsupportedOperationException("TODO multiple servers");
    }

    /**
     * (optional): number of URLs in the array pointed by iceServers (0 if unused)
     */
    int iceServersCount() {
        return this.innerCfg.iceServersCount;
    }

    /**
     * (optional): if non-NULL, specifies the proxy server URI to use for TURN relaying (ignored with libjuice as ICE backend)
     * TODO docs
     * The proxyServer URI, if present, must match the format [("http"|"socks5") (":"|"://")][username ":" password "@"]hostname["    :" port]. The default scheme is HTTP, and the default port is 3128 for HTTP or 1080 for SOCKS5.
     *
     * If the username or password of an URI contains reserved special characters, they must be percent-encoded. In particular, ":" must be encoded as "%3A" and "@" must by encoded as "%40".
     */
    RTCConfiguration proxyServer(URI proxy) {
        this.innerCfg.proxyServer = JNAUtil.toPointer(proxy.toString());
        return this;
    }

    Optional<URI> proxyServer() {
        try {
            // TODO check if pointer to null?
            if (this.innerCfg.proxyServer == null) {
                return Optional.empty();
            }
            return Optional.of(new URI(this.innerCfg.proxyServer.getString(0)));
        } catch (URISyntaxException e) {
            return Optional.empty();

        }
    }

    /**
     * (optional): if non-NULL, bind only to the given local address (ignored with libnice as ICE backend)
     */
    RTCConfiguration bindAddress(URI bindAdress) {
        this.innerCfg.bindAddress = JNAUtil.toPointer(bindAdress.toString());
        return this;
    }

    Optional<URI> bindAddress() {
        try {
            // TODO check if pointer to null?
            if (this.innerCfg.proxyServer == null) {
                return Optional.empty();
            }
            return Optional.of(new URI(this.innerCfg.proxyServer.getString(0)));
        } catch (URISyntaxException e) {
            return Optional.empty();

        }
    }

    public enum CertificateType {
        RTC_CERTIFICATE_DEFAULT(0), // ECDSA
        RTC_CERTIFICATE_ECDSA(1),
        RTC_CERTIFICATE_RSA(2);

        private static final Map<Integer, CertificateType> MAP = Util.mappedEnum(CertificateType.values(), s -> s.state);
        private final int state;

        CertificateType(int state) {
            this.state = state;
        }

        public static CertificateType of(final int state) {
            return MAP.get(state);
        }

    }

    /**
     * (optional): certificate type, either RTC_CERTIFICATE_ECDSA or RTC_CERTIFICATE_RSA (0 or RTC_CERTIFICATE_DEFAULT if default)
     */
    RTCConfiguration certificateType(CertificateType certificateType) {
        this.innerCfg.certificateType = certificateType.state;
        return this;
    }

    CertificateType certificateType() {
        return CertificateType.of(this.innerCfg.certificateType);
    }

    public enum IceTransportPolicy {
        RTC_TRANSPORT_POLICY_ALL(0),
        RTC_TRANSPORT_POLICY_RELAY(1),
        ;
        private static final Map<Integer, IceTransportPolicy> MAP = Util.mappedEnum(IceTransportPolicy.values(), s -> s.state);
        private final int state;

        IceTransportPolicy(int state) {
            this.state = state;
        }

        public static IceTransportPolicy of(final int state) {
            return MAP.get(state);
        }

    }

    /**
     * (optional): ICE transport policy, if set to RTC_TRANSPORT_POLICY_RELAY, the PeerConnection will emit only relayed candidates (0 or
     * RTC_TRANSPORT_POLICY_ALL if default)
     */
    RTCConfiguration iceTransportPolicy(IceTransportPolicy policy) {
        this.innerCfg.iceTransportPolicy = policy.state;
        return this;
    }

    IceTransportPolicy iceTransportPolicy() {
        return IceTransportPolicy.of(this.innerCfg.iceTransportPolicy);
    }

    /**
     * if true, generate TCP candidates for ICE (ignored with libjuice as ICE backend)
     */
    RTCConfiguration enableIceTcp(boolean enableIceTcp) {
        this.innerCfg.enableIceTcp = (byte) (enableIceTcp ? 1 : 0);
        return this;
    }

    boolean enableIceTcp() {
        return this.innerCfg.enableIceTcp == 1;
    }

    /**
     * if true, connections are multiplexed on the same UDP port (should be combined with portRangeBegin and portRangeEnd, ignored with libnice as ICE
     * backend)
     */
    RTCConfiguration enableIceUdpMux(boolean enableIceUdpMux) {
        this.innerCfg.enableIceUdpMux = (byte) (enableIceUdpMux ? 1 : 0);
        return this;
    }

    boolean enableIceUdpMux() {
        return this.innerCfg.enableIceUdpMux == 1;
    }

    /**
     * if true, the user is responsible for calling rtcSetLocalDescription after creating a Data Channel and after setting the remote description
     */
    RTCConfiguration disableAutoNegotiation(boolean disableAutoNegotiation) {
        this.innerCfg.enableIceUdpMux = (byte) (disableAutoNegotiation ? 1 : 0);
        return this;
    }

    boolean disableAutoNegotiation() {
        return this.innerCfg.disableAutoNegotiation == 1;
    }

    /**
     * if true, the connection allocates the SRTP media transport even if no tracks are present (necessary to add tracks during later renegotiation)
     */
    RTCConfiguration forceMediaTransport(boolean forceMediaTransport) {
        this.innerCfg.forceMediaTransport = (byte) (forceMediaTransport ? 1 : 0);
        return this;
    }

    boolean forceMediaTransport() {
        return this.innerCfg.forceMediaTransport == 1;
    }


    /**
     * (optional): first port (included) of the allowed local port range (0 if unused)
     */
    RTCConfiguration portRangeBegin(short portRangeBegin) {
        this.innerCfg.portRangeBegin = portRangeBegin;
        return this;
    }

    short portRangeBegin() {
        return this.innerCfg.portRangeBegin;
    }


    /**
     * (optional): last port (included) of the allowed local port (0 if unused)
     */
    RTCConfiguration portRangeEnd(short portRangeEnd) {
        this.innerCfg.portRangeEnd = portRangeEnd;
        return this;
    }

    short portRangeEnd() {
        return this.innerCfg.portRangeEnd;
    }

    /**
     * (optional): manually set the Maximum Transfer Unit (MTU) for the connection (0 if automatic)
     */
    RTCConfiguration mtu(int mtu) {
        this.innerCfg.mtu = mtu;
        return this;
    }

    RTCConfiguration autoMtu() {
        this.innerCfg.mtu = 0;
        return this;
    }

    int mtu() {
        return this.innerCfg.mtu;
    }

    /**
     * (optional): manually set the local maximum message size for Data Channels (0 if default)
     */
    RTCConfiguration maxMessageSize(int maxMessageSize) {
        this.innerCfg.maxMessageSize = maxMessageSize;
        return this;
    }

    int maxMessageSize() {
        return this.innerCfg.maxMessageSize;
    }


    public static RTCConfiguration of(String iceServer) {
        final var cfg = new RTCConfiguration();
        cfg.innerCfg.iceServers = new PointerByReference(JNAUtil.toPointer(iceServer));
        cfg.innerCfg.forceMediaTransport = 0;
        cfg.innerCfg.iceServersCount = 1;
        cfg.innerCfg.disableAutoNegotiation = 0;
        return cfg;
    }
}
