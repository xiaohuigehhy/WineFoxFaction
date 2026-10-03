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
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.xiaohuige_hhy.winefoxfaction.register.ModEntities;
import net.xiaohuige_hhy.winefoxfaction.register.ModItems;
import net.xiaohuige_hhy.winefoxfaction.register.ModResourceCosts;
import net.xiaohuige_hhy.winefoxfaction.units.WineFoxBlueUnit;
import net.xiaohuige_hhy.winefoxfaction.units.WineFoxLittleUnit;
import net.xiaohuige_hhy.winefoxfaction.units.WineFoxSalespersonUnit;
import net.xiaohuige_hhy.winefoxfaction.units.WineFoxUnit;

import org.slf4j.Logger;

@Mod(WineFoxFactionMod.MOD_ID)
public class WineFoxFactionMod {

	public static final String MOD_ID = "winefoxfaction";
	private static final Logger LOGGER = LogUtils.getLogger();

	public WineFoxFactionMod(FMLJavaModLoadingContext context) {
		IEventBus modEventBus = context.getModEventBus();

		ModEntities.ENTITIES.register(modEventBus);
		ModItems.ITEMS.register(modEventBus);

		modEventBus.addListener(WineFoxFactionMod::registerEntityAttributes);
		modEventBus.addListener(WineFoxFactionMod::addCreativeItem);

		modEventBus.addListener(this::commonSetup);
		modEventBus.addListener(this::onLoadComplete);

		MinecraftForge.EVENT_BUS.register(this);
		context.registerConfig(ModConfig.Type.COMMON, ModConfigs.SPEC, "winefoxfactionmod-common-" + ".toml");
	}

	public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
		event.put(ModEntities.WINE_FOX.get(), WineFoxUnit.createAttributes().build());
		event.put(ModEntities.WINEFOX_BLUE.get(), WineFoxBlueUnit.createAttributes().build());
		event.put(ModEntities.WINEFOX_SALESPERSON.get(), WineFoxSalespersonUnit.createAttributes().build());
		event.put(ModEntities.WINEFOX_LITTLE.get(), WineFoxLittleUnit.createAttributes().build());
	}
	
	public static void addCreativeItem(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey().equals(CreativeModeTabs.SPAWN_EGGS)) {
			event.accept(ModItems.WINE_FOX_EGG);
			event.accept(ModItems.WINE_FOX_BLUE_EGG);
			event.accept(ModItems.WINE_FOX_SALESPERSON_EGG);
			event.accept(ModItems.WINE_FOX_LITTLE_EGG);
		}
		if (event.getTabKey().equals(CreativeModeTabs.INGREDIENTS)) {
			event.accept(ModItems.WINEFOX_SNOWBALL);
		}
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		ModResourceCosts.deferredLoadResourceCosts();
	}

	private void onLoadComplete(final FMLLoadCompleteEvent event) {
		event.enqueueWork(WineFoxFaction::register);
	}

	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		LOGGER.info("HELLO from server starting");
	}
}
