package net.xiaohuige_hhy.winefoxfaction.productions;

import com.solegendary.reignofnether.building.buildings.placements.ProductionPlacement;
import com.solegendary.reignofnether.building.production.IUnitProductionItem;
import com.solegendary.reignofnether.building.production.ProductionItem;
import com.solegendary.reignofnether.building.production.StartProductionButton;
import com.solegendary.reignofnether.building.production.StopProductionButton;
import com.solegendary.reignofnether.hud.buttons.UnitSpawnButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;
import net.xiaohuige_hhy.winefoxfaction.WineFoxFactionMod;
import net.xiaohuige_hhy.winefoxfaction.init.ModEntities;
import net.xiaohuige_hhy.winefoxfaction.init.ModResourceCosts;

import java.util.ArrayList;
import java.util.List;

public class WineFoxMagicalProd extends ProductionItem implements IUnitProductionItem {

	public final static String itemName = "Wine Fox Magical";
	public final static ResourceCost cost = ModResourceCosts.WINE_FOX_MAGICAL;

	public WineFoxMagicalProd() {
		super(cost);
		this.onComplete = (Level level, ProductionPlacement placement) -> {
			if (!level.isClientSide())
				placement.produceUnit((ServerLevel) level, ModEntities.WINEFOX_MAGICAL.get(), placement.ownerName, true);
		};
	}

	public String getItemName() {
		return WineFoxMagicalProd.itemName;
	}

	public UnitSpawnButton getPlaceButton() {
		return new UnitSpawnButton(
			itemName,
			ResourceLocation.fromNamespaceAndPath(WineFoxFactionMod.MOD_ID, "textures/mobheads/winefox_magical.png"),
			List.of(
				Component.translatable("units.winefoxfaction.winefox_magical").withStyle(Style.EMPTY.withBold(true)).getVisualOrderText(),
				FormattedCharSequence.EMPTY,
				Component.translatable("units.winefoxfaction.winefox_magical.tooltip1").getVisualOrderText()
			)
		);
	}

	public StartProductionButton getStartButton(ProductionPlacement prodBuilding, Keybinding hotkey) {
		List<FormattedCharSequence> tooltipLines = new ArrayList<>(List.of(
			Component.translatable("units.winefoxfaction.winefox_magical").withStyle(Style.EMPTY.withBold(true)).getVisualOrderText(),
			ResourceCosts.getFormattedCost(cost),
			ResourceCosts.getFormattedPopAndTime(cost),
			FormattedCharSequence.forward("", Style.EMPTY),
			Component.translatable("units.winefoxfaction.winefox_magical.tooltip1").getVisualOrderText()
		));
		return new StartProductionButton(
			WineFoxMagicalProd.itemName,
			ResourceLocation.fromNamespaceAndPath(WineFoxFactionMod.MOD_ID, "textures/mobheads/winefox_magical.png"),
			hotkey,
			() -> false,
			() -> true,
			tooltipLines,
			this
		);
	}

	public StopProductionButton getCancelButton(ProductionPlacement prodBuilding, boolean first) {
		return new StopProductionButton(
			WineFoxMagicalProd.itemName,
			ResourceLocation.fromNamespaceAndPath(WineFoxFactionMod.MOD_ID, "textures/mobheads/winefox_magical.png"),
			prodBuilding,
			this,
			first
		);
	}
}
