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

    public static native int rtcCreateDataChannelEx(int peerHandle, String label, boolean unordered, boolean unreliable, long maxPacketLifeTime, int maxRetransmits, String protocol, boolean negotiated, int stream, boolean manualStream);

    public static native String rtcGetLocalAddress(int peerHandle);
    public static native String rtcGetRemoteAddress(int peerHandle);

    public static native String rtcGetTrackDescription(int trackHandle);
    public static native String rtcGetTrackMid(int trackHandle);

    public static native int rtcClose(int channelHandle);
    public static native int rtcDelete(int channelHandle);
    public static native boolean rtcIsClosed(int channelHandle);
    public static native boolean rtcIsOpen(int channelHandle);
    public static native int rtcGetAvailableAmount(int channelHandle);
    public static native int rtcGetBufferedAmount(int channelHandle);
    public static native int rtcGetDataChannelStream(int channelHandle);
    public static native String rtcGetDataChannelLabel(int channelHandle);
    public static native String rtcGetDataChannelProtocol(int channelHandle);
    public static native DataChannelReliability rtcGetDataChannelReliability(int channelHandle);
}
