package tel.schich.libdatachannel;

import tel.schich.jniaccess.JNIAccess;

import java.net.InetSocketAddress;

import static tel.schich.libdatachannel.Util.parseAddress;

public class CandidatePair {
    private final InetSocketAddress local;
    private final InetSocketAddress remote;

    public CandidatePair(InetSocketAddress local, InetSocketAddress remote) {
        this.local = local;
        this.remote = remote;
    }

    public InetSocketAddress local() {
        return local;
    }

    public InetSocketAddress remote() {
        return remote;
    }

    @JNIAccess
    static CandidatePair parse(String local, String remote) {
        return new CandidatePair(parseAddress(local), parseAddress(remote));
    }
}
