package net.xiaohuige_hhy.winefoxfaction.units.goals;

import com.solegendary.reignofnether.unit.goals.UnitRangedAttackGoal;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class WineFoxRangedAttackGoal<T extends Mob> extends UnitRangedAttackGoal<T> {
	
	private final Mob mob;
	
	public WineFoxRangedAttackGoal(T mob, int attackWindupTime) {
		super(mob, attackWindupTime);
		this.mob = mob;
	}
	
	@Override
	public void tick() {
		if (!this.mob.isUsingItem()) {
			LivingEntity target = this.mob.getTarget();
			if (target != null && target.isAlive()) {
				this.mob.startUsingItem(InteractionHand.MAIN_HAND);
			}
		}
		super.tick();
	}
	
	@Override
	public void stop() {
		super.stop();
		if (this.mob.isUsingItem())
			this.mob.stopUsingItem();
	}
}
