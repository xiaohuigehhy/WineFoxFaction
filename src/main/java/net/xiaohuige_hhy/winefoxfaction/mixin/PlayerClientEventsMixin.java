package net.xiaohuige_hhy.winefoxfaction.mixin;

import com.solegendary.reignofnether.player.PlayerClientEvents;

import net.xiaohuige_hhy.winefoxfaction.events.WineFoxEvents;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerClientEvents.class)
public class PlayerClientEventsMixin {

	@Inject(method = "victory", at = @At("HEAD"), remap = false)
	private static void injectWineFoxVictoryStart(String playerName, CallbackInfo ci) {
		WineFoxEvents.startWineFoxVictoryAnimation(playerName);
	}

	@Inject(method = "resetRTS", at = @At("HEAD"), remap = false)
	private static void injectWineFoxVictoryStop(boolean hardReset, CallbackInfo ci) {
		WineFoxEvents.stopWineFoxVictoryAnimation();
	}
}
