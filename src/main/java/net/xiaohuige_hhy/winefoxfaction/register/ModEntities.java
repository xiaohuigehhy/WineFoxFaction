package net.xiaohuige_hhy.winefoxfaction.register;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xiaohuige_hhy.winefoxfaction.entities.WineFoxSnowball;
import net.xiaohuige_hhy.winefoxfaction.units.WineFoxBlueUnit;
import net.xiaohuige_hhy.winefoxfaction.units.WineFoxLittleUnit;
import net.xiaohuige_hhy.winefoxfaction.units.WineFoxSalespersonUnit;
import net.xiaohuige_hhy.winefoxfaction.units.WineFoxUnit;

import static net.xiaohuige_hhy.winefoxfaction.WineFoxFactionMod.MOD_ID;

public final class ModEntities {
	private ModEntities() { }

	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);

	public static final RegistryObject<EntityType<WineFoxUnit>> WINE_FOX = ENTITIES.register("winefox",
			() -> EntityType.Builder.of(WineFoxUnit::new, MobCategory.CREATURE)
					.sized(0.6f, 1.5f).clientTrackingRange(10).build(MOD_ID + ":winefox"));

	public static final RegistryObject<EntityType<WineFoxBlueUnit>> WINEFOX_BLUE = ENTITIES.register("winefox_blue",
		() -> EntityType.Builder.of(WineFoxBlueUnit::new, MobCategory.CREATURE)
			.sized(0.6f, 1.5f).clientTrackingRange(10).build(MOD_ID + ":winefox_blue"));

	public static final RegistryObject<EntityType<WineFoxSalespersonUnit>> WINEFOX_SALESPERSON = ENTITIES.register("winefox_salesperson",
		() -> EntityType.Builder.of(WineFoxSalespersonUnit::new, MobCategory.CREATURE)
			.sized(0.6f, 1.5f).clientTrackingRange(10).build(MOD_ID + ":winefox_salesperson"));

	public static final RegistryObject<EntityType<WineFoxLittleUnit>> WINEFOX_LITTLE = ENTITIES.register("winefox_little",
		() -> EntityType.Builder.of(WineFoxLittleUnit::new, MobCategory.CREATURE)
			.sized(0.4f, 1.0f).clientTrackingRange(10).build(MOD_ID + ":winefox_little"));

	public static final RegistryObject<EntityType<WineFoxSnowball>> WINEFOX_SNOWBALL = ENTITIES.register("winefox_snowball",
			() -> EntityType.Builder.<WineFoxSnowball>of(WineFoxSnowball::new, MobCategory.MISC)
					.sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10).build(MOD_ID + ":winefox_snowball"));

}
