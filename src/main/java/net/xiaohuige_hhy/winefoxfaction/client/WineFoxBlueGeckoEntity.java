package net.xiaohuige_hhy.winefoxfaction.client;

import com.github.tartaricacid.touhoulittlemaid.api.entity.IMaid;
import com.github.tartaricacid.touhoulittlemaid.client.entity.GeckoMaidEntity;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.PlayState;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.builder.ILoopType;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.event.predicate.AnimationEvent;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.xiaohuige_hhy.winefoxfaction.init.ModItems;
import net.xiaohuige_hhy.winefoxfaction.units.WineFoxBlueUnit;

public class WineFoxBlueGeckoEntity<T extends Mob> extends AbstractWineFoxGeckoMaidEntity<T> {
	
	public WineFoxBlueGeckoEntity(T mob, IMaid maid) {
		super(mob, maid);
	}
	
	@Override
	protected PlayState winefoxVictoryPredicate(AnimationEvent<GeckoMaidEntity<?>> event) {
		Mob entity = event.getAnimatableEntity().getEntity();
		
		if (entity instanceof WineFoxBlueUnit winefox && winefox.isVictory() && !winefox.isHolding && !winefox.swinging)
			return playAnimation(event, "game_win", ILoopType.EDefaultLoopTypes.LOOP);
		
		return PlayState.STOP;
	}
	
	protected PlayState winefoxHoldPredicate(AnimationEvent<GeckoMaidEntity<?>> event) {
		Mob entity = event.getAnimatableEntity().getEntity();
		
		if (entity instanceof WineFoxBlueUnit winefox && winefox.isHolding) {
			return playAnimation(event, "hold_mainhand$minecraft:snowball", ILoopType.EDefaultLoopTypes.LOOP);
		}
		
		return super.winefoxHoldPredicate(event);
	}
	
	protected PlayState winefoxSwingPredicate(AnimationEvent<GeckoMaidEntity<?>> event) {
		Mob entity = event.getAnimatableEntity().getEntity();
		
		ItemStack itemInHand = entity.getItemInHand(InteractionHand.MAIN_HAND);
		if (entity.swinging) {
			if (entity.swingTime == 0)
				playAnimation(event, "empty", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
			if (itemInHand.is(ModItems.WINEFOX_SNOWBALL.get()))
				return playAnimation(event, "swing$minecraft:snowball", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
		}
		
		return super.winefoxSwingPredicate(event);
	}
	
}
