package fs.tdo;

import fs.tdo.layer5.TomtelVm;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public class TomtelVmTest {

    @Test
    void runHelloWorld() throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(90);

        try (InputStream inputStream = getClass().getResourceAsStream("tomtelvm_hello_world.txt")) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)));
            reader.lines().forEach(line -> {
                String[] values = line.substring(0, line.indexOf('#')).split(" ");
                Arrays.stream(values)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .forEach(s -> bb.put((byte) Short.parseShort(s, 16)));
            });
        }

        String result = new String(new TomtelVm(bb.array()).run(), StandardCharsets.US_ASCII);
        assert result.equals("Hello, world!");
    }
}
