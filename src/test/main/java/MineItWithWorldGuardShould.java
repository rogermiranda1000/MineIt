import dev.watchwolf.client.ClientPetition;
import dev.watchwolf.entities.Position;
import dev.watchwolf.entities.blocks.Block;
import dev.watchwolf.entities.blocks.Blocks;
import dev.watchwolf.entities.items.Item;
import dev.watchwolf.entities.items.ItemType;
import dev.watchwolf.tester.AbstractTest;
import dev.watchwolf.tester.TesterConnector;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MineItWithWorldGuardShould.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MineItWithWorldGuardShould extends AbstractTest {
    /* WORLD INFORMATION ZONE */
    private static final Position MINE_BLOCK_INSIDE_WG_REGION = new Position("world", 999.5, 101.5, 998.5);
    private static final Position MINE_BLOCK_OUTSIDE_WG_REGION = new Position("world", 1000.5, 101.5, 998.5);
    private static final Position BLOCK_INSIDE_WG_REGION = new Position("world", 999.5, 100.5, 998.5);

    private static final Position PLAYER_POSITION = new Position("world", 1000.5,100,1000.5);

    private static final ItemType MINEIT_TOOL_ITEMTYPE = ItemType.STICK;
    private static final Block MINEIT_DEFAULT_BASE_STAGE_BLOCK = Blocks.BEDROCK;

    /* CONFIG & SETUP ZONE */
    @Override
    public String getConfigFile() {
        return "src/test/main/resources/config-with-wg.yaml";
    }

    /**
     * We want to set the user position and give him a pickaxe and the MineIt tool
     */
    @Override
    public void beforeAll(TesterConnector server) throws IOException {
        String username = server.getClients()[0];

        server.server.tp(username, PLAYER_POSITION);
        server.server.giveItem(username, new Item(ItemType.DIAMOND_PICKAXE));

        server.server.opPlayer(username); // we need the user to be admin if we want to add&remove mines
        server.getClientPetition(0).runCommand("mineit tool");

        // TODO create WG region
    }

    private static void equipPickaxe(ClientPetition client) throws IOException {
        client.equipItemInHand(new Item(ItemType.DIAMOND_PICKAXE));
    }

    private static void equipTool(ClientPetition client) throws IOException {
        client.equipItemInHand(new Item(MineItWithWorldGuardShould.MINEIT_TOOL_ITEMTYPE));
    }

    /* TESTS ZONE */

    /**
     * In the beforeAll we've runned `/mineit tool`, so the user should have the tool
     */
    @Order(1)
    @ParameterizedTest
    @ArgumentsSource(MineItWithWorldGuardShould.class)
    public void addMineItTool(TesterConnector connector) throws Exception {
        new MineItShould().createMineWithLeftClick(connector);

        assertTrue(connector.runCommand("mineit list").contains(MineItShould.CREATED_MINE_IN_TESTS_NAME));
    }
}
