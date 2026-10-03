package net.xiaohuige_hhy.winefoxfaction;

import com.solegendary.reignofnether.config.ReignOfNetherCommonConfigs;
import com.solegendary.reignofnether.config.ResourceCostConfigEntry;

import net.minecraftforge.common.ForgeConfigSpec;
import net.xiaohuige_hhy.winefoxfaction.register.ModResourceCosts;

public class ModConfigs {

	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	static {
		BUILDER.push("Configuration File");
		BUILDER.pop();
		BUILDER.comment("Unit cost configurations");
		UnitCosts.WINE_FOX.define(BUILDER);
		UnitCosts.WINE_FOX_BLUE.define(BUILDER);
		UnitCosts.WINE_FOX_SALESPERSON.define(BUILDER);
		UnitCosts.WINE_FOX_LITTLE.define(BUILDER);
		SPEC = BUILDER.build();
	}

	public static class UnitCosts implements ReignOfNetherCommonConfigs.Costs {
		public static final ResourceCostConfigEntry WINE_FOX = ResourceCostConfigEntry.Unit(50, 0, 100, 35, 2, ModResourceCosts.WINE_FOX, "Wine Fox Config");
		public static final ResourceCostConfigEntry WINE_FOX_BLUE = ResourceCostConfigEntry.Unit(50, 0, 125, 35, 2, ModResourceCosts.WINE_FOX_BLUE, "Wine Fox Blue Config");
		public static final ResourceCostConfigEntry WINE_FOX_SALESPERSON = ResourceCostConfigEntry.Unit(50, 0, 50, 15, 1, ModResourceCosts.WINE_FOX_SALESPERSON, "Wine Fox Salesperson Config");
		public static final ResourceCostConfigEntry WINE_FOX_LITTLE = ResourceCostConfigEntry.Unit(50, 0, 40, 15, 1, ModResourceCosts.WINE_FOX_LITTLE, "Wine Fox Little Config");
	}

}
