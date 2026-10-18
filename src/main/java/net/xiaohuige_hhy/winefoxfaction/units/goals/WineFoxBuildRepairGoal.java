package net.xiaohuige_hhy.winefoxfaction.units.goals;

import com.solegendary.reignofnether.unit.goals.BuildRepairGoal;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.xiaohuige_hhy.winefoxfaction.units.WineFoxSalespersonUnit;

public class WineFoxBuildRepairGoal extends BuildRepairGoal {
	public WineFoxBuildRepairGoal(Mob mob) {
		super(mob);
	}
	
	@Override
	public void start() {
		super.start();
		if (this.mob.level().isClientSide() || !(this.mob instanceof WineFoxSalespersonUnit winefox))
			return;
		if (!this.isBuilding()) {
			winefox.setHoldingStat(WineFoxSalespersonUnit.HOLD_BUILD);
			winefox.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
		}
	}
	
	@Override
	public void stop() {
		super.stop();
		if (this.mob.level().isClientSide() || !(this.mob instanceof WineFoxSalespersonUnit winefox))
			return;
		if (winefox.getHoldingStat() == WineFoxSalespersonUnit.HOLD_BUILD) {
			winefox.setHoldingStat(WineFoxSalespersonUnit.HOLD_NONE);
			winefox.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
		}
	}
}
