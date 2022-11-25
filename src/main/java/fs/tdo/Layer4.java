package fs.tdo;

import fs.tdo.layer4.Packet;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * The payload for this layer is encoded as a stream of raw network data, as if the solution was being received over the
 * internet. The data is a series of IPv4 packets with User Datagram Protocol (UDP) inside. Extract the payload data
 * from inside each packet, and combine them together to form the solution.
 *
 * <p>Each packet has three segments: the IPv4 header, the UDP header, and the data section. So the first 20 bytes of
 * the payload will be the IPv4 header of the first packet. The next 8 bytes will be the UDP header of the first packet.
 * This is followed by a variable-length data section for the first packet. After the data section you will find the
 * second packet, starting with another 20 byte IPv4 header, and so on.
 *
 * <p>You will need to read the specifications for IPv4 and UDP in order to parse the data. The official specification
 * for IPv4 is RFC 791 (<a href="https://tools.ietf.org/html/rfc791">...</a>) and for UDP it is
 * RFC 768 (<a href="https://tools.ietf.org/html/rfc768">...</a>). The Wikipedia pages for these two protocols
 * are also good, and probably easier to read than the RFCs.
 *
 * <p>However, the payload contains extra packets that are not part of the solution. Discard these corrupted and
 * irrelevant packets when forming the solution.
 *
 * <p>Each valid packet of the solution has the following properties. Discard packets that do not have all of these
 * properties.
 *
 * <p>- The packet was sent FROM any port of 10.1.1.10 - The packet was sent TO port 42069 of 10.1.1.200 - The IPv4
 * header checksum is correct - The UDP header checksum is correct
 *
 * <p>WARNING: Failing to do this properly WILL cause the next layer to be unsolveable. If you include incorrect packets
 * in your solution, the result may be readable and look correct, but its payload WILL be corrupted in ways that are
 * impossible to detect. Trust me.
 *
 * <p>The packets appear in the correct order. No reordering is necessary.
 */
public class Layer4 extends Solver {
    protected Layer4(byte[] payload) {
        super(payload);
    }

    @Override
    byte[] solve() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            int i = 0;
            while (i < payload.length) {
                final Packet packet = new Packet(payload, i);
                i += packet.getLength();
                if (packet.isValid()) {
                    outputStream.write(packet.udpPacket.getPayload());
                }
            }
            if (i != payload.length) {
                throw new IllegalStateException("Wrong number of bytes read, more bytes read than available. i: " + i);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
