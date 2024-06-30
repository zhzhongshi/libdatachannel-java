package tel.schich.libdatachannel;

import static tel.schich.libdatachannel.GatheringState.RTC_GATHERING_COMPLETE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    public static void main2(String[] args) {
        final var cfg = RTCConfiguration.of("stun.l.google.com:19302");
        // try with resources to cleanup peer when done
        try (var peer = RTCPeerConnection.createPeer(cfg)) {
            // when complete send sdp to remote peer
            peer.onGatheringStateChange((pc, state) -> {
                if (RTC_GATHERING_COMPLETE == state) {
                    final var sdp = pc.localDescription();
                    System.out.println(sdp);
                }
            });
            // create data channel
            final var channel = peer.createDataChannel("test");
            // wait for local sdp...
            // then set answer from remote peer
            peer.setAnswer(readInput());
            // register message callback (negative size indicates a null-terminated string otherwise binary data)
            channel.onMessage((c, message, size) -> System.out.println("Incoming message: " + new String(message)));
            // block until channel is closed
            CompletableFuture<Void> future = new CompletableFuture<>();
            channel.onClose(c -> future.completeAsync(() -> null));
            future.join();
        }

    }

    public record Offer(RTCDataChannel channel, String sdp) implements AutoCloseable {

        public void answer(String remoteSdp) {
            channel.peer().setAnswer(remoteSdp);
        }

        public CompletableFuture<Void> closeFuture() {
            CompletableFuture<Void> future = new CompletableFuture<>();
            this.channel.onClose(c -> future.completeAsync(() -> null));
            this.channel.peer().onStateChange((peer, state) -> {
                if (state == PeerState.RTC_CLOSED) {
                    peer.close();
                    future.completeAsync(() -> null);
                }
            });
            return future;
        }

        @Override
        public void close() {
            this.channel.peer().close();
        }

        public static CompletableFuture<Offer> create(String label, RTCConfiguration cfg) {
            var peer = RTCPeerConnection.createPeer(cfg);
            final var offer = new CompletableFuture<Offer>();
            final var channel = peer.createDataChannel(label);
            peer.onGatheringStateChange((pc, state) -> {
                System.out.println(state);
                if (state == RTC_GATHERING_COMPLETE) {
                    offer.complete(new Offer(channel, peer.localDescription()));
                }
            });
            return offer;
        }
    }

    static final String WEBSITE = "http://localhost:8080/libdatachannel-java/test.html";

    public static void main(String[] args) {
        RTCPeerConnection.initLogger(LogLevel.RTC_LOG_ERROR);
        final var cfg = RTCConfiguration.of("stun.l.google.com:19302");
        while (true) {
            try (var offer = Offer.create("test", cfg).join()) {
                offer.channel.onOpen(Main::handleOpen);
                offer.channel.onMessage(Main::handleMessage);
                offer.channel.onError(Main::handleError);
                offer.channel.peer().onStateChange(Main::handleStateChange);
                final var encoded = Base64.getEncoder().encodeToString(offer.sdp.getBytes());
                // System.out.println("SDP:\n\n" + sdp);
                System.out.println("Awaiting Answer...\n" + WEBSITE + "?sdp=" + encoded);

                String remoteSdp = Main.readCompressedSdp();
                while (remoteSdp == null) {
                    System.out.println("Invalid! Try again");
                    remoteSdp = Main.readCompressedSdp();
                }
                System.out.println("Processing Answer...");
                System.out.println("\n\n");
                System.out.println(remoteSdp);
                System.out.println("\n\n");
                offer.answer(remoteSdp);


                offer.closeFuture().join();
            }
            System.out.println("closed!");
        }

    }

    private static void handleStateChange(final RTCPeerConnection pc, final PeerState state) {
        System.out.println(state);
        if (state == PeerState.RTC_CONNECTED) {
            final var uri = pc.remoteAddress();
            System.out.println("Connected to " + uri.getHost() + ":" + uri.getPort());
        }
    }

    private static void handleError(final RTCDataChannel c, final String error) {
        System.out.println("Error: " + error);
    }

    private static void handleMessage(final RTCDataChannel c, final byte[] message, final int size) {
        if (size < 0) {
            final var msg = new String(message);
            System.out.println("In: " + msg);
            if (msg.equals("exit")) {
                c.sendMessage("Bye!");
                System.out.println("Ok we are done");
                c.close();
            } else {
                c.sendMessage("You said things...");
            }
        } else {
            c.sendMessage("What is this file?");
            System.out.println("Got a file");
        }
    }

    private static void handleOpen(final RTCDataChannel c) {
        System.out.println("Connection Open!");
        c.sendMessage("Hello There!");
        try {
            c.sendMessage(Files.readAllBytes(Path.of("img.png")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static String readCompressedSdp() {
        var remoteDescription = readInput();
        if (remoteDescription.startsWith("v=")) {
            return remoteDescription;
        }
        remoteDescription = remoteDescription.replaceAll("\n", "");
        String cValue = null;
        String appPort = null;
        String fingerprint = null;
        String iceLine = null;
        String iceFrag = null;
        List<String> candidates = new ArrayList<>();
        int nCandidate = 0;
        for (String line : remoteDescription.split("\\.")) {
            if (fingerprint == null) {
                final var hex = base64toHex(line);
                fingerprint = IntStream.range(0, hex.length() / 2).mapToObj(i -> hex.substring(i * 2, i * 2 + 2)).collect(Collectors.joining(":"))
                        .toUpperCase();
            } else if (iceLine == null) {
                //                iceLine = base64toHex(line);
                iceLine = line;
            } else if (iceFrag == null) {
                //                iceFrag = base64toHex(line);
                iceFrag = line;
            } else {
                var ipv = line.substring(0, 1);
                var type = "s".equals(line.substring(1, 2)) ? "srflx" : "host";
                final var idIdx = line.indexOf("#");
                var rawIp = line.substring(2, idIdx);
                var id = line.substring(idIdx + 1);

                var ipWithPort = switch (ipv) {
                    case "4" -> {
                        // shortSdp += '.4' + type + btoa(ip + ":" + port).replaceAll("=+$", "") + "#" + id
                        final var ipv4 = base64toString(rawIp);

                        final var ipv4Parts = ipv4.split(":");
                        yield new IpWithPort(ipv4Parts[0], ipv4Parts[1]);
                    }
                    case "6" -> {
                        // shortSdp += '.6' + type + ipv6ToBase64(ip) + ":" + port + "#" + id;
                        final var splitV6 = rawIp.split(":");
                        final var hexIpv6 = base64toHex(splitV6[0]);
                        var ipv6 = IntStream.range(0, hexIpv6.length() / 4).mapToObj(i -> hexIpv6.substring(i * 4, i * 4 + 4))
                                .collect(Collectors.joining(":"));

                        yield new IpWithPort(ipv6, splitV6[1]);
                    }
                    default -> null;
                };


                if (ipWithPort != null) {
                    if (cValue == null) {
                        cValue = (ipWithPort.ip.contains(":") ? "IP6 " : "IP4 ") + ipWithPort.ip;
                        appPort = ipWithPort.port;
                    }
                    final var s = ipWithPort.ip.contains(":") ? "::" : "0.0.0.0";
                    candidates.add(("a=candidate:%s 1 UDP %d %s %s typ %s raddr %s rport 0")
                            .formatted(id, nCandidate++, ipWithPort.ip, ipWithPort.port, type, s));
                }

            }
        }
        //        System.out.println("FP: " + fingerprint);
        //        System.out.println("ICE: " + iceLine + ":" + iceFrag);
        String cans = String.join("\n", candidates);
        System.out.println("CAN:\n" + cans);
        if (cans.isEmpty()) {
            return null;
        }

        remoteDescription = """
                v=0
                o=- 1337 0 IN IP4 0.0.0.0
                s=-
                t=0 0
                a=group:BUNDLE 0
                a=msid-semantic: WMS
                m=application %s UDP webrtc-datachannel
                a=sendrecv
                c=IN %s
                %s
                a=end-of-candidates
                a=ice-ufrag:%s
                a=ice-pwd:%s
                a=ice-options:trickle
                a=fingerprint:sha-256 %s
                a=setup:active
                a=mid:0
                a=sctp-port:5000
                a=max-message-size:262144
                """.formatted(appPort, cValue, cans, iceFrag, iceLine, fingerprint)
        ;
        return remoteDescription;
    }

    private static String base64toHex(final String line) {
        var decodedLine = Base64.getDecoder().decode(line);
        return HexFormat.of().formatHex(decodedLine);
    }

    private static String base64toString(final String line) {
        var decodedLine = Base64.getDecoder().decode(line);
        return new String(decodedLine);
    }

    public static String readInput() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder out = new StringBuilder();
        while (true) {
            try {
                String line = in.readLine();
                if (line == null || line.isEmpty()) {
                    break;
                }
                out.append(line).append("\n");
            } catch (IOException e) {
                System.err.println("Error reading input");
            }
        }
        return out.toString();
    }


    private record IpWithPort(String ip, String port) {

    }
}
