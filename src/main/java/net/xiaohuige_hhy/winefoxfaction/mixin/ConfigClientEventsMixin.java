package net.xiaohuige_hhy.winefoxfaction.mixin;

import com.solegendary.reignofnether.config.ConfigClientEvents;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.xiaohuige_hhy.winefoxfaction.register.ModResourceCosts;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConfigClientEvents.class)
public class ConfigClientEventsMixin {
	
	@Inject(method = "onPlayerJoin", at = @At(value = "INVOKE",
			target = "Lcom/solegendary/reignofnether/resources/ResourceCosts;deferredLoadResourceCosts()V"),remap = false)
	private static void injectModResourceCosts(PlayerEvent.PlayerLoggedInEvent evt, CallbackInfo ci) {
		ModResourceCosts.deferredLoadResourceCosts();
	}
}
