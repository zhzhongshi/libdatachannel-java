package tel.schich.libdatachannel;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import generated.rtcConfiguration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static generated.DatachannelLibrary.INSTANCE;

public class Main {
    private static final Executor executor = Executors.newWorkStealingPool();
    private static final AtomicBoolean stopping = new AtomicBoolean(false);

    private static String assembleCompleteSdp(String localDesc, Collection<String> candidates) {
        var candidateAttributes = candidates.stream().map(c -> "a=" + c + "\r\n").collect(Collectors.joining());
        if (localDesc == null) {
            return candidateAttributes;
        }
        return localDesc + candidateAttributes;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Thread ID: " + Thread.currentThread().threadId());

        int dcHandle = extracted().get();
        while (!stopping.get()) {
            if (INSTANCE.rtcIsOpen(dcHandle) != 0) {
                System.out.println("Buffered amount: " + INSTANCE.rtcGetBufferedAmount(dcHandle));
                String message = "Hallo Welt! Random: " + Math.random();
                int result = INSTANCE.rtcSendMessage(dcHandle, message, -1);
                System.out.println("Sent: " + message + " (" + result + ")");
            } else {
                System.out.println("Not open anymore!");
            }
            Thread.sleep(1000);
            INSTANCE.rtcCleanup();
            return;
        }
    }

    private static CompletableFuture<Integer> extracted() {

        CompletableFuture<Integer> promise = new CompletableFuture<>();
        var t = new Thread(() -> {
            INSTANCE.rtcInitLogger(6, (level, message) -> {
                System.out.println(message.getString(0));
            });
//            INSTANCE.rtcPreload();

            final rtcConfiguration config = new rtcConfiguration();
            config.iceServers = new PointerByReference(toPointer("stun:stun.l.google.com:19302"));
            config.forceMediaTransport = 0;
            config.iceServersCount = 1;
            config.disableAutoNegotiation = 0;
            final int pcHandle = INSTANCE.rtcCreatePeerConnection(config);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                stopping.set(true);
                System.out.println("##### Shutting down...");
                INSTANCE.rtcCleanup();
                System.out.println("##### Shutting down done");
            }));

            INSTANCE.rtcSetIceStateChangeCallback(pcHandle, (pc, state, ptr) -> {
                /*
                 * typedef enum {
                 *   RTC_ICE_NEW = 0,
                 *   RTC_ICE_CHECKING = 1,
                 *   RTC_ICE_CONNECTED = 2,
                 *   RTC_ICE_COMPLETED = 3,
                 *   RTC_ICE_FAILED = 4,
                 *   RTC_ICE_DISCONNECTED = 5,
                 *   RTC_ICE_CLOSED = 6
                 * } rtcIceState;
                 */
                System.out.println("########### ICE state: " + state);
            });
            INSTANCE.rtcSetGatheringStateChangeCallback(pcHandle, (pc, state, ptr) -> {
            /*
                RTC_GATHERING_NEW = 0,
                RTC_GATHERING_INPROGRESS = 1,
                RTC_GATHERING_COMPLETE = 2
             */
                System.out.println("########### ICE Gathering State: " + state);
            });

            AtomicReference<String> localDescription = new AtomicReference<>();
            INSTANCE.rtcSetLocalDescriptionCallback(pcHandle, (int pc, Pointer sdp, Pointer type, Pointer ptr) -> {
                localDescription.set(sdp.getString(0));
                System.out.println("Local Description:\n" + localDescription.get());
            });

            final CopyOnWriteArrayList<String> localCandidates = new CopyOnWriteArrayList<>();
            INSTANCE.rtcSetLocalCandidateCallback(pcHandle, (int pc, Pointer cand, Pointer mid, Pointer ptr) -> {
                final String candidate = cand.getString(0);
                System.out.println("########### local candidate: " + candidate);
                localCandidates.add(candidate);

                System.out.println("Current SDP:\n" + assembleCompleteSdp(localDescription.get(), localCandidates));
            });


            int dcHandle = INSTANCE.rtcCreateDataChannel(pcHandle, "test");
            INSTANCE.rtcSetErrorCallback(dcHandle, (id, error, ptr) -> {
                System.out.println("Error: " + error.getString(0));
            });
            String remoteDescription = null;
            try {
                remoteDescription = new String(System.in.readAllBytes(), StandardCharsets.US_ASCII);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            INSTANCE.rtcSetOpenCallback(dcHandle, (id, ptr) -> {
                System.out.println("######### Channel open!");
            });
            INSTANCE.rtcSetMessageCallback(dcHandle, (id, message, size, ptr) -> {
                System.out.println("##### Message: " + message.getString(0));
            });
            INSTANCE.rtcSetRemoteDescription(pcHandle, remoteDescription, "answer");

            promise.completeAsync(() -> dcHandle, executor);
        });

        t.setDaemon(true);
        t.start();

        return promise;
    }

    private static Pointer toPointer(String uri) {
        final byte[] bytes = Native.toByteArray(uri);
        final Memory memory = new Memory(bytes.length + 1);
        memory.write(0, bytes, 0, bytes.length);
        memory.setByte(bytes.length, (byte) 0);
        return memory;
    }
}
