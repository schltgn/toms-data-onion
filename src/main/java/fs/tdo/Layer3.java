package fs.tdo;

/**
 * The payload has been encrypted by XOR'ing each byte with a secret, cycling key. The key is 32 bytes of random data,
 * which I'm not going to give you. You will need to use your hacker skills to discover what the key is, in order to
 * decrypt the payload.
 */
public class Layer3 extends Solver {
    private static final int keyLength = 32;

    protected Layer3(byte[] payload) {
        super(payload);
    }

    @Override
    byte[] solve() {
        final byte[] key = findKey(payload);
        return decrypt(payload, key);
    }

    private byte[] findKey(final byte[] input) {
        final byte[] key = new byte[keyLength];
        String decrypted = "==[ Layer 4/6: Network Traffic ]";
        final byte[] knownStartBytes = decrypted.getBytes();
        for (int i = 0; i < decrypted.length(); i++) {
            final int a = Byte.toUnsignedInt(input[i]);
            final int b = Byte.toUnsignedInt(knownStartBytes[i]);
            key[i] = (byte) (a ^ b);
        }
        return key;
    }

    private byte[] decrypt(final byte[] input, final byte[] key) {
        final byte[] result = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            final int b = Byte.toUnsignedInt(input[i]);
            result[i] = (byte) (b ^ key[i % key.length]);
        }
        return result;
    }
}
