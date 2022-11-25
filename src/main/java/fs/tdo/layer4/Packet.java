package fs.tdo.layer4;

public class Packet {
    public final IpV4Header ipv4Header;
    public final UdpPacket udpPacket;

    public Packet(final byte[] bytes, final int offset) {
        ipv4Header = new IpV4Header(bytes, offset);
        udpPacket = new UdpPacket(
            ipv4Header.sourceIp,
            ipv4Header.destIp,
            bytes,
            offset + ipv4Header.getLength()
        );
    }

    public int getLength() {
        return ipv4Header.getLength() + udpPacket.totalLength;
    }

    public boolean isValid() {
        return ipv4Header.isValid() && udpPacket.isValid();
    }
}