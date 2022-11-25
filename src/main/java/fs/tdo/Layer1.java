package fs.tdo;

/**
 * Apply the following operations to each byte:
 *
 * <p>1. Flip every second bit <br>
 * 2. Rotate the bits one position to the right
 */
public class Layer1 extends Solver {
    protected Layer1(byte[] payload) {
        super(payload);
    }

    @Override
    byte[] solve() {
        final byte[] result = new byte[payload.length];
        for (int i = 0; i < payload.length; i++) {
            final int unsigned = Byte.toUnsignedInt(payload[i]);
            result[i] = (byte) rotateRight(flipEverySecondBit(unsigned));
        }
        return result;
    }

    private int flipEverySecondBit(final int b) {
        return b ^ Integer.parseInt("01010101", 2);
    }

    private int rotateRight(final int b) {
        int lastBit = b & 1;
        lastBit = lastBit << 7;
        int result = b >> 1;
        return result | lastBit;
    }
}
