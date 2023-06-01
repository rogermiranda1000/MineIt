import dev.watchwolf.entities.Position;
import dev.watchwolf.tester.AbstractTest;
import dev.watchwolf.tester.TesterConnector;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.File;
import java.io.IOException;

@ExtendWith(BlocksStressTest.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BlocksStressTest extends AbstractTest {
    /* WORLD INFORMATION ZONE */
    private static final int MAX_BLOCKS_IN_MINE = Integer.MAX_VALUE;

    /**
     * In the setup world, this are the coordinates the player needs to be in
     */
    private static final Position PLAYER_POSITION = new Position("world", 1000.5,100,1000.5);



    /* CONFIG & SETUP ZONE */

    @Override
    public String getConfigFile() {
        return "src/test/stress/resources/config.yaml";
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws IOException {
        // we need the Mines.json first (autogenerated by TestWorldMinesGenerator)
        TestWorldMinesGenerator.generateMinesJSON(
                new File("./src/test/stress/resources/autogenerated/Mines.json"),
                MAX_BLOCKS_IN_MINE,
                (block) -> "TestMine" // only one mine
        );

        // now you can run the tests
        super.beforeAll(extensionContext);
    }

    @Override
    public void beforeAll(TesterConnector server) throws IOException {
        String username = server.getClients()[0];

        // make sure the chunks with the mines are loaded
        server.server.tp(username, PLAYER_POSITION);
    }



    /* TESTS ZONE */

    /**
     * MineIt lags the server just by having active mines; no need to do things.
     */
    @Order(1)
    @ParameterizedTest
    @ArgumentsSource(MineItShould.class)
    public void runFor10Minutes(TesterConnector connector) throws Exception {
        Thread.sleep(10*60*1000);

        // we'll automatically get the timings at the end of the test
    }
}