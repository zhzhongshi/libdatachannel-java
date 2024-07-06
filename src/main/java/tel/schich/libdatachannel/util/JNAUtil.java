package tel.schich.libdatachannel.util;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.BiFunction;

public class JNAUtil {

    public static Pointer toPointer(byte[] bytes) {
        final Memory memory = new Memory(bytes.length + 1);
        memory.write(0, bytes, 0, bytes.length);
        return memory;
    }
}
