import dev.watchwolf.entities.Position;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class TestWorldMinesGenerator {
    /**
     * This returns a supplier of blocks that are in the world to add mines.
     *
     * The world is set up in the following way:
     * - There's rows of STONE blocks. You'll have one row with STONE, the other
     *   empty (AIR), and then STONE again
     * - The first block is at (988,98,989)
     * - The last block is at (1026,4,1037)
     *
     * One row example: starts at (988,98,989), ends at (1026,4,989)
     */
    public static Iterator<Position> getMineBlocksSupplier() {
        return new Iterator<Position>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                int indexCpy = this.index;
                boolean r = (this.next() != null);
                this.index = indexCpy;
                return r;
            }

            @Override
            public Position next() {
                this.index++;

                // good enough... TODO use ✨maths✨ to get the block
                int checking = 0;
                for (int z = 989; z <= 1037; z += 2) {
                    for (int x = 988; x <= 1026; x++) {
                        for (int y = 4; y <= 98; y++) {
                            if (++checking == index) return new Position("world", x, y, z);
                        }
                    }
                }
                return null;
            }
        };
    }

    public static void generateMinesJSON(File out, int blockLimit, BiFunction<Position,Integer,String> blockToMineNameConverter) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append('[');

        // add the mines
        Iterator<Position> positions = TestWorldMinesGenerator.getMineBlocksSupplier();
        int index = 0;
        while (++index < blockLimit && positions.hasNext()) {
            Position current = positions.next();

            sb.append("{\"world\":\"")
                    .append(current.getWorld())
                    .append("\",\"x\":")
                    .append(current.getBlockX())
                    .append(",\"y\":")
                    .append(current.getBlockY())
                    .append(",\"z\":")
                    .append(current.getBlockZ())
                    .append(",\"object\":\"")
                    .append(blockToMineNameConverter.apply(current, index-1))
                    .append("\"},");
        }

        if (sb.length() > 1) sb.setLength(sb.length()-1); // remove last ','
        sb.append(']');

        Files.write(out.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
    }
}
