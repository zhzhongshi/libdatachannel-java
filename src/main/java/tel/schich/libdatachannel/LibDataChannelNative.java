package tel.schich.libdatachannel;

public class LibDataChannelNative {
    static {
        LibDataChannel.initialize();
    }
    public static native int rtcCreatePeerConnection(byte[] iceServers, String proxyServer, String bindAddress, int certificateType, int iceTransportPolicy, boolean enableIceTcp, boolean enableIceUdpMux, boolean disableAutoNegotiation, boolean forceMediaTransport, short portRangeBegin, short portRangeEnd, int mtu, int maxMessageSize);
    public static native int rtcClosePeerConnection(int peerHandle);
    public static native int rtcDeletePeerConnection(int peerHandle);

    public static native int rtcSetLocalDescription(int peerHandle, String type);
    public static native String rtcGetLocalDescription(int peerHandle);
    public static native String rtcGetLocalDescriptionType(int peerHandle);
    public static native int rtcSetRemoteDescription(int peerHandle, String sdp, String type);
    public static native String rtcGetRemoteDescription(int peerHandle);
    public static native String rtcGetRemoteDescriptionType(int peerHandle);

    public static native String rtcGetLocalAddress(int peerHandle);
    public static native String rtcGetRemoteAddress(int peerHandle);
}
