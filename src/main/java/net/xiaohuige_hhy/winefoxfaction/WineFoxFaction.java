package net.xiaohuige_hhy.winefoxfaction;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.faction.Faction;
import com.solegendary.reignofnether.faction.Factions;
import com.solegendary.reignofnether.registrars.SoundRegistrar;

import net.minecraft.resources.ResourceLocation;
import net.xiaohuige_hhy.winefoxfaction.register.ModEntities;
import net.xiaohuige_hhy.winefoxfaction.register.ModProductionItems;

public class WineFoxFaction {

	public static Faction WINE_FOX;

	public static void register() {
		WINE_FOX = Factions.register("winefox", new Faction()
			.setWorkerIcon(ResourceLocation.fromNamespaceAndPath(ReignOfNether.MOD_ID, "textures/mobheads/villager.png"))
			.setIcon(ResourceLocation.parse("geckolib:textures/maid_icon.png"))
			.setSound(SoundRegistrar.VILLAGER_CALM_THEME_SONG.get())
			.setCustomBuildingCondition((cb) -> cb.buildableByVillagers)
		);
		
		Factions.registerWorkerEntity(WINE_FOX, ModEntities.WINEFOX_SALESPERSON.get(), ModProductionItems.WINE_FOX_SALESPERSON);
		Factions.registerScoutEntity(WINE_FOX, ModEntities.WINEFOX_LITTLE.get(), ModProductionItems.WINE_FOX_LITTLE);
		Factions.registerEntity(WINE_FOX, ModEntities.WINE_FOX.get(), ModProductionItems.WINE_FOX);
		Factions.registerEntity(WINE_FOX, ModEntities.WINEFOX_BLUE.get(), ModProductionItems.WINE_FOX_BLUE);
	}
}
