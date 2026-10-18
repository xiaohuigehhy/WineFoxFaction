package net.xiaohuige_hhy.winefoxfaction.client;

import com.github.tartaricacid.touhoulittlemaid.api.entity.IMaid;

import net.minecraft.world.entity.Mob;

public class WineFoxGeckoMaidEntity<T extends Mob> extends AbstractWineFoxGeckoMaidEntity<T> {
	
	public WineFoxGeckoMaidEntity(T mob, IMaid maid) {
		super(mob, maid);
	}
	
}
