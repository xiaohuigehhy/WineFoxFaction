package net.xiaohuige_hhy.winefoxfaction.units.goals;

import com.solegendary.reignofnether.unit.goals.UnitRangedAttackGoal;

import net.minecraft.world.entity.Mob;
import net.xiaohuige_hhy.winefoxfaction.network.WineFoxBlueSyncClientboundPacket;

public class WineFoxBlueRangedAttackGoal<T extends Mob> extends UnitRangedAttackGoal<T> {
	
	private final Mob mob;
	
	public WineFoxBlueRangedAttackGoal(T mob, int attackWindupTime) {
		super(mob, attackWindupTime);
		this.mob = mob;
	}
	
	@Override
	public void start() {
		super.start();
		WineFoxBlueSyncClientboundPacket.send(true, this.mob);
	}
	
	@Override
	public void stop() {
		super.stop();
		WineFoxBlueSyncClientboundPacket.send(false, this.mob);
	}
}
