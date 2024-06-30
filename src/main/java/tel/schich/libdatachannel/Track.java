package tel.schich.libdatachannel;

import static generated.DatachannelLibrary.INSTANCE;

import tel.schich.libdatachannel.util.JNAUtil;
import tel.schich.libdatachannel.util.Util;

import java.nio.IntBuffer;


// Tracks are not implemented yet!
@Deprecated
public class Track implements AutoCloseable {

    private final Integer track;
    private final DataChannel channel;

    public Track(final Integer track, final DataChannel channel) {
        this.track = track;
        this.channel = channel;
        throw new UnsupportedOperationException("Not fully implemented yet!");
    }


    // After this function has been called, tr must not be used in a function call anymore. This function will block until all scheduled callbacks
    // of tr return (except the one this function might be called in) and no other callback will be called for tr after it returns.
    public String description() {
        return JNAUtil.readStringWithBuffer(((buff, size) -> INSTANCE.rtcGetTrackDescription(this.track, buff, size)));
    }

    // Retrieves the mid (media indentifier) of a Track.
    public String mid() {
        return JNAUtil.readStringWithBuffer(((buff, size) -> INSTANCE.rtcGetTrackMid(this.track, buff, size)));
    }

    // Retrieves the direction of a Track.
    public TrackDirection direction() {
        final var buffer = IntBuffer.allocate(1);
        Util.wrapError(INSTANCE.rtcGetTrackDirection(this.track, buffer));
        final var direction = buffer.get();
        // TODO implement me
        return null;
    }

    @Override
    public void close() {
        Util.wrapError(INSTANCE.rtcDeleteTrack(this.track));
    }

    public enum TrackDirection {
        RTC_DIRECTION_SENDONLY, RTC_DIRECTION_RECVONLY, RTC_DIRECTION_SENDRECV, RTC_DIRECTION_INACTIVE, RTC_DIRECTION_UNKNOWN;
    }
}
