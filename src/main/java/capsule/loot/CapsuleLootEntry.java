package capsule.loot;

import capsule.Config;
import capsule.StructureSaver;
import capsule.helpers.Capsule;
import capsule.helpers.Files;
import capsule.items.CapsuleItem;
import capsule.structure.CapsuleTemplate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.Consumer;

import static capsule.items.CapsuleItem.CapsuleState.BLUEPRINT;

/**
 * @author Lythom
 */
public class CapsuleLootEntry extends LootPoolSingletonContainer {

    public static final int DEFAULT_WEIGHT = 3;
    public static String[] COLOR_PALETTE = new String[]{
            "0xCCCCCC", "0x549b57", "0xe08822", "0x5e8eb7", "0x6c6c6c", "0xbd5757", "0x99c33d", "0x4a4cba", "0x7b2e89", "0x95d5e7", "0xffffff"
    };
    private static final Random random = new Random();
    private String templatesPath = null;

    public static LootPoolEntryContainer.Builder<?> builder(String templatePath) {
        return simpleBuilder((p_216169_1_, p_216169_2_, p_216169_3_, p_216169_4_) -> {
            int weight = findConfiguredWeight(templatePath);
            return new CapsuleLootEntry(templatePath, weight);
        });
    }

    public static int findConfiguredWeight(String path) {
        int weight = DEFAULT_WEIGHT;
        if (Config.lootTemplatesData.containsKey(path)) {
            weight = Config.lootTemplatesData.get(path).weight;
        }
        return weight;
    }

    /**
     * @param templatesPath
     * @param weightIn
     */
    protected CapsuleLootEntry(String templatesPath, int weightIn) {
        super(weightIn, 0, new ArrayList<>(), new ArrayList<>());
        this.templatesPath = templatesPath;
    }

    public static int getRandomColor() {
        return Integer.decode(COLOR_PALETTE[(int) (Math.random() * COLOR_PALETTE.length)]);
    }

    /**
     * Add all eligible capsuleList to the list to be picked from.
     */
    @Override
    public void createItemStack(Consumer<ItemStack> stacks, LootContext context) {
        if (this.templatesPath == null) return;

        if (Config.lootTemplatesData.containsKey(this.templatesPath)) {

            Pair<String, CapsuleTemplate> templatePair = getRandomTemplate(context);

            if (templatePair != null) {
                CapsuleTemplate template = templatePair.getRight();
                String templatePath = templatePair.getLeft();
                int size = Math.max(template.getSize().getX(), Math.max(template.getSize().getY(), template.getSize().getZ()));

                if (template.entities.isEmpty() && Config.allowBlueprintReward) {
                    // blueprint if there is no entities in the capsule
                    ItemStack capsule = Capsule.newLinkedCapsuleItemStack(
                            templatePath,
                            getRandomColor(),
                            getRandomColor(),
                            size,
                            false,
                            Capsule.labelFromPath(templatePath),
                            0);
                    CapsuleItem.setAuthor(capsule, template.getAuthor());
                    CapsuleItem.setState(capsule, BLUEPRINT);
                    CapsuleItem.setBlueprint(capsule);
                    CapsuleItem.setCanRotate(capsule, template.canRotate());
                    stacks.accept(capsule);
                } else {
                    // one use if there are entities and a risk of dupe
                    ItemStack capsule = Capsule.newRewardCapsuleItemStack(
                            templatePath,
                            getRandomColor(),
                            getRandomColor(),
                            size,
                            Capsule.labelFromPath(templatePath),
                            template.getAuthor());
                    CapsuleItem.setCanRotate(capsule, template.canRotate());
                    stacks.accept(capsule);
                }
            }

        }

    }

    @Nullable
    public Pair<String, CapsuleTemplate> getRandomTemplate(LootContext context) {
        Config.LootPathData lpd = Config.lootTemplatesData.get(this.templatesPath);
        if (lpd == null || lpd.files == null) {
            Files.populateAndLoadLootList(Config.getCapsuleConfigDir().toFile(), Config.lootTemplatesData, context.getLevel().getServer().getResourceManager());
            lpd = Config.lootTemplatesData.get(this.templatesPath);
        }
        if (lpd == null || lpd.files == null || lpd.files.isEmpty()) return null;

        int size = lpd.files.size();
        int initRand = random.nextInt(size);

        for (int i = 0; i < lpd.files.size(); i++) {
            int ri = (initRand + i) % lpd.files.size();
            String structureName = lpd.files.get(ri);
            CapsuleTemplate template = StructureSaver.getTemplateForReward(context.getLevel().getServer(), this.templatesPath + "/" + structureName).getRight();
            if (template != null) return Pair.of(this.templatesPath + "/" + structureName, template);
        }
        return null;
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.REFERENCE;
    }
}
