package fs.tdo;

public class Utils {
    private Utils() {
    }

    public static byte[] subarray(byte[] array, int startIndexInclusive, int endIndexExclusive) {
        int newSize = endIndexExclusive - startIndexInclusive;
        byte[] subarray = new byte[newSize];
        System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
        return subarray;
    }
}
