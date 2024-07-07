package tel.schich.libdatachannel;

import java.util.Map;

/**
 * The gathering state of a {@link PeerConnection}.
 */
public enum GatheringState {
    RTC_GATHERING_INPROGRESS(1),
    RTC_GATHERING_COMPLETE(2),
    ;

    private static final Map<Integer, GatheringState> MAP = Util.mappedEnum(GatheringState.values(), s -> s.state);
    final int state;

    GatheringState(int state) {
        this.state = state;
    }

    static GatheringState of(final int state) {
        return MAP.get(state);
    }
}
