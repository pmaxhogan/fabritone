/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.utils.schematic.format.defaults;

import baritone.utils.schematic.StaticSchematic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.datafixer.fix.ItemIdFix;
import net.minecraft.datafixer.fix.ItemInstanceTheFlatteningFix;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * @author Brady
 * @since 12/27/2019
 */
public final class MCEditSchematic extends StaticSchematic {

    public MCEditSchematic(CompoundTag schematic) {
        String type = schematic.getString("Materials");
        if (!type.equals("Alpha")) {
            throw new IllegalStateException("bad schematic " + type);
        }
        this.x = schematic.getInt("Width");
        this.y = schematic.getInt("Height");
        this.z = schematic.getInt("Length");

        byte[] blocks = schematic.getByteArray("Blocks");
        byte[] metadata = schematic.getByteArray("Data");

        byte[] additional = null;
        if (schematic.contains("AddBlocks")) {
            byte[] addBlocks = schematic.getByteArray("AddBlocks");
            additional = new byte[addBlocks.length * 2];
            for (int i = 0; i < addBlocks.length; i++) {
                additional[i * 2 + 0] = (byte) ((addBlocks[i] >> 4) & 0xF); // lower nibble
                additional[i * 2 + 1] = (byte) ((addBlocks[i] >> 0) & 0xF); // upper nibble
            }
        }
        this.states = new BlockState[this.x][this.z][this.y];
        for (int y = 0; y < this.y; y++) {
            for (int z = 0; z < this.z; z++) {
                for (int x = 0; x < this.x; x++) {
                    int blockInd = (y * this.z + z) * this.x + x;

                    int blockID = blocks[blockInd] & 0xFF;
                    if (additional != null) {
                        // additional is 0 through 15 inclusive since it's & 0xF above
                        blockID |= additional[blockInd] << 8;
                    }
                    int meta = metadata[blockInd] & 0xFF;

                    String itemFromBlockId = ItemIdFix.fromId(blockID);
                    String itemFromMetaId = ItemInstanceTheFlatteningFix.getItem(itemFromBlockId, meta);

                    Block block;
                    // if our block is something not changed in The Flattening, eg. cobblestone
                    if(itemFromMetaId == null){
                        block = Registry.BLOCK.get(Identifier.tryParse(itemFromBlockId));
                    }else{// if our block is something changed in The Flattening, eg. any kind of planks
                        block = Registry.BLOCK.get(Identifier.tryParse(itemFromMetaId));
                    }
//                    System.out.println(block.getDefaultState());

//                    this.states[x][z][y] = block.getStateFromMeta(meta);
                    this.states[x][z][y] = block.getDefaultState();
                }
            }
        }

        System.out.println("states: " + this.states);
        System.out.println("schematic " + schematic.toString());
    }
}
