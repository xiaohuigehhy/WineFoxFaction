package net.xiaohuige_hhy.winefoxfaction.register;

import com.solegendary.reignofnether.resources.ResourceCost;

import net.xiaohuige_hhy.winefoxfaction.ModConfigs;
import net.xiaohuige_hhy.winefoxfaction.WineFoxFactionMod;

public class ModResourceCosts {
	
	private static final String ID = WineFoxFactionMod.MOD_ID;
	
	public static final ResourceCost WINE_FOX = new ResourceCost(ID, "WINE_FOX");
	
	public static final ResourceCost WINE_FOX_BLUE = new ResourceCost(ID, "WINE_FOX_BLUE");
	
	public static void deferredLoadResourceCosts() {
		WINE_FOX.bakeValues(ModConfigs.UnitCosts.WINE_FOX);
		WINE_FOX_BLUE.bakeValues(ModConfigs.UnitCosts.WINE_FOX_BLUE);
	}
	
}
