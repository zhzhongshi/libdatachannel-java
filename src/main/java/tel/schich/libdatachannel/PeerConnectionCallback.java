package tel.schich.libdatachannel;

public interface PeerConnectionCallback {

    /**
     * Called when the local description has been set.
     */
    @FunctionalInterface
    interface LocalDescription {

        void handleDescription(PeerConnection peer, String sdp, String type);
    }

    /**
     * Called when a local candidate has been added.
     */
    @FunctionalInterface
    interface LocalCandidate {

        void handleCandidate(PeerConnection peer, String candidate, String mediaId);
    }

    /**
     * Called when the state of the Peer Connection has changed
     */
    @FunctionalInterface
    interface StateChange {

        void handleChange(PeerConnection peer, PeerState state);
    }

    /**
     * Called when the gathering state of the Peer Connection has changed
     */
    @FunctionalInterface
    interface GatheringStateChange {

        void handleGatherChange(PeerConnection peer, GatheringState state);
    }

    /**
     * Called when a new data channel has been created
     * TODO not when initiated by local
     */
    @FunctionalInterface
    interface DataChannel {

        void handleDC(PeerConnection peer, tel.schich.libdatachannel.DataChannel channel);
    }

    /**
     * Called when a track has been added
     */
    @FunctionalInterface
    interface Track {

        void handleTrack(PeerConnection peer, int track); // TODO track object?
    }
}
