package net.xiaohuige_hhy.winefoxfaction.units;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.solegendary.reignofnether.fogofwar.FogOfWarClientboundPacket;
import com.solegendary.reignofnether.registrars.AttributeRegistrar;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.unit.EnemySearchBehaviour;
import com.solegendary.reignofnether.unit.goals.GarrisonGoal;
import com.solegendary.reignofnether.unit.goals.MoveToTargetBlockGoal;
import com.solegendary.reignofnether.unit.goals.RandomLookAroundUnitGoal;
import com.solegendary.reignofnether.unit.goals.ReturnResourcesGoal;
import com.solegendary.reignofnether.unit.goals.SelectedTargetGoal;
import com.solegendary.reignofnether.unit.goals.UsePortalGoal;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.RangedAttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
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
import net.xiaohuige_hhy.winefoxfaction.entities.WineFoxSnowball;
import net.xiaohuige_hhy.winefoxfaction.init.ModItems;
import net.xiaohuige_hhy.winefoxfaction.init.ModResourceCosts;
import net.xiaohuige_hhy.winefoxfaction.units.goals.WineFoxBlueRangedAttackGoal;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class WineFoxBlueUnit extends AbstractWineFoxUnit implements AttackerUnit, RangedAttackerUnit {

	final static public float attackDamage = 4.0f;
	final static public float attacksPerSecond = 0.4f;
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

	private WineFoxBlueRangedAttackGoal<? extends LivingEntity> attackGoal;

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
	public ResourceCost getCost() {return ModResourceCosts.WINE_FOX_BLUE;}

	@Override
	protected String getDefaultModelId() {
		return "geckolib:winefox_blue";
	}

	public boolean canAttackBuildings() {return false;}
	
	public boolean isHolding = false;

	public WineFoxBlueUnit(EntityType<? extends EntityMaid> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	public LivingEntity getTarget() {
		return targetGoal.getTarget();
	}

	@Override
	public void resetBehaviours() {
		this.attackGoal.stop();
	}

	public void tick() {
		super.tick();
		AttackerUnit.tick(this);
	}

	public static AttributeSupplier.@NotNull Builder createAttributes() {
		return Mob.createMobAttributes()
			.add(Attributes.MOVEMENT_SPEED, WineFoxBlueUnit.movementSpeed)
			.add(Attributes.ATTACK_DAMAGE, WineFoxBlueUnit.attackDamage)
			.add(Attributes.ARMOR, WineFoxBlueUnit.armorValue)
			.add(Attributes.MAX_HEALTH, WineFoxBlueUnit.maxHealth)
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
		this.attackGoal = new WineFoxBlueRangedAttackGoal<>(this, 0);
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
		this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.WINEFOX_SNOWBALL.get()));
	}

	@Override
	public void performUnitRangedAttack(LivingEntity pTarget, float velocity) {
		if (pTarget == null)
			return;

		this.swing(InteractionHand.MAIN_HAND);

		WineFoxSnowball snowball = new WineFoxSnowball(this.level(), this);
		double d0 = pTarget.getX() - this.getX();
		double d1 = pTarget.getY(0.3333333333333333) - snowball.getY();
		double d2 = pTarget.getZ() - this.getZ();
		double d3 = Math.sqrt(d0 * d0 + d2 * d2);

		if (pTarget.getEyeHeight() <= 1.0f)
			d1 -= (1.0f - pTarget.getEyeHeight());

		snowball.shoot(d0, d1 + d3 * 0.20000000298023224, d2, 1.6F, 0);
		level.addFreshEntity(snowball);
		if (!level().isClientSide() && pTarget instanceof Unit unit)
			FogOfWarClientboundPacket.revealRangedUnit(unit.getOwnerName(), this.getId());
	}
}
