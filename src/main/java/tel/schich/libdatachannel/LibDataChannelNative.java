package tel.schich.libdatachannel;

import java.nio.ByteBuffer;

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
    public static native int rtcAddRemoteCandidate(int peerHandle, String candidate, String mediaId);
    public static native String rtcGetLocalAddress(int peerHandle);
    public static native String rtcGetRemoteAddress(int peerHandle);
    public static native CandidatePair rtcGetSelectedCandidatePair(int peerHandle);

    public static native int rtcAddTrack(int peerHandle, String sdp);
    public static native int rtcAddTrackEx(int peerHandle, int direction, int codec);
    public static native String rtcGetTrackDescription(int trackHandle);
    public static native int rtcGetTrackDirection(int trackHandle);
    public static native String rtcGetTrackMid(int trackHandle);
    public static native int rtcDeleteTrack(int trackHandle);

    public static native int rtcGetMaxDataChannelStream(int peerHandle);
    public static native int rtcGetRemoteMaxMessageSize(int peerHandle);
    public static native int rtcCreateDataChannelEx(int peerHandle, String label, boolean unordered, boolean unreliable, long maxPacketLifeTime, int maxRetransmits, String protocol, boolean negotiated, int stream, boolean manualStream);
    public static native int rtcClose(int channelHandle);
    public static native int rtcDelete(int channelHandle);
    public static native boolean rtcIsClosed(int channelHandle);
    public static native boolean rtcIsOpen(int channelHandle);
    public static native int rtcMaxMessageSize(int channelHandle);
    public static native int rtcSetBufferedAmountLowThreshold(int channelHandle, int amount);
    public static native int rtcSendMessage(int channelHandle, ByteBuffer data, int offset, int length);
    public static native ByteBuffer rtcReceiveMessage(int channelHandle);
    public static native int rtcReceiveMessageInto(int channelHandle, ByteBuffer buffer, int offset, int capacity);
    public static native int rtcGetAvailableAmount(int channelHandle);
    public static native int rtcGetBufferedAmount(int channelHandle);
    public static native int rtcGetDataChannelStream(int channelHandle);
    public static native String rtcGetDataChannelLabel(int channelHandle);
    public static native String rtcGetDataChannelProtocol(int channelHandle);
    public static native DataChannelReliability rtcGetDataChannelReliability(int channelHandle);
}
