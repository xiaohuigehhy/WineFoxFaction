package net.xiaohuige_hhy.winefoxfaction.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.unit.interfaces.WorkerUnit;

import net.minecraft.server.level.ServerLevel;
import net.xiaohuige_hhy.winefoxfaction.units.WineFoxSalespersonUnit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(BuildingPlacement.class)
public class BuildingPlacementMixin {
	
	@Redirect(method = "handleServerTick", remap = false,
		at = @At(value = "INVOKE", remap = false,
			target = "Ljava/util/ArrayList;size()I"))
	private int bonusBuilderCountForSalesperson(ArrayList<WorkerUnit> workerUnits) {
		int builderCount = workerUnits.size();
		for (WorkerUnit workerUnit : workerUnits) {
			if (workerUnit instanceof WineFoxSalespersonUnit salesperson) {
				if (salesperson.isVeteran())
					builderCount += 1;
			}
		}
		return builderCount;
	}
	
	@Inject(method = "handleServerTick", remap = false,
		at = @At(value = "INVOKE", target = "Lcom/solegendary/reignofnether/building/BuildingPlacement;queueNextBlock(Lnet/minecraft/server/level/ServerLevel;Ljava/lang/String;)V", ordinal = 0))
	private void addSalespersonExp(ServerLevel serverLevel, float blocksPlaced, float blocksTotal, CallbackInfo ci, @Local(name = "count", index = 10, ordinal = 0) int count, @Local(name = "wUnit") WorkerUnit unit) {
		if (count > 1 && unit instanceof WineFoxSalespersonUnit winefox)
			winefox.incrementExp(5);
	}
	
}
