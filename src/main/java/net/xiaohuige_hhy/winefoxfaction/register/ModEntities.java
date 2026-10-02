package net.xiaohuige_hhy.winefoxfaction.register;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xiaohuige_hhy.winefoxfaction.units.WineFoxBlueUnit;
import net.xiaohuige_hhy.winefoxfaction.units.WineFoxUnit;

import static net.xiaohuige_hhy.winefoxfaction.WineFoxFactionMod.MOD_ID;

// 集中管理所有实体（单位）的注册
public final class ModEntities {
	private ModEntities() { }
	
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);
	
	public static final RegistryObject<EntityType<WineFoxUnit>> WINE_FOX = ENTITIES.register("winefox",
			() -> EntityType.Builder.of(WineFoxUnit::new, MobCategory.CREATURE)
					.sized(0.6f, 1.5f).clientTrackingRange(10).build(MOD_ID + ":winefox"));
	
	public static final RegistryObject<EntityType<WineFoxBlueUnit>> WINEFOX_BLUE = ENTITIES.register("winefox_blue",
		() -> EntityType.Builder.of(WineFoxBlueUnit::new, MobCategory.CREATURE)
			.sized(0.6f, 1.5f).clientTrackingRange(10).build(MOD_ID + ":winefox_blue"));
	
}
