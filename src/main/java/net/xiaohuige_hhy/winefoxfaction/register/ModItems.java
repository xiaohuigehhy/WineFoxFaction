package net.xiaohuige_hhy.winefoxfaction.register;

import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static net.xiaohuige_hhy.winefoxfaction.WineFoxFactionMod.MOD_ID;

// 集中管理所有物品的注册
public final class ModItems {
	private ModItems() { }
	
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
	
	public static final RegistryObject<ForgeSpawnEggItem> WINE_FOX_EGG = ITEMS.register("winefox_spawn_egg",
			() -> new ForgeSpawnEggItem(ModEntities.WINE_FOX, 0x722f37, 0xf7c5c5, new Item.Properties()));
	
	public static final RegistryObject<ForgeSpawnEggItem> WINE_FOX_BLUE_EGG = ITEMS.register("winefox_blue_spawn_egg",
			() -> new ForgeSpawnEggItem(ModEntities.WINEFOX_BLUE, 0x722f37, 0xf7c5c5, new Item.Properties()));
	
}
