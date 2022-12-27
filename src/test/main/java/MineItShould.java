import dev.watchwolf.entities.Position;
import dev.watchwolf.entities.items.Item;
import dev.watchwolf.entities.items.ItemType;
import dev.watchwolf.tester.AbstractTest;
import dev.watchwolf.tester.TesterConnector;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.IOException;

@ExtendWith(MineItShould.class)
public class MineItShould extends AbstractTest {
    @Override
    public String getConfigFile() {
        return "src/test/main/resources/config.yaml";
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws IOException {
        super.beforeAll(extensionContext); // start the servers & clients

        // move the player to its desired location & give him a tool and the MineIt tool
        super.provideArguments(extensionContext).map(TesterConnector.class::cast).forEach(server -> {
            try {
                String username = server.getClients()[0];

                server.server.tp(username, new Position("world", 1000,100,1000));
                server.server.giveItem(username, new Item(ItemType.DIAMOND_PICKAXE));

                server.getClientPetition(0).runCommand("mineit tool");
            } catch (IOException ignore) {}
        });
    }

    @ParameterizedTest
    @ArgumentsSource(MineItShould.class)
    public void goBackOneStageWhenMined(TesterConnector connector) throws Exception {

    }
}
