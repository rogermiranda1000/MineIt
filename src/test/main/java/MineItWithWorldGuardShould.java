import dev.watchwolf.client.ClientPetition;
import dev.watchwolf.entities.Position;
import dev.watchwolf.entities.blocks.Block;
import dev.watchwolf.entities.blocks.Blocks;
import dev.watchwolf.entities.items.Item;
import dev.watchwolf.entities.items.ItemType;
import dev.watchwolf.tester.AbstractTest;
import dev.watchwolf.tester.ExtendedClientPetition;
import dev.watchwolf.tester.TesterConnector;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MineItWithWorldGuardShould.class)
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

        try {
            // some tests report that the tool wasn't found (maybe this solves it?)
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // we need a mine in MINE_BLOCK_INSIDE_WG_REGION and MINE_BLOCK_OUTSIDE_WG_REGION
        try {
            new MineItShould().createMineWithLeftClick(server);
            server.runCommand("mineit start " + MineItShould.CREATED_MINE_IN_TESTS_NAME);
            // set the blocks to the max. stage, so they don't change forwards
            server.setBlock(MINE_BLOCK_INSIDE_WG_REGION, Blocks.DIAMOND_ORE);
            server.setBlock(MINE_BLOCK_OUTSIDE_WG_REGION, Blocks.DIAMOND_ORE);
        } catch (Exception e) {
            System.err.println(e.toString());
        }

        // create WG region
        server.createRegion("mine", MINE_BLOCK_INSIDE_WG_REGION, BLOCK_INSIDE_WG_REGION);

        server.runCommand("deop " + username); // we need a normal user to test the WG region
    }

    private static void equipPickaxe(ClientPetition client) throws IOException {
        client.equipItemInHand(new Item(ItemType.DIAMOND_PICKAXE));
    }

    /* TESTS ZONE */

    @ParameterizedTest
    @ArgumentsSource(MineItWithWorldGuardShould.class)
    public void allowMineInsideWorldGuardRegion(TesterConnector connector) throws Exception {
        ExtendedClientPetition userPetition = connector.getClientPetition(0);

        MineItWithWorldGuardShould.equipPickaxe(userPetition);

        int tries = 6; // there's some chance that the stage goes immediately back to stone; we'll try a few times
        Block got = null;
        while (tries > 0 && !Blocks.STONE.equals(got)) {
            userPetition.breakBlock(MINE_BLOCK_INSIDE_WG_REGION);
            got = connector.server.getBlock(MINE_BLOCK_INSIDE_WG_REGION);
            tries--;
        }

        assertEquals(Blocks.STONE, got);
    }

    @ParameterizedTest
    @ArgumentsSource(MineItWithWorldGuardShould.class)
    public void allowMineOutsideWorldGuardRegion(TesterConnector connector) throws Exception {
        ExtendedClientPetition userPetition = connector.getClientPetition(0);

        MineItWithWorldGuardShould.equipPickaxe(userPetition);

        int tries = 6; // there's some chance that the stage goes immediately back to stone; we'll try a few times
        Block got = null;
        while (tries > 0 && !Blocks.STONE.equals(got)) {
            userPetition.breakBlock(MINE_BLOCK_OUTSIDE_WG_REGION);
            got = connector.server.getBlock(MINE_BLOCK_OUTSIDE_WG_REGION);
            tries--;
        }

        assertEquals(Blocks.STONE, got);
    }

    /**
     * If there's no mine, then WG should protect that area
     */
    @ParameterizedTest
    @ArgumentsSource(MineItWithWorldGuardShould.class)
    public void denyBreakInsideWorldGuardRegion(TesterConnector connector) throws Exception {
        ExtendedClientPetition userPetition = connector.getClientPetition(0);

        Block blockInWorld = connector.getBlock(BLOCK_INSIDE_WG_REGION);

        MineItWithWorldGuardShould.equipPickaxe(userPetition);
        userPetition.breakBlock(BLOCK_INSIDE_WG_REGION);

        assertEquals(blockInWorld, connector.getBlock(BLOCK_INSIDE_WG_REGION));
    }
}
