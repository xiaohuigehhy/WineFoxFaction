package net.xiaohuige_hhy.winefoxfaction.mixin;

import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.hud.buttons.ButtonBuilder;
import com.solegendary.reignofnether.util.MiscUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.xiaohuige_hhy.winefoxfaction.WineFoxFactionMod;
import net.xiaohuige_hhy.winefoxfaction.units.IWineFoxUnit;
import net.xiaohuige_hhy.winefoxfaction.units.WineFoxSalespersonUnit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HudClientEvents.class)
public class HudClientEventsMixin {

	@Unique
	private static LivingEntity winefoxfaction$currentUnit = null;

	@Redirect(method = "onDrawScreen", at = @At(value = "INVOKE",
			target = "Lcom/solegendary/reignofnether/hud/buttons/ButtonBuilder;entity(Lnet/minecraft/world/entity/LivingEntity;)Lcom/solegendary/reignofnether/hud/buttons/ButtonBuilder;"), remap = false)
	private static ButtonBuilder redirectUnitEntity(ButtonBuilder builder, LivingEntity entity) {
		winefoxfaction$currentUnit = entity;
		builder.entity(entity);

		if (entity instanceof IWineFoxUnit) {
			String unitName = MiscUtil.getEntityIconName(entity);
			String buttonImagePath = entity.isVehicle()
					? "textures/mobheads/" + unitName + "_half.png"
					: "textures/mobheads/" + unitName + ".png";
			builder.iconResource(ResourceLocation.fromNamespaceAndPath(WineFoxFactionMod.MOD_ID, buttonImagePath));
		}

		return builder;
	}

	@Redirect(method = "onDrawScreen", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/resources/ResourceLocation;fromNamespaceAndPath(Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;",
			ordinal = 3), remap = false)
	private static ResourceLocation redirectBgIconNamespace(String namespace, String path) {
		if (winefoxfaction$currentUnit != null && winefoxfaction$currentUnit.getFirstPassenger() instanceof IWineFoxUnit)
			return ResourceLocation.fromNamespaceAndPath(WineFoxFactionMod.MOD_ID, path);
		return ResourceLocation.fromNamespaceAndPath(namespace, path);
	}
	
	@Inject(method = "getModifiedEntityName", at = @At("RETURN"), remap = false, cancellable = true)
	private static void getWineFoxSalespersonName(LivingEntity entity, CallbackInfoReturnable<String> cir) {
		if (entity instanceof WineFoxSalespersonUnit winefox)
			if (winefox.isVeteran)
				cir.setReturnValue(Component.translatable("units.winefoxfaction.winefox_salesperson_veteran").getString());
			else 
				cir.setReturnValue(Component.translatable("units.winefoxfaction.winefox_salesperson").getString());
	}

}
