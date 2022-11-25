package fs.tdo;

/**
 * Decode Base85.
 */
public class Layer0 extends Solver {

    Layer0(byte[] payload) {
        super(payload);
    }

    @Override
    byte[] solve() {
        return payload;
    }
}