package net.xiaohuige_hhy.winefoxfaction.client;

import com.github.tartaricacid.touhoulittlemaid.api.entity.IMaid;
import com.github.tartaricacid.touhoulittlemaid.client.animation.gecko.condition.ConditionManager;
import com.github.tartaricacid.touhoulittlemaid.client.animation.gecko.condition.ConditionalHold;
import com.github.tartaricacid.touhoulittlemaid.client.animation.gecko.condition.ConditionalSwing;
import com.github.tartaricacid.touhoulittlemaid.client.entity.GeckoMaidEntity;
import com.github.tartaricacid.touhoulittlemaid.compat.gun.common.GunClientUtil;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.PlayState;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.builder.ILoopType;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.event.predicate.AnimationEvent;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.xiaohuige_hhy.winefoxfaction.units.WineFoxSalespersonUnit;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class WineFoxSalesPersonGeckoEntity<T extends Mob> extends AbstractWineFoxGeckoMaidEntity<T> {
	
	public WineFoxSalesPersonGeckoEntity(T mob, IMaid maid) {
		super(mob, maid);
	}
	
	@Override
	protected PlayState winefoxVictoryPredicate(AnimationEvent<GeckoMaidEntity<?>> event) {
		Mob entity = event.getAnimatableEntity().getEntity();
		
		if (entity instanceof WineFoxSalespersonUnit winefox && winefox.isVictory() && winefox.getHoldingStat() == 0 && !winefox.isSwingingArm())
			return playAnimation(event, "game_win", ILoopType.EDefaultLoopTypes.LOOP);
		
		return PlayState.STOP;
	}
	
	protected PlayState winefoxHoldPredicate(AnimationEvent<GeckoMaidEntity<?>> event) {
		Mob entity = event.getAnimatableEntity().getEntity();
		IMaid maid = event.getAnimatableEntity().getMaid();
		if (maid == null) {
			return PlayState.STOP;
		}
		if (!maid.asEntity().swinging && !maid.asEntity().isUsingItem()) {
			ItemStack mainHandItem = maid.asEntity().getItemInHand(InteractionHand.MAIN_HAND);
			PlayState gunHoldAnimation = GunClientUtil.playGunHoldAnimation(mainHandItem, event);
			if (gunHoldAnimation != null) {
				return gunHoldAnimation;
			}
			if (mainHandItem.is(Items.CROSSBOW) && CrossbowItem.isCharged(mainHandItem)) {
				return playAnimation(event, "hold_mainhand:charged_crossbow", ILoopType.EDefaultLoopTypes.LOOP);
			}
			if (maid.hasFishingHook()) {
				return playAnimation(event, "hold_mainhand:fishing", ILoopType.EDefaultLoopTypes.LOOP);
			}
		}
		
		if (entity instanceof WineFoxSalespersonUnit winefox && winefox.getHoldingStat() > WineFoxSalespersonUnit.HOLD_NONE && !winefox.isSwingingArm()) {
			ResourceLocation id = event.getAnimatableEntity().getAnimationFileLocation();
			ConditionalHold conditionalHold = ConditionManager.getHoldMainhand(id);
			if (conditionalHold != null) {
				String name = conditionalHold.doTest(winefox, InteractionHand.MAIN_HAND);
				if (StringUtils.isNoneBlank(name)) {
					return playAnimation(event, name, ILoopType.EDefaultLoopTypes.PLAY_ONCE);
				}
			}
		}
		
		return PlayState.STOP;
	}
	
	protected PlayState winefoxSwingPredicate(@NotNull AnimationEvent<GeckoMaidEntity<?>> event) {
		IMaid maid = event.getAnimatableEntity().getMaid();
		Mob entity = event.getAnimatableEntity().getEntity();
		
		if (maid != null && entity instanceof WineFoxSalespersonUnit winefox && winefox.isSwingingArm()) {
			ResourceLocation id = event.getAnimatableEntity().getAnimationFileLocation();
			ConditionalSwing conditionalSwing = ConditionManager.getSwingMainhand(id);
			if (conditionalSwing != null) {
				String name = conditionalSwing.doTest(maid, InteractionHand.MAIN_HAND);
				if (StringUtils.isNoneBlank(name)) {
					return playAnimation(event, name, ILoopType.EDefaultLoopTypes.LOOP);
				}
			}
			return playAnimation(event, "swing_hand", ILoopType.EDefaultLoopTypes.LOOP);
		}
		
		return PlayState.STOP;
	}
	
}
