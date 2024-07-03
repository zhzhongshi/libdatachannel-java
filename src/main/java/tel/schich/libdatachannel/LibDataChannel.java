package tel.schich.libdatachannel;

public class LibDataChannel {
    private static volatile boolean initialized = false;

    public static final String LIB_NAME = "libdatachannel-java";

    /**
     * Initializes the library by loading the native library.
     */
    public synchronized static void initialize() {
        if (initialized) {
            return;
        }

        Platform.loadNativeLibrary(LIB_NAME, LibDataChannel.class);

        initialized = true;
    }
}
