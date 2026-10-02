package net.xiaohuige_hhy.winefoxfaction;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.xiaohuige_hhy.winefoxfaction.register.ModEntities;
import net.xiaohuige_hhy.winefoxfaction.register.ModItems;
import net.xiaohuige_hhy.winefoxfaction.register.ModResourceCosts;
import net.xiaohuige_hhy.winefoxfaction.units.WineFoxBlueUnit;
import net.xiaohuige_hhy.winefoxfaction.units.WineFoxUnit;

import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(WineFoxFactionMod.MOD_ID)
public class WineFoxFactionMod {
	
	// Define mod id in a common place for everything to reference
	public static final String MOD_ID = "winefoxfaction";
	// Directly reference a slf4j logger
	private static final Logger LOGGER = LogUtils.getLogger();
	
	public WineFoxFactionMod(FMLJavaModLoadingContext context) {
		IEventBus modEventBus = context.getModEventBus();
		
		ModEntities.ENTITIES.register(modEventBus);
		ModItems.ITEMS.register(modEventBus);
		
		// Register the entity attributes and creative tab contents
		// （生产项通过 ModProductionItems 的静态字段注册，并由 mixin 并入 ProductionItems.ALL）
		modEventBus.addListener(WineFoxFactionMod::registerEntityAttributes);
		modEventBus.addListener(WineFoxFactionMod::addCreativeItem);
		
		// Register the commonSetup method for modloading
		modEventBus.addListener(this::commonSetup);
		
		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);
		context.registerConfig(ModConfig.Type.COMMON, ModConfigs.SPEC, "winefoxfactionmod-common-" + ".toml");
	}
	
	public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
		event.put(ModEntities.WINE_FOX.get(), WineFoxUnit.createAttributes().build());
		event.put(ModEntities.WINEFOX_BLUE.get(), WineFoxBlueUnit.createAttributes().build());
	}
	
	public static void addCreativeItem(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey().equals(CreativeModeTabs.SPAWN_EGGS)) {
			event.accept(ModItems.WINE_FOX_EGG);
			event.accept(ModItems.WINE_FOX_BLUE_EGG);
		}
	}
	
	private void commonSetup(final FMLCommonSetupEvent event) {
		ModResourceCosts.deferredLoadResourceCosts();
		event.enqueueWork(WineFoxFaction::register);
	}
	
	// You can use SubscribeEvent and let the Event Bus discover methods to call
	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		// Do something when the server starts
		LOGGER.info("HELLO from server starting");
	}
}
