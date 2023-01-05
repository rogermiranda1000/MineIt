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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.platform.commons.PreconditionViolationException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MineItShould.class)
public class MineItShould extends AbstractTest {
    /* WORLD INFORMATION ZONE */
    private static final Position []CONSECUTIVE_BLOCKS = {
            new Position("world", 999, 101, 998),
            new Position("world", 1000, 101, 998),
            new Position("world", 1001, 101, 998),
    };

    private static final Position ONLY_ROCK_STAGE_MINE_BLOCK = new Position("world", 1002, 101, 1002);

    private static final Position DEFAULT_MINE_BLOCK = new Position("world", 1000, 101, 1002);

    private static final Position ONLY_ROCK_STAGE_DROPPING_ICE_MINE_BLOCK = new Position("world", 998, 101, 1002);

    private static final Position PLAYER_POSITION = new Position("world", 1000.5,100,1000.5);

    /* CONFIG & SETUP ZONE */
    @Override
    public String getConfigFile() {
        return "src/test/main/resources/config.yaml";
    }

    /**
     * We want to set the user position and give him a pickaxe and the MineIt tool
     */
    @Override
    public void beforeAll(ExtensionContext extensionContext) throws IOException {
        super.beforeAll(extensionContext); // start the servers & clients

        // move the player to its desired location & give him a tool and the MineIt tool
        super.provideArguments(extensionContext).map(arg -> (TesterConnector)arg.get()[0]).forEach(server -> {
            try {
                String username = server.getClients()[0];

                server.server.tp(username, PLAYER_POSITION);
                server.server.giveItem(username, new Item(ItemType.DIAMOND_PICKAXE));

                server.getClientPetition(0).runCommand("mineit tool");
            } catch (IOException ignore) {}
        });
    }

    private static void equipPickaxe(ClientPetition client) throws IOException {
        client.equipItemInHand(new Item(ItemType.DIAMOND_PICKAXE));
    }

    private static void equipTool(ClientPetition client) throws IOException {
        client.equipItemInHand(new Item(ItemType.STICK));
    }

    /* TESTS ZONE */

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
        int tries = 20;
        while ((tries--) > 0 && !connector.server.getBlock(ONLY_ROCK_STAGE_MINE_BLOCK).equals(Blocks.STONE)) Thread.sleep(1000);
        if (tries == 0) throw new PreconditionViolationException("We expected STONE at " + ONLY_ROCK_STAGE_MINE_BLOCK + "; found " + connector.server.getBlock(ONLY_ROCK_STAGE_MINE_BLOCK) + " instead");

        tries = 4; // there's some chance that the stage goes immediately back to stone; we'll try a few times
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

        int tries = 20;
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
    public void convertAdjacentStoneIntoTargetedMinesOnMineItToolUsage(TesterConnector connector) throws Exception {
        ExtendedClientPetition client = connector.getClientPetition(0);
        MineItShould.equipTool(client);

        client.lookAt(CONSECUTIVE_BLOCKS[0]);
        //client.hit(); TODO

        assertEquals(Blocks.EMERALD_BLOCK, connector.server.getBlock(CONSECUTIVE_BLOCKS[2]));
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
