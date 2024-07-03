package tel.schich.libdatachannel;

public class LibDataChannelNative {
    static {
        LibDataChannel.initialize();
    }
    public static native int newRtcPeerConnection();
}
