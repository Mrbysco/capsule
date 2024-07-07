package capsule.helpers;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Serialization {
    protected static final Logger LOGGER = LogManager.getLogger(Serialization.class);

    public static List<Block> deserializeBlockList(List<? extends String> blockIds) {
        ArrayList<Block> states = new ArrayList<>();
        ArrayList<String> notfound = new ArrayList<>();

        for (String blockId : blockIds) {
            ResourceLocation excludedLocation = new ResourceLocation(blockId);
            // is it a whole registryName to exclude ?
            if (StringUtil.isNullOrEmpty(excludedLocation.getPath())) {
                List<Block> blockIdsList = BuiltInRegistries.BLOCK.entrySet().stream()
                        .filter(blockEntry -> blockEntry.getKey().toString().toLowerCase().contains(blockId.toLowerCase()))
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toList());
                if (blockIdsList.size() > 0) {
                    states.addAll(blockIdsList);
                } else {
                    notfound.add(blockId);
                }
            } else {
                // is it a block ?
                Block b = BuiltInRegistries.BLOCK.get(excludedLocation);
                if (b != null) {
                    // exclude the block
                    states.add(b);
                } else {
                    // is it a tag ?
                    Optional<HolderSet.Named<Block>> tag = BuiltInRegistries.BLOCK.getTag(TagKey.create(Registries.BLOCK, excludedLocation));
                    if (tag.isPresent()) {
                        // get all blocks concerned by tag
                        List<Block> blockIdsList = new ArrayList<>();
                        tag.get().forEach((holder) -> blockIdsList.add(holder.value()));
                        states.addAll(blockIdsList);
                    } else {
                        notfound.add(excludedLocation.toString());
                    }
                }
            }
        }
        if (notfound.size() > 0) {
            LOGGER.info(String.format(
                    "Blocks couldn't be resolved as Block or Tag from config name : %s. Those blocks won't be considered in the overridable or excluded blocks list when capturing with capsule.",
                    String.join(", ", notfound.toArray(new CharSequence[0]))
            ));
        }

        Block[] output = new Block[states.size()];
        return states;
    }

    public static String[] serializeBlockArray(Block[] states) {
        String[] blocksNames = new String[states.length];
        for (int i = 0; i < states.length; i++) {
            ResourceLocation registryName = BuiltInRegistries.BLOCK.getKey(states[i]);
            blocksNames[i] = registryName == null ? null : registryName.toString();
        }
        return blocksNames;
    }
}
