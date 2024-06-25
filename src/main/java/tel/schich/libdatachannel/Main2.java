package tel.schich.libdatachannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main2 {

    static final String WEBSITE = "http://localhost:8080/libdatachannel-java/test.html";

    public static void main(String[] args) {
        LibDataChannel.setLogLevel(LibDataChannel.LogLevel.RTC_LOG_WARNING);
        final var cfg = RTCPeerConnection.RTCConfiguration.of("stun.l.google.com:19302");
        try (var pc = RTCPeerConnection.createPeer(cfg)) {
            final var localSdp = new LocalSdpAssembler();
            pc.onLocalDescription(localSdp);
            pc.onLocalCandidate(localSdp);

            final var channel = pc.createDataChannel("test");
            channel.onOpen(c -> {
                System.out.println("Connection Open!");
                c.sendMessage("Hello There!");
            });
            channel.onMessage((c, message) -> {
                System.out.println("In: " + message);
                c.sendMessage("You said things...");
            });

            CompletableFuture<String> future = new CompletableFuture<>();
            channel.onClose(c -> future.completeAsync(() -> "closed!!"));

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            final var sdp = localSdp.assemble();
            var encoded = Base64.getEncoder().encodeToString(sdp.getBytes());

            System.out.println("SDP:\n\n" + sdp);
            System.out.println("Awaiting RemoteDescription...");
            System.out.println(WEBSITE + "?sdp=" + encoded);

            var remoteDescription = readInput();
            remoteDescription = Main2.readCompressed(remoteDescription);
            System.out.println("Processing Answer...\n" + remoteDescription);

            pc.setAnswer(remoteDescription);

            System.out.println(future.join());
        }

    }

    static String readCompressed(String remoteDescription) {
        if (remoteDescription.startsWith("v=")) {
            return remoteDescription;
        }
        String cValue = null;
        String port = null;
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
                iceLine = base64toHex(line);
                ;
            } else if (iceFrag == null) {
                iceFrag = base64toHex(line);
            } else {
                var ipv = line.substring(0, 1);
                var type = "s".equals(line.substring(1, 2)) ? "srflx" : "host";
                final var idIdx = line.indexOf("#");
                var rawIp = line.substring(2, idIdx);
                var id = line.substring(idIdx + 1, idIdx + 2);

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
                        cValue = ipWithPort.ip;
                        port = ipWithPort.port;
                    }
                    candidates.add("a=candidate:%s 1 UDP %d %s %s typ %s raddr 0.0.0.0 rport 0"
                            .formatted(id, nCandidate++, ipWithPort.ip, ipWithPort.port, type));
                }

            }
        }
        System.out.println("FP: " + fingerprint);
        System.out.println("ICE: " + iceLine + ":" + iceFrag);
        System.out.println("CAN: " + candidates);
        // id, prio, ip, port, type
        var candidatePattern = Pattern.compile("([0-9]+)#(.+)#([0-9]+)#(.+)");
        AtomicInteger n = new AtomicInteger(0);
        String cans = candidates.stream().map(candidate -> {
            final var matcher = candidatePattern.matcher(candidate);
            if (matcher.find()) {
                return "a=candidate:%s 1 UDP %d %s %s typ %s raddr 0.0.0.0 rport 0".formatted(matcher.group(1), n.getAndIncrement(), matcher.group(2),
                        matcher.group(3), matcher.group(4));
            }
            return "";
        }).filter(String::isBlank).collect(Collectors.joining("\n"));

        remoteDescription = """
                v=0
                o=mozilla...THIS_IS_SDPARTA-99.0 1707886350958927893 0 IN IP4 0.0.0.0
                s=-
                t=0 0
                a=sendrecv
                a=fingerprint:sha-256 %s
                c=IN IP4 %s
                %s
                a=ice-pwd:%s
                a=ice-ufrag:%s
                m=application %s UDP/DTLS/SCTP webrtc-datachannel
                a=setup:active
                """.formatted(fingerprint, cValue, cans, iceLine, iceFrag, port)
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
                break;
            } catch (IOException e) {
                System.err.println("Error reading input");
            }
        }
        return out.toString();
    }

    static String assembleCompleteSdp(String localDesc, Collection<String> candidates) {
        var candidateAttributes = candidates.stream().map(c -> "a=" + c + "\r\n").collect(Collectors.joining());
        if (localDesc == null) {
            return candidateAttributes;
        }
        return localDesc + candidateAttributes;
    }

    public static class LocalSdpAssembler implements RTCPeerConnection.PeerCallbacks.LocalCandidate,
            RTCPeerConnection.PeerCallbacks.LocalDescription {

        public String local;
        public List<String> candidates = new ArrayList<>();

        @Override
        public void handleDescription(final RTCPeerConnection peer, final String sdp, final String type) {
                this.local = sdp;
        }

        @Override
        public void handleCandidate(final RTCPeerConnection peer, final String candidate, final String mediaId) {
            this.candidates.add(candidate);
        }

        public String assemble() {
            var candidateAttributes = candidates.stream().map(c -> "a=" + c + "\r\n").collect(Collectors.joining());
            if (local == null) {
                return candidateAttributes;
            }
            return local + candidateAttributes;
        }
    }

    private record IpWithPort(String ip, String port) {

    }
}
