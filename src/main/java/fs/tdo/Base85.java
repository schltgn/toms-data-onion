package fs.tdo;

import java.io.ByteArrayOutputStream;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class Base85 {

    private Base85() {
    }

    public static byte[] decode(String s) {
        s = s.trim();
        if (s.startsWith("<~") && s.endsWith("~>")) {
            s = s.substring(2, s.length() - 2).trim();
        }
        final CharacterIterator it = new StringCharacterIterator(s);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        long i = 0;
        int j = 0;
        for (char ch = it.first(); ch != CharacterIterator.DONE && ch != '~'; ch = it.next()) {
            if (ch == 'z' && j == 0) {
                writeMultiple(0, out, 4);
            } else if (ch == 'y' && j == 0) {
                writeMultiple(' ', out, 4);
            } else if (ch == 'x' && j == 0) {
                writeMultiple(-1, out, 4);
            } else if (ch >= '!' && ch <= 'u') {
                i = i * 85L + ch - '!';
                j++;
                if (j >= 5) {
                    out.write((int) (i >> 24L));
                    out.write((int) (i >> 16L));
                    out.write((int) (i >> 8L));
                    out.write((int) i);
                    i = 0;
                    j = 0;
                }
            }
        }
        if (j == 4) {
            i = i * 85L + 84L;
            out.write((int) (i >> 24L));
            out.write((int) (i >> 16L));
            out.write((int) (i >> 8L));
        } else if (j == 3) {
            i = i * 85L + 84L;
            i = i * 85L + 84L;
            out.write((int) (i >> 24L));
            out.write((int) (i >> 16L));
        } else if (j == 2) {
            i = i * 85L + 84L;
            i = i * 85L + 84L;
            i = i * 85L + 84L;
            out.write((int) (i >> 24L));
        }
        return out.toByteArray();
    }

    private static void writeMultiple(int value, ByteArrayOutputStream out, int times) {
        for (int i = 0; i < times; i++) {
            out.write(value);
        }
    }
}
