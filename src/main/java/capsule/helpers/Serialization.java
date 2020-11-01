package capsule.helpers;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Serialization {
    protected static final Logger LOGGER = LogManager.getLogger(Serialization.class);

    public static List<Block> deserializeBlockList(String[] blockIds) {
        ArrayList<Block> states = new ArrayList<>();
        ArrayList<String> notfound = new ArrayList<>();

        for (String blockId : blockIds) {
            Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
            if (b != null) {
                states.add(b);
            } else {
                List<Block> blockIdsList = ForgeRegistries.BLOCKS.getValues().stream()
                        .filter(block -> {
                            ResourceLocation registryName = block.getRegistryName();
                            if (registryName == null) return false;
                            return registryName.toString().toLowerCase().contains(blockId.toLowerCase());
                        }).collect(Collectors.toList());
                if (blockIdsList.size() > 0) {
                    states.addAll(blockIdsList);
                } else {
                    notfound.add(blockId);
                }
            }
        }
        if (notfound.size() > 0) {
            LOGGER.debug(String.format(
                    "Blocks not found from config name : %s. Those blocks won't be considered in the overridable or excluded blocks list when capturing with capsule.",
                    String.join(", ", notfound.toArray(new CharSequence[0]))
            ));
        }
        Block[] output = new Block[states.size()];
        return states;
    }

    public static String[] serializeBlockArray(Block[] states) {
        String[] blocksNames = new String[states.length];
        for (int i = 0; i < states.length; i++) {
            ResourceLocation registryName = states[i].getRegistryName();
            blocksNames[i] = registryName == null ? null : registryName.toString();
        }
        return blocksNames;
    }
}
