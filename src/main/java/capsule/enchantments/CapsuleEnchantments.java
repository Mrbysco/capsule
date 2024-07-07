package capsule.enchantments;

import capsule.CapsuleMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class CapsuleEnchantments {
    private static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(BuiltInRegistries.ENCHANTMENT, CapsuleMod.MODID);
    public static final Supplier<Enchantment> RECALL = ENCHANTMENTS.register("recall", RecallEnchant::new);

    public static final Predicate<Entity> hasRecallEnchant = (Entity entityIn) ->
            entityIn instanceof ItemEntity itemEntity && itemEntity.getItem().getEnchantmentLevel(CapsuleEnchantments.RECALL.get()) > 0;

    public static void registerEnchantments(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus);
    }

    public static RecallEnchant CreateRecall() {
        return new RecallEnchant();
    }
}
