package net.xiaohuige_hhy.winefoxfaction.units;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.projectile.DanmakuShoot;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.solegendary.reignofnether.fogofwar.FogOfWarClientboundPacket;
import com.solegendary.reignofnether.registrars.AttributeRegistrar;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.unit.EnemySearchBehaviour;
import com.solegendary.reignofnether.unit.UnitAnimationAction;
import com.solegendary.reignofnether.unit.goals.GarrisonGoal;
import com.solegendary.reignofnether.unit.goals.MoveToTargetBlockGoal;
import com.solegendary.reignofnether.unit.goals.RandomLookAroundUnitGoal;
import com.solegendary.reignofnether.unit.goals.ReturnResourcesGoal;
import com.solegendary.reignofnether.unit.goals.SelectedTargetGoal;
import com.solegendary.reignofnether.unit.goals.UsePortalGoal;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.ConvertableUnit;
import com.solegendary.reignofnether.unit.interfaces.RangedAttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.packets.UnitAnimationClientboundPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.xiaohuige_hhy.winefoxfaction.init.ModResourceCosts;
import net.xiaohuige_hhy.winefoxfaction.units.goals.WineFoxRangedAttackGoal;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class WineFoxMagicalUnit extends AbstractWineFoxUnit implements AttackerUnit, RangedAttackerUnit, ConvertableUnit {

	final static public float attackDamage = 4.0f;
	final static public float attacksPerSecond = 0.6f;
	final static public float maxHealth = 45.0f;
	final static public float armorValue = 0.0f;
	final static public float movementSpeed = 0.24f;
	final static public float attackRange = 14.0F;
	final static public float aggroRange = 14;
	final static public boolean willRetaliate = true;
	final static public boolean aggressiveWhenIdle = true;

	public int fogRevealDuration = 0;
	public int getFogRevealDuration() { return fogRevealDuration; }
	public void setFogRevealDuration(int duration) { fogRevealDuration = duration; }

	private WineFoxRangedAttackGoal<? extends LivingEntity> attackGoal;

	private EnemySearchBehaviour attackSearchBehaviour = EnemySearchBehaviour.NONE;
	public EnemySearchBehaviour getEnemySearchBehaviour() { return attackSearchBehaviour; }
	public void setEnemySearchBehaviour(EnemySearchBehaviour behaviour) { attackSearchBehaviour = behaviour; }

	private BlockPos attackMoveTarget = null;
	public BlockPos getAttackMoveTarget() { return attackMoveTarget; }
	public void setAttackMoveTarget(@Nullable BlockPos bp) { this.attackMoveTarget = bp; }

	public boolean getWillRetaliate() {return willRetaliate;}
	public boolean getAggressiveWhenIdle() {return aggressiveWhenIdle && !isVehicle();}
	public float getUnitAttackDamage() {return AttackerUnit.super.getUnitAttackDamage();}

	public Goal getAttackBuildingGoal() {return null;}
	public Goal getAttackGoal() {return attackGoal;}

	@Nullable
	public ResourceCost getCost() {return ModResourceCosts.WINE_FOX_MAGICAL;}

	@Override
	protected String getDefaultModelId() {
		return "geckolib:winefox_magical";
	}

	public boolean canAttackBuildings() {return false;}

	public WineFoxMagicalUnit(EntityType<? extends EntityMaid> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	public void setUnitAttackTarget(@Nullable LivingEntity target) {
		AttackerUnit.super.setUnitAttackTarget(target);
		if (!this.level().isClientSide()) {
			if (target != null)
				UnitAnimationClientboundPacket.sendEntityPacket(UnitAnimationAction.NON_KEYFRAME_START, this, target);
			else
				UnitAnimationClientboundPacket.sendBasicPacket(UnitAnimationAction.NON_KEYFRAME_STOP, this);
		}
	}

	@Override
	public LivingEntity getTarget() {
		return targetGoal.getTarget();
	}

	@Override
	public void setAttackBuildingTarget(BlockPos preselectedBlockPos, boolean forced) {
		AttackerUnit.super.setAttackBuildingTarget(preselectedBlockPos, forced);
		if (!this.level().isClientSide())
			UnitAnimationClientboundPacket.sendBlockPosPacket(UnitAnimationAction.NON_KEYFRAME_START, this, preselectedBlockPos);
	}

	@Override
	public void resetBehaviours() {
		if (!this.level().isClientSide())
			UnitAnimationClientboundPacket.sendBasicPacket(UnitAnimationAction.NON_KEYFRAME_STOP, this);
	}

	public void tick() {
		super.tick();
		AttackerUnit.tick(this);
	}

	public static AttributeSupplier.@NotNull Builder createAttributes() {
		return Mob.createMobAttributes()
			.add(Attributes.MOVEMENT_SPEED, WineFoxMagicalUnit.movementSpeed)
			.add(Attributes.ATTACK_DAMAGE, WineFoxMagicalUnit.attackDamage)
			.add(Attributes.ARMOR, WineFoxMagicalUnit.armorValue)
			.add(Attributes.MAX_HEALTH, WineFoxMagicalUnit.maxHealth)
			.add(Attributes.FOLLOW_RANGE, Unit.getFollowRange())
			.add(AttributeRegistrar.ATTACK_DAMAGE.get(), attackDamage)
			.add(AttributeRegistrar.ATTACKS_PER_SECOND.get(), attacksPerSecond)
			.add(AttributeRegistrar.ATTACK_RANGE.get(), attackRange)
			.add(AttributeRegistrar.AGGRO_RANGE.get(), aggroRange)
			.add(AttributeRegistrar.SIGHT_RANGE.get(), Unit.DEFAULT_SIGHT_RANGE)
			.add(AttributeRegistrar.MAGIC_DAMAGE_RESIST.get(), 0);
	}

	public void initialiseGoals() {
		this.usePortalGoal = new UsePortalGoal(this);
		this.moveGoal = new MoveToTargetBlockGoal(this, false, 0);
		this.targetGoal = new SelectedTargetGoal<>(this, true, true);
		this.garrisonGoal = new GarrisonGoal(this);
		this.attackGoal = new WineFoxRangedAttackGoal<>(this, 0);
		this.returnResourcesGoal = new ReturnResourcesGoal(this);
	}

	@Override
	protected void registerGoals() {
		initialiseGoals();
		this.goalSelector.addGoal(2, usePortalGoal);

		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(2, attackGoal);
		this.goalSelector.addGoal(2, returnResourcesGoal);
		this.goalSelector.addGoal(2, garrisonGoal);
		this.targetSelector.addGoal(2, targetGoal);
		this.targetSelector.addGoal(3, moveGoal);
		this.goalSelector.addGoal(4, new RandomLookAroundUnitGoal(this));
	}

	@Override
	public void setupEquipmentAndUpgradesServer() {
		this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(InitItems.HAKUREI_GOHEI.get()));
	}

	@Override
	public void performUnitRangedAttack(LivingEntity pTarget, float velocity) {
		if (pTarget == null)
			return;

		float speed = (0.3f * (velocity + 1)) + Mth.clamp(this.distanceTo(pTarget) / 40f - 0.4f, 0, 2.4f);

		DanmakuShoot.create().setWorld(level).setThrower(this)
			.setTarget(pTarget).setRandomColor().setRandomType()
			.setDamage(getUnitAttackDamage() + 4).setGravity(0)
			.setVelocity(speed).setInaccuracy(0)
			.aimedShot();
		if (!level().isClientSide() && pTarget instanceof Unit unit)
			FogOfWarClientboundPacket.revealRangedUnit(unit.getOwnerName(), this.getId());
	}

	@Override
	public boolean shouldDiscard() {
		return false;
	}

	@Override
	public void setShouldDiscard(boolean shouldDiscard) {

	}
}
