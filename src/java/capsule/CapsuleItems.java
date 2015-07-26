package capsule;

import capsule.items.CapsuleCreativeTP;
import capsule.items.CapsuleItem;
import capsule.items.recipes.CapsuleDyeRecipe;
import capsule.items.recipes.RepairRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagInt;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CapsuleItems {
	
	public static Item capsule;
	public static Item creativeTP;
	
	public static void createItems(String modid) {
		GameRegistry.registerItem(creativeTP = new CapsuleCreativeTP("capsule_CTP"), "capsule_CTP");
		GameRegistry.registerItem(capsule = new CapsuleItem("capsule"), "capsule");
		ModelBakery.addVariantName(capsule, modid+":capsule_empty", modid+":capsule_activated", modid+":capsule_linked", modid+":capsule_deployed", modid+":capsule_broken");
    }
	
	public static void registerRenderers(String modid) {
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(CapsuleItems.capsule, 0, new ModelResourceLocation(modid+":capsule_empty", "inventory"));
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(CapsuleItems.capsule, 1, new ModelResourceLocation(modid+":capsule_activated", "inventory"));
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(CapsuleItems.capsule, 2, new ModelResourceLocation(modid+":capsule_linked", "inventory"));
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(CapsuleItems.capsule, 3, new ModelResourceLocation(modid+":capsule_deployed", "inventory"));
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(CapsuleItems.capsule, 4, new ModelResourceLocation(modid+":capsule_broken", "inventory"));
	}
	
	public static void registerRecipes() {
		ItemStack ironCapsule = new ItemStack(CapsuleItems.capsule, 1, CapsuleItem.STATE_EMPTY);
		ironCapsule.setTagInfo("color", new NBTTagInt(0xCCCCCC));
		ironCapsule.setTagInfo("size", new NBTTagInt(3));
		
		ItemStack goldCapsule = new ItemStack(CapsuleItems.capsule, 1, CapsuleItem.STATE_EMPTY);
		goldCapsule.setTagInfo("color", new NBTTagInt(0xFFD700));
		goldCapsule.setTagInfo("size", new NBTTagInt(5));
		
		ItemStack diamondCapsule = new ItemStack(CapsuleItems.capsule, 1, CapsuleItem.STATE_EMPTY);
		diamondCapsule.setTagInfo("color", new NBTTagInt(0x00FFF2));
		diamondCapsule.setTagInfo("size", new NBTTagInt(7));
		
		// base recipes
		GameRegistry.addRecipe(ironCapsule, new Object[] {"   ", "#P#", " # ", '#', Items.iron_ingot, 'P', Items.ender_pearl});
		GameRegistry.addRecipe(goldCapsule, new Object[] {"   ", "#P#", " # ", '#', Items.gold_ingot, 'P', Items.ender_pearl});
		GameRegistry.addRecipe(diamondCapsule, new Object[] {"   ", "#P#", " # ", '#', Items.diamond, 'P', Items.ender_pearl});
		
		// repair recipe
		GameRegistry.addRecipe(new RepairRecipe(new ItemStack(CapsuleItems.capsule, 1, CapsuleItem.STATE_BROKEN), CapsuleItem.STATE_EMPTY));
		
		// dye recipe
		GameRegistry.addRecipe(new CapsuleDyeRecipe());
		
	}
}
