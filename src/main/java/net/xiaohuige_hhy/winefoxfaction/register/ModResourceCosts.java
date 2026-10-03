package net.xiaohuige_hhy.winefoxfaction.register;

import com.solegendary.reignofnether.resources.ResourceCost;

import net.xiaohuige_hhy.winefoxfaction.ModConfigs;
import net.xiaohuige_hhy.winefoxfaction.WineFoxFactionMod;

public class ModResourceCosts {

	private static final String ID = WineFoxFactionMod.MOD_ID;

	public static final ResourceCost WINE_FOX = new ResourceCost(ID, "WINE_FOX");

	public static final ResourceCost WINE_FOX_BLUE = new ResourceCost(ID, "WINE_FOX_BLUE");

	public static final ResourceCost WINE_FOX_SALESPERSON = new ResourceCost(ID, "WINE_FOX_SALESPERSON");

	public static final ResourceCost WINE_FOX_LITTLE = new ResourceCost(ID, "WINE_FOX_LITTLE");

	public static void deferredLoadResourceCosts() {
		WINE_FOX.bakeValues(ModConfigs.UnitCosts.WINE_FOX);
		WINE_FOX_BLUE.bakeValues(ModConfigs.UnitCosts.WINE_FOX_BLUE);
		WINE_FOX_SALESPERSON.bakeValues(ModConfigs.UnitCosts.WINE_FOX_SALESPERSON);
		WINE_FOX_LITTLE.bakeValues(ModConfigs.UnitCosts.WINE_FOX_LITTLE);
	}

}
