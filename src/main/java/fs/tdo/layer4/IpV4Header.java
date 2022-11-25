package fs.tdo.layer4;

import fs.tdo.Utils;
import java.nio.ByteBuffer;

public class IpV4Header {
    public final byte version;
    public final byte ihl;
    public final byte tos;
    public final byte flags;
    public final byte ttl;
    public final byte protocol;
    public final short totalLength;
    public final short identification;
    public final short fragmentOffset;
    public final short checksum;
    public final int sourceIp;
    public final int destIp;
    public final String sourceIpStr;
    public final String destIpStr;
    public final boolean dontFragment;
    public final boolean moreFragments;
    private final ByteBuffer bb;

    public IpV4Header(final byte[] bytes, final int offset) {
        version = (byte) (bytes[offset] >> 4 & 0xf);
        ihl = (byte) (bytes[offset] & 0xf);
        if (ihl != 5) {
            throw new RuntimeException("Options handling not implemented");
        }
        final byte[] data = Utils.subarray(bytes, offset, offset + ihl * 4);
        bb = ByteBuffer.wrap(data, 0, data.length);
        bb.get();

        tos = bb.get();
        totalLength = bb.getShort();
        identification = bb.getShort();
        final byte tmp = bb.get();
        flags = (byte) ((tmp & 0b11100000) >>> 5);
        dontFragment = (flags & 0b00000010) != 0;
        moreFragments = (flags & 0b00000100) != 0;
        fragmentOffset = (short) (((tmp & 0b00011111) << 8 & 0xff00 | bb.get() & 0xff) & 0x1fff);
        if (!dontFragment || moreFragments || fragmentOffset != 0) {
            throw new RuntimeException("Fragmentation handling not implemented");
        }
        ttl = bb.get();
        protocol = bb.get();
        checksum = bb.getShort();
        sourceIp = bb.getInt();
        destIp = bb.getInt();
        sourceIpStr = bytes[offset + 12]
            + "." + (0xff & bytes[offset + 13])
            + "." + (0xff & bytes[offset + 14])
            + "." + (0xff & bytes[offset + 15]);
        destIpStr = (0xff & bytes[offset + 16])
            + "." + (0xff & bytes[offset + 17])
            + "." + (0xff & bytes[offset + 18])
            + "." + (0xff & bytes[offset + 19]);
    }

    static int intTo16BitsWithCarryOver(int intValue) {
        while (intValue > 0xffff) {
            intValue = (intValue & 0xffff) + (intValue >> 16);
        }
        return intValue;
    }

    public boolean isValid() {
        return version == 4
            && sourceIpStr.equals("10.1.1.10")
            && destIpStr.equals("10.1.1.200")
            && ttl != 0
            && protocol == 17
            && validChecksum();
    }

    private boolean validChecksum() {
        if (bb.limit() % 2 != 0) {
            throw new IllegalStateException("length of ipv4 header is not even.");
        }
        return getChecksum() == 0xffff;
    }

    private int getChecksum() {
        bb.rewind();
        int accumulation = 0;
        while (bb.hasRemaining()) {
            accumulation += Short.toUnsignedInt(bb.getShort());
        }
        return intTo16BitsWithCarryOver(accumulation);
    }

    public int getLength() {
        return bb.limit();
    }
}