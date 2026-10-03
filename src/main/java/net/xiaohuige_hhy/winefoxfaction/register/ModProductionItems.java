package net.xiaohuige_hhy.winefoxfaction.register;

import com.solegendary.reignofnether.api.ReignOfNetherRegistries;
import com.solegendary.reignofnether.building.production.ProductionItem;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.xiaohuige_hhy.winefoxfaction.WineFoxFactionMod;
import net.xiaohuige_hhy.winefoxfaction.productions.WineFoxBlueProd;
import net.xiaohuige_hhy.winefoxfaction.productions.WineFoxLittleProd;
import net.xiaohuige_hhy.winefoxfaction.productions.WineFoxProd;
import net.xiaohuige_hhy.winefoxfaction.productions.WineFoxSalespersonProd;

import java.util.List;

public final class ModProductionItems {
	public static final WineFoxProd WINE_FOX = register(ResourceLocation.fromNamespaceAndPath(WineFoxFactionMod.MOD_ID, "winefox"), new WineFoxProd());
	public static final WineFoxBlueProd WINE_FOX_BLUE = register(ResourceLocation.fromNamespaceAndPath(WineFoxFactionMod.MOD_ID, "winefox_blue"), new WineFoxBlueProd());
	public static final WineFoxSalespersonProd WINE_FOX_SALESPERSON = register(ResourceLocation.fromNamespaceAndPath(WineFoxFactionMod.MOD_ID, "winefox_salesperson"), new WineFoxSalespersonProd());
	public static final WineFoxLittleProd WINE_FOX_LITTLE = register(ResourceLocation.fromNamespaceAndPath(WineFoxFactionMod.MOD_ID, "winefox_little"), new WineFoxLittleProd());
	public static final List<ProductionItem> ALL = List.of(
		WINE_FOX,
		WINE_FOX_BLUE,
		WINE_FOX_SALESPERSON,
		WINE_FOX_LITTLE
	);

	private ModProductionItems() {
	}

	private static <T extends ProductionItem> T register(ResourceLocation id, T building) {
		return Registry.register(ReignOfNetherRegistries.PRODUCTION_ITEM, id, building);
	}
}
