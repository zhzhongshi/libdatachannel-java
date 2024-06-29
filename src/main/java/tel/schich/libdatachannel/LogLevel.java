package tel.schich.libdatachannel;

public enum LogLevel {
    RTC_LOG_FATAL(1),
    RTC_LOG_ERROR(2),
    RTC_LOG_WARNING(3),
    RTC_LOG_INFO(4),
    RTC_LOG_DEBUG(5),
    RTC_LOG_VERBOSE(6),
    ;

    final int level;

    LogLevel(int level) {
        this.level = level;
    }
}
