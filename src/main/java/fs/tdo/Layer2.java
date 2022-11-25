package fs.tdo;

import java.util.BitSet;

/**
 * For each byte of the payload, the seven most significant bits carry data, and the least significant bit is the
 * parity bit. Combine the seven data bits from each byte where the parity bit is correct, discarding bytes where the
 * parity bit is incorrect.<br>
 * <br>
 * To determine if the parity bit is correct, first count how many '1' bits exist within the seven data bits. If the
 * count is odd, the parity bit should be '1'. If the count is even, the parity bit should be '0'.
 */
public class Layer2 extends Solver {
    protected Layer2(byte[] payload) {
        super(payload);
    }

    @Override
    byte[] solve() {
        final StringBuilder bits = new StringBuilder();
        for (final byte element : payload) {
            final int val = Byte.toUnsignedInt(element);
            final int parity = val & 1;
            if (isValid(val >> 1, parity)) {
                int checkBit = 0b10000000;
                for (int k = 0; k < 7; k++) {
                    final boolean bitVal = (val & checkBit) != 0;
                    bits.append(bitVal ? 1 : 0);
                    checkBit = checkBit >> 1;
                }
            }
        }
        final byte[] result = new byte[bits.length() / 8];
        int resultIndex = -1;

        for (int i = 0; i < bits.length(); i = i + 8) {
            result[++resultIndex] = (byte) Integer.parseInt(bits.substring(i, i + 8), 2);
        }
        return result;
    }

    private boolean isValid(final int data, final int parity) {
        final BitSet bits = BitSet.valueOf(new byte[] {(byte) data});
        final boolean even = bits.cardinality() % 2 == 0;
        return even && parity == 0 || !even && parity == 1;
    }
}
