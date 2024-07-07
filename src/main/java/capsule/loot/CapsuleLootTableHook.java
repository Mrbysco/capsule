package capsule.loot;

import capsule.CapsuleMod;
import capsule.Config;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.LootTableLoadEvent;

@Mod.EventBusSubscriber(modid = CapsuleMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapsuleLootTableHook {
    public static LootPool capsulePool = null;

    public CapsuleLootTableHook() {
    }

    @SubscribeEvent
    public static void hookCapsulesOnLootTable(LootTableLoadEvent event) {

        if (!Config.lootTablesList.contains(event.getName().toString()))
            return;

        // create a capsule loot entry per folder
        if (capsulePool == null) {
            LootPool.Builder capsulePoolBuilder = LootPool.lootPool()
                    .setBonusRolls(ConstantValue.exactly(0))
                    .name("capsulePool")
                    .setRolls(ConstantValue.exactly(1));
            for (Config.LootPathData data : Config.lootTemplatesData.values()) {
                capsulePoolBuilder.add(CapsuleLootEntry.builder(data.path));
            }
            capsulePool = capsulePoolBuilder.build();
        }

        // add a new pool containing all weighted entries
        event.getTable().addPool(capsulePool);
    }
}
