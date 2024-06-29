package tel.schich.libdatachannel;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.nio.ByteBuffer;
import java.util.function.BiFunction;

public class JNAUtil {

    public static Pointer toPointer(String string) {
        final byte[] bytes = Native.toByteArray(string);
        return toPointer(bytes);
    }

    public static Pointer toPointer(byte[] bytes) {
        final Memory memory = new Memory(bytes.length + 1);
        memory.write(0, bytes, 0, bytes.length);
        memory.setByte(bytes.length, (byte) 0);
        return memory;
    }

    public static String readStringWithBuffer(BiFunction<ByteBuffer, Integer, Integer> biFunc) {
        final var bufferSize = biFunc.apply(null, -1);
        final var buffer = ByteBuffer.allocate(bufferSize);
        final var code = biFunc.apply(buffer, bufferSize);
        if (code < 0) {
            throw new IllegalStateException("Error: " + code);
        }

        final var bytes = new byte[code - 1];
        buffer.get(bytes, 0, code - 1);
        return new String(bytes);
    }
}
