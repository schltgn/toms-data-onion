package fs.tdo;

import java.nio.charset.StandardCharsets;

public abstract class Solver {
    protected final byte[] payload;

    protected Solver(final byte[] payload) {
        String payloadString = new String(payload, StandardCharsets.US_ASCII);
        if (!payloadString.contains("<~") || !payloadString.contains("~>")) {
            throw new IllegalStateException("Payload does not look right, missing tokens <~ | ~>");
        }

        int start = payloadString.lastIndexOf("<~") + 2;
        int end = payloadString.lastIndexOf("~>");
        this.payload = Base85.decode(payloadString.substring(start, end));
    }

    abstract byte[] solve();
}
