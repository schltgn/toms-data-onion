package fs.tdo;

import fs.tdo.layer5.TomtelVm;

public class Layer6 extends Solver {

    Layer6(byte[] payload) {
        super(payload);
    }

    @Override
    byte[] solve() {
        return new TomtelVm(payload).run();
    }
}