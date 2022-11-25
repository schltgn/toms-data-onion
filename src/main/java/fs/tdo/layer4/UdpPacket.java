package fs.tdo.layer4;

import fs.tdo.Utils;
import java.nio.ByteBuffer;

public class UdpPacket {
    public final short destPort;
    public final short totalLength;
    public final short checksum;
    private final int sourceIp;
    private final int destIp;
    private final byte[] raw;

    public UdpPacket(final int sourceIp, final int destIp, final byte[] bytes, final int offset) {
        this.sourceIp = sourceIp;
        this.destIp = destIp;
        destPort = (short) ((bytes[offset + 2] << 8 & 0xff00 | bytes[offset + 3] & 0xff) & 0xffff);
        totalLength = (short) ((bytes[offset + 4] << 8 & 0xff00 | bytes[offset + 5] & 0xff) & 0xffff);
        checksum = (short) ((bytes[offset + 6] << 8 & 0xff00 | bytes[offset + 7] & 0xff) & 0xffff);
        raw = Utils.subarray(bytes, offset, offset + totalLength);
    }

    public boolean isValid() {
        return Short.toUnsignedInt(destPort) == 42069 && validChecksum();
    }

    public byte[] getPayload() {
        return Utils.subarray(raw, 8, raw.length);
    }

    private boolean validChecksum() {
        if (checksum == 0) {
            return true;
        }
        byte[] data = new byte[12 + raw.length];
        final ByteBuffer bb = ByteBuffer.wrap(data);
        bb.putInt(sourceIp);
        bb.putInt(destIp);
        bb.put((byte) 0);
        bb.put((byte) 17);
        bb.putShort(totalLength);
        bb.put(raw);

        return computeChecksum(data) == 0xffff;
    }

    private int computeChecksum(byte[] data) {
        int sumHighBytes = 0;
        for (int i = 0; i < data.length; i += 2) {
            sumHighBytes += Byte.toUnsignedInt(data[i]);
        }
        int sumLowBytes = 0;
        for (int i = 1; i < data.length; i += 2) {
            sumLowBytes += Byte.toUnsignedInt(data[i]);
        }
        int totalSum = (sumHighBytes << 8) + sumLowBytes;
        return IpV4Header.intTo16BitsWithCarryOver(totalSum);
    }
}