package fs.tdo;

import java.security.Key;
import java.security.Security;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * This payload has been encrypted with AES-256 in Cipher Block
 * Chaining (CBC) mode. To decrypt the payload you will need
 * the encryption key and the initialization vector (IV). It is
 * not possible to guess these, so I will just give them to
 * you. They are at the start of the payload.
 *
 * <p>But... surprise! The key is also encrypted with AES. It
 * turns out that the U.S. Government also has standards for
 * how to encrypt encryption keys. I've encrypted the key using
 * the AES Key Wrap algorithm specified in RFC 3394. How do you
 * decrypt the key? Well, you will need another key, called the
 * "key encrypting key" (KEK), and another initialization
 * vector. These are also impossible to guess, so I will just
 * give them to you. They are also at the start of the payload.
 *
 * <p>But... surprise! Just kidding. I haven't encrypted the KEK.
 * The U.S. Government does not have a standard for encrypting
 * key encrypting keys, as far as I'm aware. That would be a
 * bit too crazy.
 *
 * <p>The payload is structured like this:
 *
 * <p>- First 32 bytes: The 256-bit key encrypting key (KEK).
 * - Next 8 bytes: The 64-bit initialization vector (IV) for
 * the wrapped key.<br>
 * - Next 40 bytes: The wrapped (encrypted) key. When
 * decrypted, this will become the 256-bit encryption key.<br>
 * - Next 16 bytes: The 128-bit initialization vector (IV) for
 * the encrypted payload.<br>
 * - All remaining bytes: The encrypted payload.
 *
 * <p>The first step is to use the KEK and the 64-bit IV to unwrap
 * the wrapped key. The second step is to use the unwrapped key
 * and the 128-bit IV to decrypt the rest of the payload.
 *
 * <p>Don't try to write the decryption algorithms yourself. Or
 * do. I'm not your dad. You do you. Personally, I used OpenSSL
 * to generate the payload for this layer, and reused the
 * `aes_key_wrap` Ruby gem that I wrote years ago.
 */
public class Layer5 extends Solver {

    Layer5(byte[] payload) {
        super(payload);
    }

    @Override
    byte[] solve() {
        try {
            Security.addProvider(new BouncyCastleProvider());

            byte[] kek = Arrays.copyOfRange(payload, 0, 32);
            byte[] kekIv = Arrays.copyOfRange(payload, 32, 40);
            byte[] wrappedKey = Arrays.copyOfRange(payload, 40, 80);
            byte[] ivPayload = Arrays.copyOfRange(payload, 80, 96);
            byte[] encryptedPayload = Arrays.copyOfRange(payload, 96, payload.length);

            Key unwrappedKey = unwrap(wrappedKey, kek, kekIv);
            return decrypt(encryptedPayload, unwrappedKey, ivPayload);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Key unwrap(byte[] wrappedKey,
                       byte[] kek,
                       byte[] iv)
        throws Exception {
        Cipher cipher = Cipher.getInstance("AESWrap", "BC");
        cipher.init(
            Cipher.UNWRAP_MODE,
            new SecretKeySpec(kek, "AES"),
            new IvParameterSpec(iv)
        );
        return cipher.unwrap(
            wrappedKey,
            "AES",
            Cipher.SECRET_KEY
        );
    }

    private byte[] pad(byte[] data, int blocksize) {
        int remaining = data.length % blocksize;
        if (remaining == 0) {
            return data;
        }

        int padLen = blocksize - remaining;
        return Arrays.copyOf(data, data.length + padLen);
    }

    private byte[] decrypt(byte[] encryptedData,
                           Key key,
                           byte[] ivPayload)
        throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(
            Cipher.DECRYPT_MODE,
            key,
            new IvParameterSpec(ivPayload)
        );
        return cipher.doFinal(encryptedData);
    }
}