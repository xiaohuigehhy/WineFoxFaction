package net.xiaohuige_hhy.winefoxfaction.client;

import com.github.tartaricacid.touhoulittlemaid.api.entity.IMaid;
import com.github.tartaricacid.touhoulittlemaid.client.animation.gecko.AnimationManager;
import com.github.tartaricacid.touhoulittlemaid.client.entity.GeckoMaidEntity;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.PlayState;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.builder.AnimationBuilder;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.builder.ILoopType;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.controller.AnimationController;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.event.predicate.AnimationEvent;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.xiaohuige_hhy.winefoxfaction.units.AbstractWineFoxUnit;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public abstract class AbstractWineFoxGeckoMaidEntity<T extends Mob> extends GeckoMaidEntity<T> {

    public AbstractWineFoxGeckoMaidEntity(T mob, IMaid maid) {
        super(mob, maid);
    }

    @Override
    public void registerControllers() {
        AnimationManager manager = AnimationManager.getInstance();
        for (int i = 0; i < 8; i++) {
            String controllerName = String.format("pre_parallel_%d_controller", i);
            String animationName = String.format("pre_parallel%d", i);
            addAnimationController(new AnimationController<>(this, controllerName, 0, e -> manager.predicateParallel(e, animationName)));
        }
        addAnimationController(new AnimationController<>(this, "main", 2, manager::predicateMain));
        addAnimationController(new AnimationController<>(this, "hold_offhand", 0, manager::predicateOffhandHold));
        addAnimationController(new AnimationController<>(this, "hold", 0, this::winefoxHoldPredicate));
        addAnimationController(new AnimationController<>(this, "use", 2, this::winefoxUsePredicate));
        addAnimationController(new AnimationController<>(this, "swing", 2, this::winefoxSwingPredicate));
        addAnimationController(new AnimationController<>(this, "magic_casting", 2, manager::predicateMagicCastingAnimation));
        addAnimationController(new AnimationController<>(this, "misc", 2, manager::predicateMisc));
        addAnimationController(new AnimationController<>(this, "passenger", 2, manager::predicatePassengerAnimation));
        for (int i = 0; i < 8; i++) {
            String controllerName = String.format("parallel_%d_controller", i);
            String animationName = String.format("parallel%d", i);
            addAnimationController(new AnimationController<>(this, controllerName, 0, e -> manager.predicateParallel(e, animationName)));
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                String controllerName = String.format("%s_controller", slot.getName());
                addAnimationController(new AnimationController<GeckoMaidEntity<T>>(this, controllerName, 0, e -> manager.predicateArmor(e, slot)));
            }
        }
        addAnimationController(new AnimationController<>(this, "victory", 0, this::winefoxVictoryPredicate));
    }

    protected PlayState winefoxVictoryPredicate(AnimationEvent<GeckoMaidEntity<?>> event) {
        Mob entity = event.getAnimatableEntity().getEntity();

        if (entity instanceof AbstractWineFoxUnit winefox && winefox.isVictory() && !winefox.isSwingingArms())
            return playAnimation(event, "game_win", ILoopType.EDefaultLoopTypes.LOOP);

        return PlayState.STOP;
    }

    protected PlayState winefoxHoldPredicate(AnimationEvent<GeckoMaidEntity<?>> event) {
        return AnimationManager.getInstance().predicateMainhandHold(event);
    }

    protected PlayState winefoxUsePredicate(AnimationEvent<GeckoMaidEntity<?>> event) {
        return AnimationManager.getInstance().predicateUse(event);
    }

    protected PlayState winefoxSwingPredicate(AnimationEvent<GeckoMaidEntity<?>> event) {
        return AnimationManager.getInstance().predicateSwing(event);
    }
    
    @Nonnull
    protected static PlayState playAnimation(@NotNull AnimationEvent<?> event, String animationName, ILoopType loopType) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation(animationName, loopType));
        return PlayState.CONTINUE;
    }
    
}
