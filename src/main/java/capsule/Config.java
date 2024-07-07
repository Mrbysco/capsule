package capsule;

import capsule.config.SimpleCommentedConfig;
import capsule.helpers.Files;
import capsule.helpers.Serialization;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Config {

    protected static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(Config.class);

    private static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
    public static ModConfigSpec COMMON_CONFIG;

    public static final String CATEGORY_BALANCE = "balance";
    public static final String CATEGORY_LOOT = "loot";
    public static final String CATEGORY_ENCHANTS = "enchants";

    static {
        COMMON_BUILDER.comment(" Check out the wiki pages at https://github.com/Lythom/capsule/wiki for full documentation.\n\nLoot settings").push(CATEGORY_LOOT);
        configureLoot(COMMON_BUILDER);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("enchants settings").push(CATEGORY_ENCHANTS);
        configureEnchants(COMMON_BUILDER);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Balancing settings").push(CATEGORY_BALANCE);
        configureCapture(COMMON_BUILDER);
        COMMON_BUILDER.pop();

        COMMON_CONFIG = COMMON_BUILDER.build();
    }

    // calculated and cached from init
    public static class LootPathData {
        public String path;
        public int weight;
        public List<String> files;

        public LootPathData(String path, int weight) {
            this.path = path;
            this.weight = weight;
        }
    }

    public static Map<String, LootPathData> lootTemplatesData = new HashMap<>();
    public static List<String> starterTemplatesList = new ArrayList<>();
    public static List<String> prefabsTemplatesList = new ArrayList<>();
    public static HashMap<String, JsonObject> blueprintWhitelist = new HashMap<>();
    public static List<Block> excludedBlocks;
    public static List<Block> opExcludedBlocks;

    public static String starterTemplatesPath;
    public static String prefabsTemplatesPath;
    public static String rewardTemplatesPath;
    public static List<? extends String> lootTablesList;
    public static int upgradeLimit;
    public static boolean allowBlueprintReward;
    public static String starterMode;
    public static boolean allowMirror;
    public static int previewDisplayDuration;

    public static ModConfigSpec.ConfigValue<String> enchantRarity;
    public static ModConfigSpec.ConfigValue<String> recallEnchantType;

    // provided by spec
    private static ModConfigSpec.ConfigValue<List<? extends String>> excludedBlocksIdsCfg;
    private static ModConfigSpec.ConfigValue<List<? extends String>> opExcludedBlocksIdsCfg;
    private static ModConfigSpec.ConfigValue<List<CommentedConfig>> lootTemplatesPathsCfg;
    private static ModConfigSpec.ConfigValue<List<? extends String>> lootTablesListCfg;
    private static ModConfigSpec.ConfigValue<String> starterTemplatesPathCfg;
    private static ModConfigSpec.ConfigValue<String> prefabsTemplatesPathCfg;
    private static ModConfigSpec.ConfigValue<String> rewardTemplatesPathCfg;
    private static ModConfigSpec.IntValue upgradeLimitCfg;
    private static ModConfigSpec.BooleanValue allowBlueprintRewardCfg;
    private static ModConfigSpec.BooleanValue allowMirrorCfg;
    private static ModConfigSpec.ConfigValue<String> starterModeCfg;
    private static ModConfigSpec.IntValue previewDisplayDurationCfg;


    public static Path getCapsuleConfigDir() {
        return FMLPaths.CONFIGDIR.get().resolve("capsule");
    }

    public static void bakeConfig(final ModConfig config) {
        // init paths properties from config
        List<CommentedConfig> templatesPaths = Config.lootTemplatesPathsCfg.get();
        for (CommentedConfig rawData : templatesPaths) {
            LootPathData data = new LootPathData(rawData.get("path"), rawData.get("weight"));
            if (!Config.lootTemplatesData.containsKey(data.path)) {
                Config.lootTemplatesData.put(data.path, data);
            }
        }

        Config.opExcludedBlocks = Serialization.deserializeBlockList(opExcludedBlocksIdsCfg.get());
        Config.excludedBlocks = Serialization.deserializeBlockList(excludedBlocksIdsCfg.get());
        Config.lootTablesList = lootTablesListCfg.get();
        Config.starterTemplatesPath = starterTemplatesPathCfg.get();
        Config.prefabsTemplatesPath = prefabsTemplatesPathCfg.get();
        Config.rewardTemplatesPath = rewardTemplatesPathCfg.get();
        Config.upgradeLimit = upgradeLimitCfg.get();
        Config.allowBlueprintReward = allowBlueprintRewardCfg.get();
        Config.starterMode = starterModeCfg.get();
        Config.allowMirror = allowMirrorCfg.get();
        Config.previewDisplayDuration = previewDisplayDurationCfg.get();

        if (CapsuleMod.server != null) {
            populateConfigFolders(CapsuleMod.server);
        }
    }

    public static void populateConfigFolders(MinecraftServer server) {
        ResourceManager ressourceManager = server.getResourceManager();
        Files.populateAndLoadLootList(Config.getCapsuleConfigDir().toFile(), Config.lootTemplatesData, ressourceManager);
        Config.blueprintWhitelist = Files.populateWhitelistConfig(Config.getCapsuleConfigDir().toFile(), ressourceManager);
        Config.starterTemplatesList = Files.populateStarters(Config.getCapsuleConfigDir().toFile(), Config.starterTemplatesPath, ressourceManager);
        Config.prefabsTemplatesList = Files.populatePrefabs(Config.getCapsuleConfigDir().toFile(), Config.prefabsTemplatesPath, ressourceManager);
    }

    public static void configureCapture(ModConfigSpec.Builder configBuild) {

        previewDisplayDurationCfg = configBuild.comment("Duration in ticks for an undeployed capsule to remain activated (preview displayed) when right clicking. 20 ticks = 1 second.\nDefault value: 120.")
                .defineInRange("previewDisplayDuration", 120, 0, Integer.MAX_VALUE);

        // upgrade limits
        upgradeLimitCfg = configBuild.comment("Number of upgrades an empty capsule can get to improve capacity. If <= 0, the capsule won't be able to upgrade.")
                .worldRestart()
                .defineInRange("capsuleUpgradesLimit", 10, 0, Integer.MAX_VALUE);

        // Excluded
        Block[] defaultExcludedBlocksOP = new Block[]{Blocks.AIR, Blocks.STRUCTURE_VOID, Blocks.BEDROCK};
        Block[] defaultExcludedBlocks = new Block[]{Blocks.SPAWNER, Blocks.END_PORTAL, Blocks.END_PORTAL_FRAME};

        String[] excludedBlocksOPArray = ArrayUtils.addAll(
                Serialization.serializeBlockArray(defaultExcludedBlocksOP),
                "ic2:",
                "refinedstorage:",
                "superfactorymanager:",
                "gregtech:machine",
                "gtadditions:",
                "bloodmagic:alchemy_table",
                "mekanism:machineblock",
                "mekanism:boundingblock",
                "tombstone:player_graves"
        );
        String[] excludedBlocksStandardArray = ArrayUtils.addAll(
                Serialization.serializeBlockArray(defaultExcludedBlocks),
                excludedBlocksOPArray
        );
        excludedBlocksIdsCfg = configBuild.comment("List of block ids or tags that will never be captured by a non overpowered capsule. While capturing, the blocks will stay in place.\n Ex block: minecraft:spawner\n Ex tag: minecraft:beds")
                .worldRestart()
                .defineList("excludedBlocks", Arrays.asList(excludedBlocksStandardArray), item -> item instanceof String);

        opExcludedBlocksIdsCfg = configBuild.comment("List of block ids or tags that will never be captured even with an overpowered capsule. While capturing, the blocks will stay in place.\nMod prefix usually indicate an incompatibility, remove at your own risk. See https://github.com/Lythom/capsule/wiki/Known-incompatibilities. \n Ex: minecraft:spawner")
                .worldRestart()
                .defineList("opExcludedBlocks", Arrays.asList(excludedBlocksOPArray), item -> item instanceof String);
    }

    public static void configureLoot(ModConfigSpec.Builder configBuild) {

        // Loot tables that can reward a capsule
        List<String> defaultLootTablesList = Arrays.asList(
                BuiltInLootTables.ABANDONED_MINESHAFT.toString(),
                BuiltInLootTables.BASTION_BRIDGE.toString(),
                BuiltInLootTables.BASTION_HOGLIN_STABLE.toString(),
                BuiltInLootTables.BASTION_OTHER.toString(),
                BuiltInLootTables.BASTION_TREASURE.toString(),
                BuiltInLootTables.SHIPWRECK_TREASURE.toString(),
                BuiltInLootTables.DESERT_PYRAMID.toString(),
                BuiltInLootTables.END_CITY_TREASURE.toString(),
                BuiltInLootTables.IGLOO_CHEST.toString(),
                BuiltInLootTables.JUNGLE_TEMPLE.toString(),
                BuiltInLootTables.SIMPLE_DUNGEON.toString(),
                BuiltInLootTables.STRONGHOLD_CORRIDOR.toString(),
                BuiltInLootTables.STRONGHOLD_CROSSING.toString(),
                BuiltInLootTables.STRONGHOLD_LIBRARY.toString(),
                BuiltInLootTables.VILLAGE_TOOLSMITH.toString(),
                BuiltInLootTables.VILLAGE_ARMORER.toString(),
                BuiltInLootTables.VILLAGE_TEMPLE.toString(),
                BuiltInLootTables.VILLAGE_WEAPONSMITH.toString(),
                BuiltInLootTables.BURIED_TREASURE.toString(),
                BuiltInLootTables.JUNGLE_TEMPLE_DISPENSER.toString(),
                BuiltInLootTables.PILLAGER_OUTPOST.toString(),
                BuiltInLootTables.SHIPWRECK_TREASURE.toString(),
                BuiltInLootTables.UNDERWATER_RUIN_BIG.toString(),
                BuiltInLootTables.UNDERWATER_RUIN_SMALL.toString(),
                BuiltInLootTables.WOODLAND_MANSION.toString());

        Config.lootTablesListCfg = configBuild.comment("List of loot tables that will eventually reward a capsule.\n Example of valid loot tables : gameplay/fishing/treasure, chests/spawn_bonus_chest, entities/villager (killing a villager).\nAlso see https://minecraft.gamepedia.com/Loot_table#List_of_loot_tables.")
                .worldRestart()
                .defineList("lootTablesList", defaultLootTablesList, item -> item instanceof String);

        SimpleCommentedConfig common = new SimpleCommentedConfig(null);
        common.add("path", "config/capsule/loot/common");
        common.add("weight", 10);
        SimpleCommentedConfig uncommon = new SimpleCommentedConfig(null);
        uncommon.add("path", "config/capsule/loot/uncommon");
        uncommon.add("weight", 6);
        SimpleCommentedConfig rare = new SimpleCommentedConfig(null);
        rare.add("path", "config/capsule/loot/rare");
        rare.add("weight", 2);
        List<CommentedConfig> list = new ArrayList<>();
        list.add(common);
        list.add(uncommon);
        list.add(rare);
        Config.lootTemplatesPathsCfg = configBuild.comment("List of paths and weights where the mod will look for structureBlock files. Each .nbt or .schematic in those folders have a chance to appear as a reward capsule in a dungeon chest.\nHigher weight means more common. Default weights : 2 (rare), 6 (uncommon) or 10 (common)\nTo Lower the chance of getting a capsule at all, insert an empty folder here and configure its weight accordingly.")
                .worldRestart()
                .define("lootTemplatesPaths", list, o -> o instanceof ArrayList);

        Config.starterModeCfg = configBuild.comment("Players can be given one or several starter structures on their first arrival.\nThose structures nbt files can be placed in the folder defined at starterTemplatesPath below.\nPossible values : \"all\", \"random\", or \"none\".\nDefault value: \"random\"")
                .worldRestart()
                .define("starterMode", "random");

        Config.starterTemplatesPathCfg = configBuild.comment("Each structure in this folder will be given to the player as standard reusable capsule on game start.\nEmpty the folder or the value to disable starter capsules.\nDefault value: \"config/capsule/starters\"")
                .worldRestart()
                .define("starterTemplatesPath", "config/capsule/starters");

        Config.prefabsTemplatesPathCfg = configBuild.comment("Each structure in this folder will auto-generate a blueprint recipe that player will be able to craft.\nRemove/Add structure in the folder to disable/enable the recipe.\nDefault value: \"config/capsule/prefabs\"")
                .worldRestart()
                .define("prefabsTemplatesPath", "config/capsule/prefabs");

        Config.rewardTemplatesPathCfg = configBuild.comment("Paths where the mod will look for structureBlock files when invoking command /capsule fromExistingRewards <structureName> [playerName].")
                .worldRestart()
                .define("rewardTemplatesPath", "config/capsule/rewards");

        Config.allowBlueprintRewardCfg = configBuild.comment("If true, loot rewards will be pre-charged blueprint when possible (if the content contains no entity).\nIf false loot reward will always be one-use capsules.\nDefault value: true")
                .define("allowBlueprintReward", true);

        Config.allowMirrorCfg = configBuild.comment("If true, sneak+left click in air allow mirroring of the capsule content. Can be disabled for multiblock compatibility. \nDefault value: true")
                .define("allowMirror", true);
    }

    public static void configureEnchants(ModConfigSpec.Builder configBuild) {

        Config.enchantRarity = configBuild.comment("Rarity of the enchantmant. Possible values : COMMON, UNCOMMON, RARE, VERY_RARE. Default: RARE.")
                .worldRestart()
                .define("recallEnchantRarity", "RARE");

        Config.recallEnchantType = configBuild.comment("Possible targets for the enchantment. By default : null.\nPossible values are ALL, ARMOR, ARMOR_FEET, ARMOR_LEGS, ARMOR_TORSO, ARMOR_HEAD, WEAPON, DIGGER, FISHING_ROD, BREAKABLE, BOW, null.\nIf null or empty, Capsules will be the only items to be able to get this Enchantment.")
                .worldRestart()
                .define("recallEnchantType", "null");
    }


    public static String getRewardPathFromName(String structureName) {
        return rewardTemplatesPath + "/" + structureName;
    }

    public static JsonObject getBlueprintAllowedNBT(Block b) {
        return blueprintWhitelist.get(BuiltInRegistries.BLOCK.getKey(b).toString());
    }

    /**
     * Identity NBT is NBT that is required to identify the item as a specific block. Ie. immersive engineering conveyor belts differs by their nbt but use the same block/item class.
     */
    public static List<String> getBlueprintIdentityNBT(Block b) {
        JsonObject allowedNBT = getBlueprintAllowedNBT(b);
        if (allowedNBT == null) return null;
        return allowedNBT.entrySet().stream()
                .filter(ks -> !ks.getValue().isJsonNull())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
