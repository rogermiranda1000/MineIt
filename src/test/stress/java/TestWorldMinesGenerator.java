import dev.watchwolf.entities.Position;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class TestWorldMinesGenerator {
    public abstract Iterator<Position> getMineBlocksSupplier();

    public void generateMinesJSON(File out, int blockLimit, BiFunction<Position,Integer,String> blockToMineNameConverter) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append('[');

        // add the mines
        Iterator<Position> positions = this.getMineBlocksSupplier();
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

    public static class CompactTestWorldMinesGenerator extends TestWorldMinesGenerator {
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
        @Override
        public Iterator<Position> getMineBlocksSupplier() {
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
    }

    public static class LargeAreaTestWorldMinesGenerator extends TestWorldMinesGenerator {
        /**
         * This returns a supplier of blocks that are in the world to add mines.
         *
         * The world is a large rectangle of height 1.
         * - The first block is at (988,3,1038)
         * - The last block is at (1026,3,2000)
         */
        @Override
        public Iterator<Position> getMineBlocksSupplier() {
            return new Iterator<Position>() {
                private static final int BLOCKS_PER_ROW = (1026-988 +1);
                private static final int ROWS = (2000-1038 +1);
                private static final int NUM_BLOCKS = BLOCKS_PER_ROW*ROWS;

                private int index = 0;

                @Override
                public boolean hasNext() {
                    return this.index < NUM_BLOCKS;
                }

                @Override
                public Position next() {
                    if (!this.hasNext()) return null;

                    int rowNum = this.index / BLOCKS_PER_ROW,
                            colNum = this.index % BLOCKS_PER_ROW;

                    this.index++;

                    return new Position("world", 988 + colNum, 3, 1038 + rowNum);
                }
            };
        }
    }
}
