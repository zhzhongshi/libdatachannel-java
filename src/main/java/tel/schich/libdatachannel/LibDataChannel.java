package tel.schich.libdatachannel;

import static generated.DatachannelLibrary.INSTANCE;

public class LibDataChannel {

    public enum LogLevel {
        RTC_LOG_FATAL(1),
        RTC_LOG_ERROR(2),
        RTC_LOG_WARNING(3),
        RTC_LOG_INFO(4),
        RTC_LOG_DEBUG(5),
        RTC_LOG_VERBOSE(6),
        ;

        private final int level;

        LogLevel(int level) {
            this.level = level;
        }
    }

    public static void setLogLevel(LogLevel level) {
        // TODO slf4j?
        INSTANCE.rtcInitLogger(level.level, (lvl, msg) -> System.out.println(lvl + ": " + msg));
    }

    public static void preload() {
        INSTANCE.rtcPreload();
    }

    public static void cleanup() {
        // TODO must never call from callback
        INSTANCE.rtcCleanup();
    }


    // TODO do we expose this? java has websockets

    // TODO rtcCreateWebSocket
    // TODO rtcDeleteWebSocket on WebSocket obj.
    // TODO rtcGetWebSocketRemoteAddress on WebSocket obj.
    // TODO rtcGetWebSocketPath on WebSocket obj.
    // TODO rtcGetWebSocketPath on WebSocket obj.

    // TODO rtcCreateWebSocketServer
    // TODO rtcDeleteWebSocketServer on WebSocketServer obj
    // TODO rtcGetWebSocketServerPort on WebSocketServer obj
}
