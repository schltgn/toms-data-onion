package fs.tdo;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Main {

    public static void main(String[] args) throws Exception {
        URL url = Objects.requireNonNull(
            Main.class.getClassLoader().getResource("layer0.txt")
        );
        try (InputStream inputStream = url.openStream()) {
            byte[] layer0 = inputStream.readAllBytes();
            byte[] layer1 = new Layer0(layer0).solve();
            byte[] layer2 = new Layer1(layer1).solve();
            byte[] layer3 = new Layer2(layer2).solve();
            byte[] layer4 = new Layer3(layer3).solve();
            byte[] layer5 = new Layer4(layer4).solve();
            byte[] layer6 = new Layer5(layer5).solve();
            byte[] core = new Layer6(layer6).solve();
            System.out.print(new String(core, StandardCharsets.US_ASCII));
        }
    }
}
