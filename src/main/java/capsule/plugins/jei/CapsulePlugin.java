package capsule.plugins.jei;

import capsule.CapsuleMod;
import capsule.Config;
import capsule.blocks.CapsuleBlocks;
import capsule.items.CapsuleItem;
import capsule.items.CapsuleItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ByteTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static capsule.items.CapsuleItem.CapsuleState.DEPLOYED;
import static capsule.items.CapsuleItem.CapsuleState.EMPTY;

@JeiPlugin
public class CapsulePlugin implements IModPlugin {

    protected static final Logger LOGGER = LogManager.getLogger(CapsulePlugin.class);

    @Override
    public void registerItemSubtypes(ISubtypeRegistration subtypeRegistry) {
        subtypeRegistry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, CapsuleItems.CAPSULE.get(), new CapsuleSubtypeInterpreter());
    }

    @Override
    public void registerRecipes(@Nonnull IRecipeRegistration registry) {

        // normally you should ignore nbt per-item, but these tags are universally understood
        // and apply to many vanilla and modded items
        List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();

        Ingredient upgradeIngredient = CapsuleItems.upgradedCapsule.getValue().upgradeIngredient;
        for (ItemStack capsule : CapsuleItems.capsuleList.keySet()) {
            for (int upLevel = 1; upLevel < Math.min(8, Config.upgradeLimit); upLevel++) {
                ItemStack capsuleUp = CapsuleItems.getUpgradedCapsule(capsule, upLevel);
                NonNullList<Ingredient> ingredients = NonNullList.withSize(upLevel + 1, upgradeIngredient);
                ingredients.set(0, Ingredient.of(capsule));
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(capsule.getItem());
                recipes.add(new RecipeHolder<>(id, new ShapelessRecipe("capsule", CraftingBookCategory.MISC, capsuleUp, ingredients)));
            }
            // clear
            recipes.add(new RecipeHolder<>(new ResourceLocation(CapsuleMod.MODID, "capsule_clear"),
                    new ShapelessRecipe("capsule", CraftingBookCategory.MISC, capsule, NonNullList.of(Ingredient.EMPTY, Ingredient.of(CapsuleItems.getUnlabelledCapsule(capsule))))));
        }

        if (CapsuleItems.recoveryCapsule == null ||
                CapsuleItems.unlabelledCapsule == null ||
                CapsuleItems.deployedCapsule == null ||
                CapsuleItems.blueprintChangedCapsule == null) {
            LOGGER.error("Some required capsule recipe is missing (recovery, regular capsules or blueprintChanged recipe). The datapack might be corrupted for capsule or the recipe have been remove. JEI won't display the items and capsule might break at some points.");
            return;
        }

        ItemStack recoveryCapsule = CapsuleItems.recoveryCapsule.getKey();
        ItemStack unlabelled = CapsuleItems.unlabelledCapsule.getKey();
        ItemStack unlabelledDeployed = CapsuleItems.deployedCapsule.getKey();
        CapsuleItem.setState(unlabelledDeployed, DEPLOYED);
        Ingredient anyBlueprint = Ingredient.of(CapsuleItems.blueprintCapsules.stream().map(Pair::getKey).toArray(ItemStack[]::new));
        Ingredient unlabelledIng = Ingredient.fromValues(Arrays.asList(Ingredient.of(unlabelled), anyBlueprint, Ingredient.of(recoveryCapsule)).stream().flatMap(i -> Arrays.stream(i.values)));
        // recovery
        recipes.add(new RecipeHolder<>(new ResourceLocation(CapsuleMod.MODID, "recovery_capsule"), CapsuleItems.recoveryCapsule.getValue()));
        for (Pair<ItemStack, CraftingRecipe> r : CapsuleItems.blueprintCapsules) {
            ResourceLocation blueprintId = BuiltInRegistries.ITEM.getKey(r.getKey().getItem());
            recipes.add(new RecipeHolder<>(blueprintId, r.getValue()));
        }
        for (Pair<ItemStack, CraftingRecipe> r : CapsuleItems.blueprintPrefabs) {
            ResourceLocation prefabId = BuiltInRegistries.ITEM.getKey(r.getKey().getItem());
            recipes.add(new RecipeHolder<>(prefabId, r.getValue()));
        }
        ItemStack withNewTemplate = CapsuleItems.blueprintChangedCapsule.getKey();
        CapsuleItem.setStructureName(withNewTemplate, "newTemplate");
        CapsuleItem.setLabel(withNewTemplate, "Changed Template");
        recipes.add((new RecipeHolder<>(new ResourceLocation(CapsuleMod.MODID, "capsule"),
                new ShapelessRecipe("capsule", CraftingBookCategory.MISC, withNewTemplate, NonNullList.of(Ingredient.EMPTY, anyBlueprint, unlabelledIng)))));

        registry.addRecipes(RecipeTypes.CRAFTING, recipes);
        registry.addIngredientInfo(new ArrayList<>(CapsuleItems.capsuleList.keySet()), VanillaTypes.ITEM_STACK, Component.translatable("jei.capsule.desc.capsule"));
        registry.addIngredientInfo(CapsuleItems.blueprintChangedCapsule.getKey(), VanillaTypes.ITEM_STACK, Component.translatable("jei.capsule.desc.blueprintCapsule"));
        registry.addIngredientInfo(CapsuleItems.unlabelledCapsule.getKey(), VanillaTypes.ITEM_STACK, Component.translatable("jei.capsule.desc.linkedCapsule"));
        registry.addIngredientInfo(CapsuleItems.deployedCapsule.getKey(), VanillaTypes.ITEM_STACK, Component.translatable("jei.capsule.desc.linkedCapsule"));
        registry.addIngredientInfo(CapsuleItems.recoveryCapsule.getKey(), VanillaTypes.ITEM_STACK, Component.translatable("jei.capsule.desc.recoveryCapsule"));
        for (Pair<ItemStack, CraftingRecipe> blueprintCapsule : CapsuleItems.blueprintCapsules) {
            registry.addIngredientInfo(blueprintCapsule.getKey(), VanillaTypes.ITEM_STACK, Component.translatable("jei.capsule.desc.blueprintCapsule"));
        }
        for (Pair<ItemStack, CraftingRecipe> blueprintCapsule : CapsuleItems.blueprintPrefabs) {
            registry.addIngredientInfo(blueprintCapsule.getKey(), VanillaTypes.ITEM_STACK, Component.translatable("jei.capsule.desc.blueprintCapsule"));
        }
        ItemStack opCapsule = CapsuleItems.withState(EMPTY);
        opCapsule.addTagElement("overpowered", ByteTag.valueOf(true));
        registry.addIngredientInfo(opCapsule, VanillaTypes.ITEM_STACK, Component.translatable("jei.capsule.desc.opCapsule"));
        registry.addIngredientInfo(new ItemStack(CapsuleBlocks.CAPSULE_MARKER.get()), VanillaTypes.ITEM_STACK, Component.translatable("jei.capsule.desc.capsuleMarker"));
    }

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(CapsuleMod.MODID, "main");
    }


    private static class CapsuleSubtypeInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {

        @Override
        public String apply(ItemStack itemStack, UidContext context) {
            if (!(itemStack.getItem() instanceof CapsuleItem)) return null;
            String isOP = String.valueOf(itemStack.hasTag() && itemStack.getTag().getBoolean("overpowered"));
            String capsuleState = String.valueOf(CapsuleItem.getState(itemStack));
            String capsuleColor = String.valueOf(CapsuleItem.getMaterialColor(itemStack));
            String capsuleBlueprint = String.valueOf(CapsuleItem.isBlueprint(itemStack));
            Component labelComp = CapsuleItem.getLabel(itemStack);
            String label = labelComp == null ? "" : labelComp.getString();
            return capsuleState + capsuleColor + isOP + capsuleBlueprint + label;
        }
    }
}
