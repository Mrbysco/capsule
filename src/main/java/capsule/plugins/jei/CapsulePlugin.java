package capsule.plugins.jei;

import capsule.Config;
import capsule.blocks.CapsuleBlocks;
import capsule.items.CapsuleItem;
import capsule.items.CapsuleItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@JEIPlugin
public class CapsulePlugin implements IModPlugin {

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
        subtypeRegistry.registerSubtypeInterpreter(CapsuleItems.capsule, new CapsuleSubtypeInterpreter());
    }

    @Override
    public void register(@Nonnull IModRegistry registry) {

        // normally you should ignore nbt per-item, but these tags are universally understood
        // and apply to many vanilla and modded items
        List<IRecipe> recipes = new ArrayList<>();

        Ingredient upgradeIngredient = CapsuleItems.upgradedCapsule.getValue().upgradeIngredient;
        for (ItemStack capsule : CapsuleItems.capsuleList.keySet()) {
            for (int upLevel = 1; upLevel < Math.min(8, Config.upgradeLimit); upLevel++) {
                ItemStack capsuleUp = CapsuleItems.getUpgradedCapsule(capsule, upLevel);
                NonNullList<Ingredient> ingredients = NonNullList.withSize(upLevel + 1, upgradeIngredient);
                ingredients.set(0, Ingredient.fromStacks(capsule));
                recipes.add(new ShapelessRecipes("capsule", capsuleUp, ingredients));
            }
            // clear
            recipes.add(new ShapelessRecipes("capsule", capsule, NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(CapsuleItems.getUnlabelledCapsule(capsule)))));
        }

        ItemStack recoveryCapsule = CapsuleItems.recoveryCapsule.getKey();
        ItemStack unlabelled = CapsuleItems.unlabelledCapsule.getKey();
        ItemStack blueprintCapsule = CapsuleItems.blueprintCapsule.getKey();
        ItemStack blueprintCapsule2 = CapsuleItems.blueprintChangedCapsule.getKey();
        Ingredient anyBlueprint = Ingredient.fromStacks(blueprintCapsule, blueprintCapsule2);
        Ingredient unlabelledIng = Ingredient.fromStacks(unlabelled);
        // recovery
        recipes.add(CapsuleItems.recoveryCapsule.getValue().recipe);

        // blueprint
        recipes.add(CapsuleItems.blueprintCapsule.getValue().recipe);
        ItemStack withNewTemplate = CapsuleItems.blueprintChangedCapsule.getKey();
        CapsuleItem.setStructureName(withNewTemplate, "newTemplate");
        CapsuleItem.setLabel(withNewTemplate, "Changed Template");
        recipes.add(new ShapelessRecipes("capsule", withNewTemplate, NonNullList.from(Ingredient.EMPTY, unlabelledIng, anyBlueprint)));

        registry.addRecipes(recipes, VanillaRecipeCategoryUid.CRAFTING);
        registry.addIngredientInfo(new ArrayList<>(CapsuleItems.capsuleList.keySet()), VanillaTypes.ITEM, "jei.capsule.desc.capsule");
        registry.addIngredientInfo(unlabelled, VanillaTypes.ITEM, "jei.capsule.desc.linkedCapsule");
        registry.addIngredientInfo(recoveryCapsule, VanillaTypes.ITEM, "jei.capsule.desc.recoveryCapsule");
        registry.addIngredientInfo(Arrays.asList(anyBlueprint.getMatchingStacks()), VanillaTypes.ITEM, "jei.capsule.desc.blueprintCapsule");
        registry.addIngredientInfo(new ArrayList<>(CapsuleItems.opCapsuleList.keySet()), VanillaTypes.ITEM, "jei.capsule.desc.opCapsule");
        registry.addIngredientInfo(new ItemStack(CapsuleBlocks.blockCapsuleMarker), VanillaTypes.ITEM, "jei.capsule.desc.capsuleMarker");
    }


    private static class CapsuleSubtypeInterpreter implements ISubtypeRegistry.ISubtypeInterpreter {
        @Override
        public String apply(ItemStack itemStack) {
            if (!(itemStack.getItem() instanceof CapsuleItem)) return null;
            String isOP = String.valueOf(itemStack.getTagCompound() != null && itemStack.getTagCompound().hasKey("overpowered") && itemStack.getTagCompound().getBoolean("overpowered"));
            String capsuleState = String.valueOf(itemStack.getItemDamage());
            String capsuleColor = String.valueOf(CapsuleItem.getMaterialColor(itemStack));
            String capsuleBlueprint = String.valueOf(CapsuleItem.isBlueprint(itemStack));
            return capsuleState + capsuleColor + isOP + capsuleBlueprint;
        }
    }
}
