package net.xiaohuige_hhy.winefoxfaction.mixin;

import com.solegendary.reignofnether.building.buildings.placements.ItemShopPlacement;
import com.solegendary.reignofnether.items.ItemClientEvents;
import com.solegendary.reignofnether.items.ItemShopMenu;

import net.minecraft.resources.ResourceLocation;
import net.xiaohuige_hhy.winefoxfaction.WineFoxFactionMod;
import net.xiaohuige_hhy.winefoxfaction.units.IWineFoxUnit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemShopMenu.class)
public class ItemShopMenuMixin {

	@Redirect(method = "renderTitleAndButtons", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/resources/ResourceLocation;fromNamespaceAndPath(Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;",
			ordinal = 0), remap = false)
	private static ResourceLocation redirectIconNamespace(String namespace, String path) {
		ItemShopPlacement shop = ItemClientEvents.openItemShop;
		if (shop != null && shop.getServedUnit() instanceof IWineFoxUnit)
			return ResourceLocation.fromNamespaceAndPath(WineFoxFactionMod.MOD_ID, path);
		return ResourceLocation.fromNamespaceAndPath(namespace, path);
	}

}
