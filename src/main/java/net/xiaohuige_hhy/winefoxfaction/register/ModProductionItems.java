package net.xiaohuige_hhy.winefoxfaction.register;

import com.solegendary.reignofnether.api.ReignOfNetherRegistries;
import com.solegendary.reignofnether.building.production.ProductionItem;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.xiaohuige_hhy.winefoxfaction.WineFoxFactionMod;
import net.xiaohuige_hhy.winefoxfaction.productions.WineFoxBlueProd;
import net.xiaohuige_hhy.winefoxfaction.productions.WineFoxProd;

import java.util.List;

public final class ModProductionItems {
	public static final WineFoxProd WINE_FOX = register(ResourceLocation.fromNamespaceAndPath(WineFoxFactionMod.MOD_ID, "winefox"), new WineFoxProd());
	public static final WineFoxBlueProd WINE_FOX_BLUE = register(ResourceLocation.fromNamespaceAndPath(WineFoxFactionMod.MOD_ID, "winefox_blue"), new WineFoxBlueProd());
	public static final List<ProductionItem> ALL = List.of(
		WINE_FOX,
		WINE_FOX_BLUE
	);
	
	private ModProductionItems() {
	}
	
	private static <T extends ProductionItem> T register(ResourceLocation id, T building) {
		return Registry.register(ReignOfNetherRegistries.PRODUCTION_ITEM, id, building);
	}
}
