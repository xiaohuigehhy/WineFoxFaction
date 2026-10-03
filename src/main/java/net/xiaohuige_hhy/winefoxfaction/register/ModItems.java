package net.xiaohuige_hhy.winefoxfaction.register;

import static net.xiaohuige_hhy.winefoxfaction.WineFoxFactionMod.MOD_ID;

import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xiaohuige_hhy.winefoxfaction.items.WineFoxSnowballItem;

public final class ModItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
	public static final RegistryObject<ForgeSpawnEggItem> WINE_FOX_EGG = ITEMS.register("winefox_spawn_egg",
		() -> new ForgeSpawnEggItem(ModEntities.WINE_FOX, 0x722f37, 0xf7c5c5, new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> WINE_FOX_BLUE_EGG = ITEMS.register("winefox_blue_spawn_egg",
		() -> new ForgeSpawnEggItem(ModEntities.WINEFOX_BLUE, 0x722f37, 0xf7c5c5, new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> WINE_FOX_SALESPERSON_EGG = ITEMS.register("winefox_salesperson_spawn_egg",
		() -> new ForgeSpawnEggItem(ModEntities.WINEFOX_SALESPERSON, 0x722f37, 0xf7c5c5, new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> WINE_FOX_LITTLE_EGG = ITEMS.register("winefox_little_spawn_egg",
		() -> new ForgeSpawnEggItem(ModEntities.WINEFOX_LITTLE, 0x722f37, 0xf7c5c5, new Item.Properties()));
	public static final RegistryObject<WineFoxSnowballItem> WINEFOX_SNOWBALL = ITEMS.register("winefox_snowball",
		() -> new WineFoxSnowballItem(new Item.Properties().stacksTo(16)));
	
	private ModItems() {
	}
	
}
