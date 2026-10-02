package net.xiaohuige_hhy.winefoxfaction.mixin;

import com.solegendary.reignofnether.hud.ControlGroup;
import com.solegendary.reignofnether.hud.HudClientEvents;

import net.minecraft.resources.ResourceLocation;
import net.xiaohuige_hhy.winefoxfaction.WineFoxFactionMod;
import net.xiaohuige_hhy.winefoxfaction.units.IWineFoxUnit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ControlGroup.class)
public class ControlGroupMixin {

	@Redirect(method = "saveFromSelected", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/resources/ResourceLocation;fromNamespaceAndPath(Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"), remap = false)
	private ResourceLocation redirectIconNamespace(String namespace, String path) {
		if (HudClientEvents.hudSelectedEntity instanceof IWineFoxUnit)
			return ResourceLocation.fromNamespaceAndPath(WineFoxFactionMod.MOD_ID, path);
		return ResourceLocation.fromNamespaceAndPath(namespace, path);
	}

}
