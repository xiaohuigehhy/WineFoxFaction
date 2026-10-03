package net.xiaohuige_hhy.winefoxfaction.mixin;

import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.interfaces.WorkerUnit;

import net.xiaohuige_hhy.winefoxfaction.units.WineFoxSalespersonUnit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
					builderCount += 2;
				else
					builderCount += 1;
			}
		}
		return builderCount;
	}

	@Redirect(method = "handleServerTick", remap = false,
		at = @At(value = "INVOKE", remap = false,
			target = "Lcom/solegendary/reignofnether/unit/interfaces/Unit;getOwnerName()Ljava/lang/String;"))
	private String giveSalespersonBuilderExp(Unit builder) {
		String ownerName = builder.getOwnerName();
		if (builder instanceof WineFoxSalespersonUnit salesperson) {
			var count = 0;
			for (BuildingPlacement placement : BuildingServerEvents.getBuildings()) {
				if (!placement.ownerName.equals(ownerName)) continue;
				count++;
				if (count <= 1) continue;
				salesperson.incrementExp(1);
				break;
			}
		}
		return ownerName;
	}

}
