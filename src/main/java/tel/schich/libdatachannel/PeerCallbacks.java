package tel.schich.libdatachannel;

public interface PeerCallbacks {

    /**
     * Called when the local description has been set.
     */
    @FunctionalInterface
    interface LocalDescription {

        void handleDescription(RTCPeerConnection peer, String sdp, String type);
    }

    /**
     * Called when a local candidate has been added.
     */
    @FunctionalInterface
    interface LocalCandidate {

        void handleCandidate(RTCPeerConnection peer, String candidate, String mediaId);
    }

    /**
     * Called when the state of the Peer Connection has changed
     */
    @FunctionalInterface
    interface StateChange {

        void handleChange(RTCPeerConnection peer, PeerState state);
    }

    /**
     * Called when the gathering state of the Peer Connection has changed
     */
    @FunctionalInterface
    interface GatheringStateChange {

        void handleGatherChange(RTCPeerConnection peer, GatheringState state);
    }

    /**
     * Called when a new data channel has been created
     * TODO not when initiated by local
     */
    @FunctionalInterface
    interface DataChannel {

        void handleDC(RTCPeerConnection peer, RTCDataChannel channel);
    }

    /**
     * Called when a track has been added
     */
    @FunctionalInterface
    interface Track {

        void handleTrack(RTCPeerConnection peer, int track); // TODO track object?
    }
}
