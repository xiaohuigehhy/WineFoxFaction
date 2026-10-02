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
		// 将 WineFoxUnit 注册为该派系的可生产单位
		Factions.registerEntity(WINE_FOX, ModEntities.WINE_FOX.get(), ModProductionItems.WINE_FOX);
		// 将 WineFoxBlueUnit 注册为该派系的可生产单位
		Factions.registerEntity(WINE_FOX, ModEntities.WINEFOX_BLUE.get(), ModProductionItems.WINE_FOX_BLUE);
	}
}
