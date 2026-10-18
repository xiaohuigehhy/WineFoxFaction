package net.xiaohuige_hhy.winefoxfaction.units;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.solegendary.reignofnether.registrars.AttributeRegistrar;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.unit.EnemySearchBehaviour;
import com.solegendary.reignofnether.unit.UnitAnimationAction;
import com.solegendary.reignofnether.unit.goals.AbstractMeleeAttackUnitGoal;
import com.solegendary.reignofnether.unit.goals.GarrisonGoal;
import com.solegendary.reignofnether.unit.goals.MeleeAttackBuildingGoal;
import com.solegendary.reignofnether.unit.goals.MeleeAttackUnitGoal;
import com.solegendary.reignofnether.unit.goals.MoveToTargetBlockGoal;
import com.solegendary.reignofnether.unit.goals.RandomLookAroundUnitGoal;
import com.solegendary.reignofnether.unit.goals.ReturnResourcesGoal;
import com.solegendary.reignofnether.unit.goals.SelectedTargetGoal;
import com.solegendary.reignofnether.unit.goals.UsePortalGoal;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.packets.UnitAnimationClientboundPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import javax.annotation.Nullable;

public class WineFoxUnit extends AbstractWineFoxUnit implements AttackerUnit {

	public static AttributeSupplier.@NotNull Builder createAttributes() {
		return Mob.createMobAttributes()
			.add(Attributes.MOVEMENT_SPEED, WineFoxUnit.movementSpeed)
			.add(Attributes.ATTACK_DAMAGE, WineFoxUnit.attackDamage)
			.add(Attributes.ARMOR, WineFoxUnit.armorValue)
			.add(Attributes.MAX_HEALTH, WineFoxUnit.maxHealth)
			.add(Attributes.FOLLOW_RANGE, Unit.getFollowRange())
			.add(AttributeRegistrar.ATTACK_DAMAGE.get(), attackDamage)
			.add(AttributeRegistrar.ATTACKS_PER_SECOND.get(), attacksPerSecond)
			.add(AttributeRegistrar.ATTACK_RANGE.get(), attackRange)
			.add(AttributeRegistrar.AGGRO_RANGE.get(), aggroRange)
			.add(AttributeRegistrar.SIGHT_RANGE.get(), Unit.DEFAULT_SIGHT_RANGE)
			.add(AttributeRegistrar.RANGED_DAMAGE_RESIST.get(), rangedDamageResist)
			.add(AttributeRegistrar.MAGIC_DAMAGE_RESIST.get(), 0);
	}

	final static public float attackDamage = 6.0f;
	final static public float attacksPerSecond = 0.5f;
	final static public float maxHealth = 65.0f;
	final static public float armorValue = 0.0f;
	final static public float movementSpeed = 0.28f;
	final static public float attackRange = 2;
	final static public float aggroRange = 10;
	final static public boolean willRetaliate = true;
	final static public boolean aggressiveWhenIdle = true;
	final static public float rangedDamageResist = 0.2f;

	private AbstractMeleeAttackUnitGoal attackGoal;
	private MeleeAttackBuildingGoal attackBuildingGoal;

	private EnemySearchBehaviour attackSearchBehaviour = EnemySearchBehaviour.NONE;
	private BlockPos attackMoveTarget = null;

	public EnemySearchBehaviour getEnemySearchBehaviour() { return attackSearchBehaviour; }
	public void setEnemySearchBehaviour(EnemySearchBehaviour behaviour) { attackSearchBehaviour = behaviour; }

	public Goal getAttackBuildingGoal() {return attackBuildingGoal;}
	public Goal getAttackGoal() {return attackGoal;}

	public BlockPos getAttackMoveTarget() { return attackMoveTarget; }
	public void setAttackMoveTarget(@Nullable BlockPos bp) { this.attackMoveTarget = bp; }

	public boolean getWillRetaliate() {return willRetaliate;}
	public boolean getAggressiveWhenIdle() {return aggressiveWhenIdle && !isVehicle();}
	public float getUnitAttackDamage() {return AttackerUnit.super.getUnitAttackDamage();}

	@Nullable
	public ResourceCost getCost() {return ResourceCosts.VINDICATOR;}

	@Override
	protected String getDefaultModelId() {
		return "geckolib:winefox";
	}

	public boolean canAttackBuildings() {return getAttackBuildingGoal() != null;}

	public WineFoxUnit(EntityType<? extends EntityMaid> entityType, Level level) {
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

	public void initialiseGoals() {
		this.usePortalGoal = new UsePortalGoal(this);
		this.moveGoal = new MoveToTargetBlockGoal(this, false, 0);
		this.targetGoal = new SelectedTargetGoal<>(this, true, true);
		this.garrisonGoal = new GarrisonGoal(this);
		this.attackGoal = new MeleeAttackUnitGoal(this, false);
		this.attackBuildingGoal = new MeleeAttackBuildingGoal(this);
		this.returnResourcesGoal = new ReturnResourcesGoal(this);
	}

	@Override
	protected void registerGoals() {
		initialiseGoals();
		this.goalSelector.addGoal(2, usePortalGoal);

		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(2, attackGoal);
		this.goalSelector.addGoal(2, attackBuildingGoal);
		this.goalSelector.addGoal(2, returnResourcesGoal);
		this.goalSelector.addGoal(2, garrisonGoal);
		this.targetSelector.addGoal(2, targetGoal);
		this.targetSelector.addGoal(3, moveGoal);
		this.goalSelector.addGoal(4, new RandomLookAroundUnitGoal(this));
	}

	@Override
	public void setupEquipmentAndUpgradesServer() {
		Item axe = Items.IRON_SWORD;
		int damageMod = 0;
		ItemStack axeStack = new ItemStack(axe);
		AttributeModifier mod = new AttributeModifier(UUID.randomUUID().toString(), damageMod, AttributeModifier.Operation.ADDITION);
		axeStack.addAttributeModifier(Attributes.ATTACK_DAMAGE, mod, EquipmentSlot.MAINHAND);

		this.setItemSlot(EquipmentSlot.MAINHAND, axeStack);
	}
}
