package net.xiaohuige_hhy.winefoxfaction.mixin;

import com.solegendary.reignofnether.util.MiscUtil;

import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MiscUtil.class)
public class MiscUtilMixin {

	@Inject(at = @At("TAIL"), method = "getEntityIconName", cancellable = true, remap = false)
	private static void getEntityIconName(Entity entity, CallbackInfoReturnable<String> cir) {
		cir.setReturnValue(cir.getReturnValue().replace("entity.winefoxfaction.", ""));
	}

}
