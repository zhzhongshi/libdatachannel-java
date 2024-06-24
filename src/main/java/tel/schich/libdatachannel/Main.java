package tel.schich.libdatachannel;

import static generated.DatachannelLibrary.INSTANCE;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import generated.rtcConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    private static final Executor executor = Executors.newWorkStealingPool();
    private static final AtomicBoolean stopping = new AtomicBoolean(false);
    private static final AtomicBoolean connected = new AtomicBoolean(false);

    private static final String WEBSITE = "http://localhost:8080/libdatachannel-java/test.html";

    private static class Connection {

        public Integer peer;
        public Integer channel;
    }

    public static final Connection CONN = new Connection();


    public static void main(String[] args) throws Exception {
        registerShutdownHook();

        initLogger();

        var pcHandle = createPeer();
        CONN.peer = pcHandle;

        Integer dcHandle = null;
        while (!stopping.get()) {
            if (connected.get()) {
                System.out.print("z");
                Thread.sleep(1000);
            } else {
                if (dcHandle != null) {
                    pcHandle = recreatePeer(dcHandle, pcHandle);
                    CONN.peer = pcHandle;
                    CONN.channel = null;
                    dcHandle = null;
                } else {
                    System.out.println("Create Channel...");
                    dcHandle = createChannel(pcHandle, Main::handleMessage).get();
                    CONN.channel = dcHandle;
                    System.out.println("Created Channel! " + dcHandle);
                }
            }
        }
    }

    private static int recreatePeer(final Integer dcHandle, int pcHandle) {
        System.out.println("Recreating Peer... ");
        System.out.print("Channel: close... ");
        INSTANCE.rtcClose(dcHandle);
        System.out.print("delete... ");
        INSTANCE.rtcDeleteDataChannel(dcHandle);
        System.out.println("deleted!");
        System.out.print("Peer:    close... ");
        INSTANCE.rtcClosePeerConnection(pcHandle);
        System.out.print("delete... ");
        INSTANCE.rtcDeletePeerConnection(pcHandle);
        System.out.println("deleted!");
        INSTANCE.rtcCleanup();
        System.out.println("cleaned");
        pcHandle = createPeer();
        return pcHandle;
    }

    interface MessageHandler {

        void handle(int channel, String message);
    }

    private static CompletableFuture<Integer> createChannel(int pcHandle, MessageHandler messageHandler) {

        CompletableFuture<Integer> promise = new CompletableFuture<>();
        var t = new Thread(() -> {
            AtomicReference<String> localDescription = new AtomicReference<>();
            INSTANCE.rtcSetLocalDescriptionCallback(pcHandle, (int pc, Pointer sdp, Pointer type, Pointer ptr) -> {
                localDescription.set(sdp.getString(0));
                System.out.println(" - Got Local Description:");
            });

            final CopyOnWriteArrayList<String> localCandidates = new CopyOnWriteArrayList<>();
            INSTANCE.rtcSetLocalCandidateCallback(pcHandle, (int pc, Pointer cand, Pointer mid, Pointer ptr) -> {
                final String candidate = cand.getString(0);
                System.out.println(" - local candidate: " + candidate);
                localCandidates.add(candidate);
            });

            INSTANCE.rtcSetStateChangeCallback(pcHandle, (pc, state, ptr) -> {
                System.out.println(" - State Change: " + state); // RTC_CONNECTING, RTC_CONNECTED, RTC_DISCONNECTED, RTC_FAILED, or RTC_CLOSED.
                if (state == 5) {
                    if (!promise.isDone()) {
                        connected.set(false);
                        promise.completeAsync(() -> null);

                    }
                }
            });

            INSTANCE.rtcSetGatheringStateChangeCallback(pcHandle, (pc, state, ptr) -> {
                // RTC_GATHERING_NEW = 0, RTC_GATHERING_INPROGRESS = 1, RTC_GATHERING_COMPLETE = 2
                System.out.println(" - ICE Gathering State: " + state);
            });

            System.out.println(" - creating channel...");
            int dcHandle = INSTANCE.rtcCreateDataChannel(pcHandle, "test");
            System.out.println(" - channel created id: " + dcHandle);

            INSTANCE.rtcSetOpenCallback(dcHandle, (id, ptr) -> {
                connected.set(true);
                INSTANCE.rtcSendMessage(id, "Hello from the other side", -1);
                System.out.println(" - Channel open! " + id);
                if (!promise.isDone()) {
                    promise.completeAsync(() -> dcHandle, executor);
                }
            });
            INSTANCE.rtcSetClosedCallback(dcHandle, (id, ptr) -> {
                connected.set(false);
                System.out.println(" - Channel closed!");
                if (!promise.isDone()) {
                    connected.set(false);
                    promise.completeAsync(() -> null, executor);
                }
            });

            System.out.println(" - register msg callback for channel " + dcHandle);
            INSTANCE.rtcSetMessageCallback(dcHandle, (id, message, size, ptr) -> messageHandler.handle(id, message.getString(0)));
            INSTANCE.rtcSetErrorCallback(dcHandle, (id, error, ptr) -> {
                System.out.println(" - Error: " + error.getString(0));
            });

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            final var sdp = assembleCompleteSdp(localDescription.get(), localCandidates);
            var encoded = Base64.getEncoder().encodeToString(sdp.getBytes());
            System.out.println("SDP:\n\n" + sdp);
            System.out.println("Awaiting RemoteDescription...");
            System.out.println(WEBSITE + "?sdp=" + encoded);

            INSTANCE.rtcSetIceStateChangeCallback(pcHandle, (pc, state, ptr) -> {
                // RTC_ICE_NEW = 0, RTC_ICE_CHECKING = 1, RTC_ICE_CONNECTED = 2, RTC_ICE_COMPLETED = 3, RTC_ICE_FAILED = 4, RTC_ICE_DISCONNECTED =
                // 5, RTC_ICE_CLOSED = 6
                System.out.println(" - ICE state: " + state);
                if (state == 6) {
                    if (!promise.isDone()) {
                        connected.set(false);
                        promise.completeAsync(() -> null);

                    }
                }
            });


            var remoteDescription = readInput();
            remoteDescription = readCompressed(remoteDescription);
            System.out.println("Processing Answer...\n" + remoteDescription);


            final var code = INSTANCE.rtcSetRemoteDescription(pcHandle, remoteDescription, "answer");
            System.out.println("setRemoteDesc returned " + code);
            if (code != 0) {
                if (!promise.isDone()) {
                    connected.set(false);
                    promise.completeAsync(() -> null);
                }
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (!promise.isDone()) {
                    promise.completeAsync(() -> dcHandle);
                }
            }
        });

        t.setDaemon(true);
        t.start();

        return promise;
    }

    private record IpWithPort(String ip, String port) {

    }

    private static String readCompressed(String remoteDescription) {
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

    private static int createPeer() {
        System.out.print("Create Peer... ");
        final rtcConfiguration config = new rtcConfiguration();
        config.iceServers = new PointerByReference(toPointer("stun.l.google.com:19302"));
        config.forceMediaTransport = 0;
        config.iceServersCount = 1;
        config.disableAutoNegotiation = 0;
        final int pcHandle = INSTANCE.rtcCreatePeerConnection(config);
        System.out.println("created!");
        return pcHandle;
    }

    private static void initLogger() {
        INSTANCE.rtcInitLogger(3, (level, message) -> {
            System.out.println(message.getString(0));
        });
    }

    private static void handleMessage(final int id, final String message) {
        INSTANCE.rtcSendMessage(id, "Random: " + (int) (Math.random() * 100), -1);
        System.out.println("IN: " + message);
    }

    private static String assembleCompleteSdp(String localDesc, Collection<String> candidates) {
        var candidateAttributes = candidates.stream().map(c -> "a=" + c + "\r\n").collect(Collectors.joining());
        if (localDesc == null) {
            return candidateAttributes;
        }
        return localDesc + candidateAttributes;
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

    private static Pointer toPointer(String uri) {
        final byte[] bytes = Native.toByteArray(uri);
        final Memory memory = new Memory(bytes.length + 1);
        memory.write(0, bytes, 0, bytes.length);
        memory.setByte(bytes.length, (byte) 0);
        return memory;
    }

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stopping.set(true);
            System.out.print("##### Shutting down... ");
            if (CONN.channel != null) {
                System.out.print("channels... ");
                INSTANCE.rtcClose(CONN.channel);
                INSTANCE.rtcDeleteDataChannel(CONN.channel);
            }
            if (CONN.peer != null) {
                System.out.print("peer... ");
                INSTANCE.rtcClosePeerConnection(CONN.peer);
                INSTANCE.rtcDeletePeerConnection(CONN.peer);
            }
            INSTANCE.rtcCleanup();
            System.out.println(" done!");
        }));
    }
}
