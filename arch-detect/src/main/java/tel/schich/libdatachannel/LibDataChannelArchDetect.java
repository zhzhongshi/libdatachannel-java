package tel.schich.libdatachannel;

public class LibDataChannelArchDetect {
    public static void initialize() {
        System.setProperty(Platform.classPathPropertyNameForLibrary(LibDataChannel.LIB_NAME), "/" + Platform.detectCpuArch() + "/native/" + Platform.libraryFilename(LibDataChannel.LIB_NAME));
        LibDataChannel.initialize();
    }
}
