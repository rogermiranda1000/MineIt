import dev.watchwolf.client.ClientPetition;
import dev.watchwolf.entities.Position;
import dev.watchwolf.entities.blocks.Block;
import dev.watchwolf.entities.blocks.Blocks;
import dev.watchwolf.entities.entities.DroppedItem;
import dev.watchwolf.entities.entities.Entity;
import dev.watchwolf.entities.items.Item;
import dev.watchwolf.entities.items.ItemType;
import dev.watchwolf.tester.AbstractTest;
import dev.watchwolf.tester.ExtendedClientPetition;
import dev.watchwolf.tester.TesterConnector;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.platform.commons.PreconditionViolationException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MineItShould.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MineItShould extends AbstractTest {
    /* WORLD INFORMATION ZONE */
    private static final Position []CONSECUTIVE_BLOCKS = {
            new Position("world", 999.5, 101.5, 998.5),
            new Position("world", 1000.5, 101.5, 998.5),
            new Position("world", 1001.5, 101.5, 998.5),
    };

    private static final Position ONLY_ROCK_STAGE_MINE_BLOCK = new Position("world", 1002, 101, 1002);

    private static final Position DEFAULT_MINE_BLOCK = new Position("world", 1000, 101, 1002);

    private static final Position ONLY_ROCK_STAGE_DROPPING_ICE_MINE_BLOCK = new Position("world", 998, 101, 1002);

    private static final Position PLAYER_POSITION = new Position("world", 1000.5,100,1000.5);

    private static final ItemType MINEIT_TOOL_ITEMTYPE = ItemType.STICK;
    private static final Block MINEIT_DEFAULT_BASE_STAGE_BLOCK = Blocks.BEDROCK;

    private static final String CREATED_MINE_IN_TESTS_NAME = "TestMine";

    /* CONFIG & SETUP ZONE */
    @Override
    public String getConfigFile() {
        return "src/test/main/resources/config.yaml";
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
    }

    private static void equipPickaxe(ClientPetition client) throws IOException {
        client.equipItemInHand(new Item(ItemType.DIAMOND_PICKAXE));
    }

    private static void equipTool(ClientPetition client) throws IOException {
        client.equipItemInHand(new Item(MineItShould.MINEIT_TOOL_ITEMTYPE));
    }

    /* TESTS ZONE */

    /**
     * In the beforeAll we've runned `/mineit tool`, so the user should have the tool
     */
    @Order(1)
    @ParameterizedTest
    @ArgumentsSource(MineItShould.class)
    public void addMineItTool(TesterConnector connector) throws Exception {
        ExtendedClientPetition client = connector.getClientPetition(0);
        assertTrue(MineItShould.getItemAmounts(client.getInventory().getItems()).get(MineItShould.MINEIT_TOOL_ITEMTYPE) > 0);
    }

    /**
     * When you select a STONE block (to create a mine), you should also select the adjacent blocks
     */
    @Order(2)
    @ParameterizedTest
    @ArgumentsSource(MineItShould.class)
    public void convertSelectedStoneIntoTargetedMinesOnMineItToolUsage(TesterConnector connector) throws Exception {
        ExtendedClientPetition client = connector.getClientPetition(0);
        MineItShould.equipTool(client);

        Position converting = CONSECUTIVE_BLOCKS[0];

        int intents = 5; // TODO it should work the first time
        do {
            client.lookAt(converting);
            Thread.sleep(5000);
        } while ((Math.abs(client.getYaw() - 150f) > 15f || Math.abs(client.getPitch() - 3f) > 10f) && (intents--) > 0); // this is the rotation needed to look at that block
        if (intents == 0) System.out.println("Exhaused all lookAt intents; this test may fail.");
        client.use();

        assertEquals(Blocks.EMERALD_BLOCK, connector.server.getBlock(converting));
    }

    /**
     * When you select a STONE block (to create a mine), you should also select the adjacent blocks
     */
    @Order(3) // @pre convertAdjacentStoneIntoTargetedMinesOnMineItToolUsage
    @ParameterizedTest
    @ArgumentsSource(MineItShould.class)
    public void convertAdjacentStoneIntoTargetedMinesOnMineItToolUsage(TesterConnector connector) throws Exception {
        // in the previous test we've changed the first block; let's check if the adjacent ones got changed too
        assertEquals(Blocks.EMERALD_BLOCK, connector.server.getBlock(CONSECUTIVE_BLOCKS[2]));
    }

    @Order(4) // @pre convertAdjacentStoneIntoTargetedMinesOnMineItToolUsage
    @ParameterizedTest
    @ArgumentsSource(MineItShould.class)
    public void createSelectedMine(TesterConnector connector) throws Exception {
        ExtendedClientPetition client = connector.getClientPetition(0);

        String createReturn = client.runCommand("mineit create " + CREATED_MINE_IN_TESTS_NAME);
        assertTrue(createReturn.contains("Mine created successfully"), "Expected the 'mine created' message, got '" + createReturn + "' instead");

        // once the mine is created there should be a bedrock block
        assertEquals(MineItShould.MINEIT_DEFAULT_BASE_STAGE_BLOCK, connector.server.getBlock(CONSECUTIVE_BLOCKS[0]));

        String mines = client.runCommand("mineit list");
        assertTrue(mines.contains(MineItShould.CREATED_MINE_IN_TESTS_NAME), "Expected '" + MineItShould.CREATED_MINE_IN_TESTS_NAME + "' in mines list. Return by MineIt: " + mines);
    }

    /**
     * You should be able to create a mine with the MineIt tool
     */
    @Order(5) // @pre createMineWithRightClick
    @ParameterizedTest
    @ArgumentsSource(MineItShould.class)
    public void removeCreatedMine(TesterConnector connector) throws Exception {
        ExtendedClientPetition client = connector.getClientPetition(0);

        String removeReturn = client.runCommand("mineit remove " + CREATED_MINE_IN_TESTS_NAME);
        assertTrue(removeReturn.contains("removed"), "Expected a confirmation remove mine message, got '" + removeReturn + "' instead");

        String mines = client.runCommand("mineit list");
        assertFalse(mines.contains(MineItShould.CREATED_MINE_IN_TESTS_NAME), "Expected not to find '" + MineItShould.CREATED_MINE_IN_TESTS_NAME + "' in mines list. Return by MineIt: " + mines);
    }

    /**
     * You should be able to create a mine with the MineIt tool
     */
    @Order(6) // @pre removeCreatedMine
    @ParameterizedTest
    @ArgumentsSource(MineItShould.class)
    public void createMineWithLeftClick(TesterConnector connector) throws Exception {
        ExtendedClientPetition client = connector.getClientPetition(0);
        MineItShould.equipTool(client);

        // first remove the blocks previously setted by the last mine
        for (Position b : MineItShould.CONSECUTIVE_BLOCKS) connector.server.setBlock(b, Blocks.STONE);

        client.lookAt(CONSECUTIVE_BLOCKS[0]);
        client.hit(); // instead of use, hit

        assertEquals(Blocks.EMERALD_BLOCK, connector.server.getBlock(CONSECUTIVE_BLOCKS[2]));

        createSelectedMine(connector); // now create it
    }

    /**
     * we have one stone block at ONLY_ROCK_STAGE_MINE_BLOCK
     * it should turn into bedrock on mined
     */
    @ParameterizedTest
    @ArgumentsSource(MineItShould.class)
    public void goBackOneStageWhenMined(TesterConnector connector) throws Exception {
        ExtendedClientPetition client = connector.getClientPetition(0);
        MineItShould.equipPickaxe(client);

        // do we really have a stone block at ONLY_ROCK_STAGE_MINE_BLOCK?
        int tries = 25;
        while ((tries--) > 0 && !connector.server.getBlock(ONLY_ROCK_STAGE_MINE_BLOCK).equals(Blocks.STONE)) Thread.sleep(1000);
        if (tries == 0) throw new PreconditionViolationException("We expected STONE at " + ONLY_ROCK_STAGE_MINE_BLOCK + "; found " + connector.server.getBlock(ONLY_ROCK_STAGE_MINE_BLOCK) + " instead");

        tries = 6; // there's some chance that the stage goes immediately back to stone; we'll try a few times
        Block got = null;
        while (tries > 0 && !Blocks.BEDROCK.equals(got)) {
            client.breakBlock(ONLY_ROCK_STAGE_MINE_BLOCK);
            got = connector.server.getBlock(ONLY_ROCK_STAGE_MINE_BLOCK);
            tries--;
        }

        assertEquals(Blocks.BEDROCK, got);
    }

    /**
     * we'll place a bedrock block (stage 0) on ONLY_ROCK_STAGE_MINE_BLOCK
     * it should turn into stone
     */
    @ParameterizedTest
    @ArgumentsSource(MineItShould.class)
    public void goToTheNextStage(TesterConnector connector) throws Exception {
        connector.server.setBlock(ONLY_ROCK_STAGE_MINE_BLOCK, Blocks.BEDROCK);

        int tries = 25;
        Block got = null;
        while (tries > 0) {
            got = connector.server.getBlock(ONLY_ROCK_STAGE_MINE_BLOCK);
            if (!Blocks.STONE.equals(got)) Thread.sleep(1000);
            tries--;
        }

        assertEquals(Blocks.STONE, got);
    }

    @ParameterizedTest
    @ArgumentsSource(MineItShould.class)
    public void dropSpecialBlocks(TesterConnector connector) throws Exception {
        ExtendedClientPetition client = connector.getClientPetition(0);
        MineItShould.equipPickaxe(client);

        client.breakBlock(ONLY_ROCK_STAGE_DROPPING_ICE_MINE_BLOCK);

        HashMap<ItemType,Integer> drops = MineItShould.getItemAmounts(connector.server.getEntities(ONLY_ROCK_STAGE_DROPPING_ICE_MINE_BLOCK,5f));

        assertEquals(1, drops.get(ItemType.ICE));
    }

    private static HashMap<ItemType,Integer> getItemAmounts(Entity ...entities) {
        return MineItShould.getItemAmounts(Arrays.stream(entities).filter(e -> e instanceof DroppedItem).map(e -> ((DroppedItem)e).getItem()).toArray(Item[]::new));
    }

    /**
     * Given a container items it returns all the found items and the amount
     * @param items All the items in the container
     * @return {DIRT:2,WHEAT:6,...}
     */
    private static HashMap<ItemType,Integer> getItemAmounts(Item ...items) {
        HashMap<ItemType,Integer> r = new HashMap<>();

        for (Item i : items) {
            if (i == null) continue;

            Integer acum = r.get(i.getType());
            if (acum == null) r.put(i.getType(), (int)i.getAmount());
            else r.put(i.getType(), acum + i.getAmount());
        }

        return r;
    }
}
