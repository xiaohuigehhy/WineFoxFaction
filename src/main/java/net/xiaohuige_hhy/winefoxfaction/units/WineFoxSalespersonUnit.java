package net.xiaohuige_hhy.winefoxfaction.units;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.solegendary.reignofnether.building.production.ProductionItems;
import com.solegendary.reignofnether.faction.Factions;
import com.solegendary.reignofnether.hud.buttons.Button;
import com.solegendary.reignofnether.registrars.AttributeRegistrar;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.unit.EnemySearchBehaviour;
import com.solegendary.reignofnether.unit.goals.AbstractMeleeAttackUnitGoal;
import com.solegendary.reignofnether.unit.goals.BuildRepairGoal;
import com.solegendary.reignofnether.unit.goals.ExploreBuildLocationGoal;
import com.solegendary.reignofnether.unit.goals.GarrisonGoal;
import com.solegendary.reignofnether.unit.goals.GatherResourcesGoal;
import com.solegendary.reignofnether.unit.goals.MeleeAttackUnitGoal;
import com.solegendary.reignofnether.unit.goals.MoveToTargetBlockGoal;
import com.solegendary.reignofnether.unit.goals.RandomLookAroundUnitGoal;
import com.solegendary.reignofnether.unit.goals.ReturnResourcesGoal;
import com.solegendary.reignofnether.unit.goals.SelectedTargetGoal;
import com.solegendary.reignofnether.unit.goals.UsePortalGoal;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.interfaces.WorkerUnit;
import com.solegendary.reignofnether.unit.packets.UnitSyncClientboundPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.xiaohuige_hhy.winefoxfaction.init.ModResourceCosts;
import net.xiaohuige_hhy.winefoxfaction.units.goals.WineFoxBuildRepairGoal;
import net.xiaohuige_hhy.winefoxfaction.units.goals.WineFoxGatherResourcesGoal;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class WineFoxSalespersonUnit extends AbstractWineFoxUnit implements WorkerUnit, AttackerUnit {

	final static public float attackDamage = 1.0f;
	final static public float attacksPerSecond = 0.5f;
	final static public float maxHealth = 25.0f;
	final static public float armorValue = 0.0f;
	final static public float movementSpeed = 0.25f;
	final static public float attackRange = 2;
	final static public float aggroRange = 0;
	final static public boolean willRetaliate = false;
	final static public boolean aggressiveWhenIdle = false;
	final static public float rangedDamageResist = 0.0f;

	public static final String VETERAN_MODEL_ID = "geckolib:winefox_salesperson_84961723c2751ef4c6b9a0f8596f95cc";

	public static final String NBT_EXP = "WineFoxExp";
	public static final String NBT_VETERAN = "WineFoxVeteran";

	public boolean isVeteran = false;
	public boolean isVeteran() { return isVeteran; }

	public void setVeteran(boolean veteran) {
		if (this.isVeteran == veteran)
			return;
		this.isVeteran = veteran;
		super.setModelId(veteran ? VETERAN_MODEL_ID : getDefaultModelId());
		if (!this.level().isClientSide())
			UnitSyncClientboundPacket.makeVillagerVeteran(this);
	}

	public void makeVeteran() {
		setVeteran(true);
	}

	public boolean hasSpeedCheat() {
		return !this.level().isClientSide() && ResearchServerEvents.playerHasCheat(getOwnerName(), "operationcwal");
	}

	final static public int EXP_REQ = 600;
	public int exp = 0;
	public void setExp(int exp) {
		this.exp = exp;
		if (this.exp >= EXP_REQ && !isVeteran)
			makeVeteran();
	}
	public void incrementExp(int exp) {
		setExp(this.exp + (hasSpeedCheat() ? 10 : 1) * exp);
	}

	final static public float LUMBERJACK_SPEED_MULT_VETERAN = 1.5f;
	final static public float MINER_SPEED_MULT_VETERAN = 1.5f;

	public WineFoxBuildRepairGoal buildRepairGoal;
	public WineFoxGatherResourcesGoal gatherResourcesGoal;
	private ExploreBuildLocationGoal exploreBuildLocationGoal;
	private AbstractMeleeAttackUnitGoal attackGoal;

	private EnemySearchBehaviour attackSearchBehaviour = EnemySearchBehaviour.NONE;
	public EnemySearchBehaviour getEnemySearchBehaviour() { return attackSearchBehaviour; }
	public void setEnemySearchBehaviour(EnemySearchBehaviour behaviour) { attackSearchBehaviour = behaviour; }

	private BlockPos attackMoveTarget = null;
	public BlockPos getAttackMoveTarget() { return attackMoveTarget; }
	public void setAttackMoveTarget(@Nullable BlockPos bp) { this.attackMoveTarget = bp; }

	public BuildRepairGoal getBuildRepairGoal() {return buildRepairGoal;}
	public GatherResourcesGoal getGatherResourceGoal() {return gatherResourcesGoal;}
	public ExploreBuildLocationGoal getExploreBuildLocationGoal() {return exploreBuildLocationGoal;}
	public Goal getAttackGoal() {return attackGoal;}
	public Goal getAttackBuildingGoal() {return null;}

	public boolean getWillRetaliate() {return willRetaliate;}
	public boolean getAggressiveWhenIdle() {return aggressiveWhenIdle && !isVehicle();}
	public float getUnitAttackDamage() {return AttackerUnit.super.getUnitAttackDamage();}

	@Nullable
	public ResourceCost getCost() {return ModResourceCosts.WINE_FOX_SALESPERSON;}
	public boolean canAttackBuildings() {return getAttackBuildingGoal() != null;}

	@Override
	protected String getDefaultModelId() {
		return "geckolib:winefox_salesperson";
	}

	public BlockState getReplantBlockState() {
		return Blocks.MELON_STEM.defaultBlockState();
	}
	
	public boolean isSwingingArmRepeatedly() {
		return ((this.getGatherResourceGoal() != null && this.getGatherResourceGoal().isGathering()) ||
			(this.getBuildRepairGoal() != null && this.getBuildRepairGoal().isBuilding()));
	}

	public WineFoxSalespersonUnit(EntityType<? extends EntityMaid> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public LivingEntity getTarget() {
		return targetGoal.getTarget();
	}
	
	public void resetBehaviours() {
		this.gatherResourcesGoal.stop();
		this.buildRepairGoal.stop();
	}
	
	public static final int HOLD_NONE = 0;
	public static final int HOLD_BUILD = 1;
	public static final int HOLD_FOOD = 2;
	public static final int HOLD_WOOD = 3;
	public static final int HOLD_ORE = 4;

	private static final EntityDataAccessor<Integer> DATA_HOLDING_STAT =
		SynchedEntityData.defineId(WineFoxSalespersonUnit.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_SWINGING_ARM =
		SynchedEntityData.defineId(WineFoxSalespersonUnit.class, EntityDataSerializers.BOOLEAN);

	public int getHoldingStat() { return this.entityData.get(DATA_HOLDING_STAT); }

	public void setHoldingStat(int stat) { this.entityData.set(DATA_HOLDING_STAT, stat); }

	public boolean isSwingingArm() { return this.entityData.get(DATA_SWINGING_ARM); }

	public void setSwingingArm(boolean swinging) { this.entityData.set(DATA_SWINGING_ARM, swinging); }

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_HOLDING_STAT, HOLD_NONE);
		this.entityData.define(DATA_SWINGING_ARM, false);
	}

	public void tick() {
		super.tick();
		AttackerUnit.tick(this);
		WorkerUnit.tick(this);
		if (!level.isClientSide)
			this.setSwingingArm(isSwingingArmRepeatedly());
		ItemStack mainHandItem = this.getItemBySlot(EquipmentSlot.MAINHAND);
		if (this.getHoldingStat() == HOLD_BUILD) {
			if (!mainHandItem.is(Items.IRON_SHOVEL) && !mainHandItem.is(Items.DIAMOND_SHOVEL))
				super.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(this.isVeteran() ? Items.DIAMOND_SHOVEL : Items.IRON_SHOVEL));
		} else if (this.getHoldingStat() == HOLD_FOOD) {
			if (!mainHandItem.is(Items.IRON_HOE) && !mainHandItem.is(Items.DIAMOND_HOE))
				super.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(this.isVeteran() && this.getGatherResourceGoal().isFarming() ? Items.DIAMOND_HOE : Items.IRON_HOE));
		} else if (this.getHoldingStat() == HOLD_WOOD) {
			if (!mainHandItem.is(Items.IRON_AXE) && !mainHandItem.is(Items.DIAMOND_AXE))
				super.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(this.isVeteran() ? Items.DIAMOND_AXE : Items.IRON_AXE));
		} else if (this.getHoldingStat() == HOLD_ORE) {
			if (!mainHandItem.is(Items.IRON_PICKAXE) && !mainHandItem.is(Items.DIAMOND_PICKAXE))
				super.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(this.isVeteran() ? Items.DIAMOND_PICKAXE : Items.IRON_PICKAXE));
		} else if (this.getTargetGoal().getTarget() != null) {
			if (!mainHandItem.is(Items.WOODEN_SWORD) && !mainHandItem.is(Items.STONE_SWORD))
				super.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(this.isVeteran() ? Items.STONE_SWORD : Items.WOODEN_SWORD));
		}
	}

	@Override
	public void setItemSlot(@NotNull EquipmentSlot slot, @NotNull ItemStack stack) {
		if (slot == EquipmentSlot.MAINHAND && stack.isEmpty() && this.getHoldingStat() != HOLD_NONE)
			return;
		
		super.setItemSlot(slot, stack);
	}

	@Override
	public void addAdditionalSaveData(@NotNull CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt(NBT_EXP, this.exp);
		tag.putBoolean(NBT_VETERAN, this.isVeteran);
	}

	@Override
	public void readAdditionalSaveData(@NotNull CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		if (tag.contains(NBT_VETERAN))
			this.setVeteran(tag.getBoolean(NBT_VETERAN));
		if (tag.contains(NBT_EXP))
			this.setExp(tag.getInt(NBT_EXP));
	}

	public static AttributeSupplier.@NotNull Builder createAttributes() {
		return Mob.createMobAttributes()
			.add(Attributes.MOVEMENT_SPEED, WineFoxSalespersonUnit.movementSpeed)
			.add(Attributes.ATTACK_DAMAGE, WineFoxSalespersonUnit.attackDamage)
			.add(Attributes.ARMOR, WineFoxSalespersonUnit.armorValue)
			.add(Attributes.MAX_HEALTH, WineFoxSalespersonUnit.maxHealth)
			.add(Attributes.FOLLOW_RANGE, Unit.getFollowRange())
			.add(AttributeRegistrar.ATTACK_DAMAGE.get(), attackDamage)
			.add(AttributeRegistrar.ATTACKS_PER_SECOND.get(), attacksPerSecond)
			.add(AttributeRegistrar.ATTACK_RANGE.get(), attackRange)
			.add(AttributeRegistrar.AGGRO_RANGE.get(), aggroRange)
			.add(AttributeRegistrar.SIGHT_RANGE.get(), Unit.DEFAULT_SIGHT_RANGE)
			.add(AttributeRegistrar.RANGED_DAMAGE_RESIST.get(), rangedDamageResist)
			.add(AttributeRegistrar.MAGIC_DAMAGE_RESIST.get(), 0);
	}

	public void initialiseGoals() {
		this.usePortalGoal = new UsePortalGoal(this);
		this.moveGoal = new MoveToTargetBlockGoal(this, false, 0);
		this.targetGoal = new SelectedTargetGoal<>(this, true, true);
		this.garrisonGoal = new GarrisonGoal(this);
		this.attackGoal = new MeleeAttackUnitGoal(this, true);
		this.buildRepairGoal = new WineFoxBuildRepairGoal(this);
		this.gatherResourcesGoal = new WineFoxGatherResourcesGoal(this);
		this.returnResourcesGoal = new ReturnResourcesGoal(this);
		this.exploreBuildLocationGoal = new ExploreBuildLocationGoal(this);
	}

	@Override
	protected void registerGoals() {
		initialiseGoals();
		this.goalSelector.addGoal(2, usePortalGoal);

		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(2, attackGoal);
		this.goalSelector.addGoal(2, exploreBuildLocationGoal);
		this.goalSelector.addGoal(2, buildRepairGoal);
		this.goalSelector.addGoal(2, gatherResourcesGoal);
		this.goalSelector.addGoal(2, returnResourcesGoal);
		this.goalSelector.addGoal(2, garrisonGoal);
		this.targetSelector.addGoal(2, targetGoal);
		this.goalSelector.addGoal(3, moveGoal);
		this.goalSelector.addGoal(4, new RandomLookAroundUnitGoal(this));
	}

	@Override
	public void setupEquipmentAndUpgradesClient() {
		if (ResearchClient.hasResearch(ProductionItems.RESEARCH_RESOURCE_CAPACITY))
			this.maxResources = 200;
	}

	@Override
	public void setupEquipmentAndUpgradesServer() {
		if (ResearchServerEvents.playerHasResearch(this.getOwnerName(), ProductionItems.RESEARCH_RESOURCE_CAPACITY))
			this.maxResources = 200;
	}

	@Override
	public List<Button> getAbilityButtons() {
		List<Button> abilities = new ArrayList<>(getAbilities().getButtons(this));
		if (FMLEnvironment.dist == Dist.CLIENT) {
			abilities.addAll(Factions.getFaction(this).getBuildingButtons());
		}
		return abilities;
	}
}
