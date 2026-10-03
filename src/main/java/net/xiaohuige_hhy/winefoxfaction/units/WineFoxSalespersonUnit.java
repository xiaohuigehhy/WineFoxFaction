package net.xiaohuige_hhy.winefoxfaction.units;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.world.data.MaidWorldData;
import com.mojang.serialization.Dynamic;
import com.solegendary.reignofnether.ability.Abilities;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.production.ProductionItems;
import com.solegendary.reignofnether.faction.Factions;
import com.solegendary.reignofnether.hud.buttons.Button;
import com.solegendary.reignofnether.registrars.AttributeRegistrar;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.unit.Checkpoint;
import com.solegendary.reignofnether.unit.EnemySearchBehaviour;
import com.solegendary.reignofnether.unit.UnitAnimationAction;
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
import com.solegendary.reignofnether.unit.interfaces.ArmSwingingUnit;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.interfaces.WorkerUnit;
import com.solegendary.reignofnether.unit.packets.UnitAnimationClientboundPacket;
import com.solegendary.reignofnether.unit.packets.UnitSyncClientboundPacket;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.xiaohuige_hhy.winefoxfaction.units.goals.WineFoxGatherResourcesGoal;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

public class WineFoxSalespersonUnit extends EntityMaid implements IWineFoxUnit, Unit, WorkerUnit, AttackerUnit, ArmSwingingUnit {

	public static final Abilities ABILITIES = new Abilities();

	@Override
	public void updateAbilityButtons() {
		abilities = ABILITIES.clone();
	}
	Object2ObjectArrayMap<Ability, Float> cooldowns = Unit.createCooldownMap();
	Object2ObjectArrayMap<Ability, Integer> charges = new Object2ObjectArrayMap<>();

	@Override public boolean hasAutocast(Ability ability) { return autocast == ability; }
	@Override public void setAutocast(Ability autocast) { this.autocast = autocast; }

	@Override
	public Object2ObjectArrayMap<Ability, Float> getAbilityCooldowns() { return cooldowns; }
	@Override
	public Object2ObjectArrayMap<Ability, Integer> getAbilityCharges() { return charges; }

	Ability autocast;

	private int eatingTicksLeft = 0;
	public void setEatingTicksLeft(int amount) { eatingTicksLeft = amount; }
	public int getEatingTicksLeft() { return eatingTicksLeft; }
	private BlockPos anchorPos = new BlockPos(0,0,0);
	public void setAnchor(BlockPos bp) { anchorPos = bp; }
	public BlockPos getAnchor() { return anchorPos; }

	private final ArrayList<Checkpoint> checkpoints = new ArrayList<>();
	public ArrayList<Checkpoint> getCheckpoints() { return checkpoints; }

	GarrisonGoal garrisonGoal;
	public GarrisonGoal getGarrisonGoal() { return garrisonGoal; }
	public boolean canGarrison() { return getGarrisonGoal() != null; }

	UsePortalGoal usePortalGoal;
	public UsePortalGoal getUsePortalGoal() { return usePortalGoal; }
	public boolean canUsePortal() { return getUsePortalGoal() != null; }

	public Abilities getAbilities() {return abilities;}
	public List<ItemStack> getItems() {return items;}
	public MoveToTargetBlockGoal getMoveGoal() {return moveGoal;}
	public SelectedTargetGoal<? extends LivingEntity> getTargetGoal() {return targetGoal;}
	public BuildRepairGoal getBuildRepairGoal() {return buildRepairGoal;}
	public GatherResourcesGoal getGatherResourceGoal() {return gatherResourcesGoal;}
	public ReturnResourcesGoal getReturnResourcesGoal() {return returnResourcesGoal;}
	public ExploreBuildLocationGoal getExploreBuildLocationGoal() {return exploreBuildLocationGoal;}
	public Goal getAttackGoal() {return attackGoal;}
	public Goal getAttackBuildingGoal() {return null;}
	public int getMaxResources() {return maxResources;}

	private EnemySearchBehaviour attackSearchBehaviour = EnemySearchBehaviour.NONE;
	public EnemySearchBehaviour getEnemySearchBehaviour() { return attackSearchBehaviour; }
	public void setEnemySearchBehaviour(EnemySearchBehaviour behaviour) { attackSearchBehaviour = behaviour; }

	private MoveToTargetBlockGoal moveGoal;
	private SelectedTargetGoal<? extends LivingEntity> targetGoal;
	public BuildRepairGoal buildRepairGoal;
	public GatherResourcesGoal gatherResourcesGoal;
	private ReturnResourcesGoal returnResourcesGoal;
	private ExploreBuildLocationGoal exploreBuildLocationGoal;
	private AbstractMeleeAttackUnitGoal attackGoal;

	public BlockPos getAttackMoveTarget() { return attackMoveTarget; }
	public LivingEntity getFollowTarget() { return followTarget; }
	public boolean getHoldPosition() { return holdPosition; }
	public void setHoldPosition(boolean holdPosition) { this.holdPosition = holdPosition; }

	private BlockPos attackMoveTarget = null;
	private LivingEntity followTarget = null;
	private boolean holdPosition = false;

	public String getOwnerName() { return this.entityData.get(ownerDataAccessor); }
	public void setOwnerName(String name) { this.entityData.set(ownerDataAccessor, name); }
	public static final EntityDataAccessor<String> ownerDataAccessor =
		SynchedEntityData.defineId(WineFoxSalespersonUnit.class, EntityDataSerializers.STRING);

	public int getScenarioRoleIndex() { return this.entityData.get(scenarioRoleDataAccessor); }
	public void setScenarioRoleIndex(int index) { this.entityData.set(scenarioRoleDataAccessor, index); }
	public static final EntityDataAccessor<Integer> scenarioRoleDataAccessor =
		SynchedEntityData.defineId(WineFoxSalespersonUnit.class, EntityDataSerializers.INT);

	public String getOnDeathCommand() { return this.entityData.get(onDeathCommandDataAccessor); }
	public void setOnDeathCommand(String command) { this.entityData.set(onDeathCommandDataAccessor, command); }
	public static final EntityDataAccessor<String> onDeathCommandDataAccessor =
		SynchedEntityData.defineId(WineFoxSalespersonUnit.class, EntityDataSerializers.STRING);

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(ownerDataAccessor, "");
		this.entityData.define(scenarioRoleDataAccessor, -1);
		this.entityData.define(onDeathCommandDataAccessor, "");
		setModelId("geckolib:winefox_salesperson");
	}

	public boolean getWillRetaliate() {return willRetaliate;}
	public boolean getAggressiveWhenIdle() {return aggressiveWhenIdle && !isVehicle();}
	public float getUnitAttackDamage() {return AttackerUnit.super.getUnitAttackDamage();}

	@Nullable
	public ResourceCost getCost() {return ResourceCosts.VILLAGER;}
	public boolean canAttackBuildings() {return getAttackBuildingGoal() != null;}

	public void setAttackMoveTarget(@Nullable BlockPos bp) { this.attackMoveTarget = bp; }
	public void setFollowTarget(@Nullable LivingEntity target) { this.followTarget = target; }


	public BlockState getReplantBlockState() {
		return Blocks.PUMPKIN_STEM.defaultBlockState();
	}

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

	public int maxResources = 100;
	
	public boolean isVeteran = false;
	public boolean isVeteran() { return isVeteran; }
	
	public void makeVeteran() {
		isVeteran = true;
		UnitSyncClientboundPacket.makeVillagerVeteran(this);
		this.setModelId("geckolib:winefox_salesperson_84961723c2751ef4c6b9a0f8596f95cc");
	}
	
	public boolean hasSpeedCheat() {
		return !this.level().isClientSide() && ResearchServerEvents.playerHasCheat(getOwnerName(), "operationcwal");
	}
	
	final static public int EXP_REQ = 600;
	public int exp = 0;
	public void incrementExp(int exp) {
		this.exp += (hasSpeedCheat() ? 10 : 1) * exp;
		if (this.exp >= EXP_REQ && !isVeteran)
			makeVeteran();
	}
	
	final static public float LUMBERJACK_SPEED_MULT_VETERAN = 1.5f;
	final static public float MINER_SPEED_MULT_VETERAN = 1.5f;
	
	
	private Abilities abilities = ABILITIES.clone();
	private final List<ItemStack> items = new ArrayList<>();

	private boolean isSwingingArmOnce = false;
	private int swingTime = 0;

	public int getSwingTime() {
		return swingTime;
	}

	public void setSwingTime(int time) {
		this.swingTime = time;
	}

	public boolean isSwingingArmOnce() {
		return isSwingingArmOnce;
	}

	public void setSwingingArmOnce(boolean swing) {
		isSwingingArmOnce = swing;
	}

	public boolean isSwingingArmRepeatedly() {
		return ((this.getGatherResourceGoal() != null && this.getGatherResourceGoal().isGathering()) ||
			(this.getBuildRepairGoal() != null && this.getBuildRepairGoal().isBuilding()));
	}

	@SuppressWarnings("unchecked")
	public WineFoxSalespersonUnit(EntityType<? extends EntityMaid> entityType, Level level) {
		super((EntityType<EntityMaid>) entityType, level);
		updateAbilityButtons();
	}

	@Override
	protected void dropEquipment() {
	}

	@Override
	public boolean isPushable() {
		return false;
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
	protected void hurtArmor(@NotNull DamageSource damageSource, float damage) {
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

	@Override
	public void onRemovedFromWorld() {
		super.onRemovedFromWorld();
		if (!this.level().isClientSide && this.isAlive() && this.getOwnerUUID() != null) {
			MaidWorldData data = MaidWorldData.get(this.level());
			if (data != null) {
				data.removeInfo(this);
			}
		}
	}

	@Override
	public @NotNull InteractionResult mobInteract(@NotNull Player playerIn, @NotNull InteractionHand hand) {
		return InteractionResult.FAIL;
	}

	@Override
	public boolean canBrainMoving() {
		return false;
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

	public void tick() {
		this.setCanPickUpLoot(true);
		super.tick();
		Unit.tick(this);
		AttackerUnit.tick(this);
		WorkerUnit.tick(this);
		ItemStack mainHandItem = this.getItemBySlot(EquipmentSlot.MAINHAND);
		if (this.getBuildRepairGoal().isBuilding()) {
			if (!mainHandItem.is(Items.IRON_SHOVEL)) {
				if (this.isVeteran())
					this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SHOVEL));
				else
					this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
			}
		} else if (this.getGatherResourceGoal().isGathering()) {
			switch (this.getGatherResourceGoal().getTargetResourceName()) {
				case FOOD -> {
					if (!mainHandItem.is(Items.IRON_HOE)) {
						if (this.isVeteran() && this.getGatherResourceGoal().isFarming())
							this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_HOE));
						else
							this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_HOE));
					}
				}
				case WOOD -> {
					if (!mainHandItem.is(Items.IRON_AXE)) {
						if (this.isVeteran())
							this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_AXE));
						else
							this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
					}
				}
				case ORE -> {
					if (!mainHandItem.is(Items.IRON_PICKAXE)) {
						if (this.isVeteran())
							this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_PICKAXE));
						else
							this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_PICKAXE));
					}
				}
			}
		} else if (this.getTargetGoal().getTarget() != null) {
			if (!mainHandItem.is(Items.WOODEN_SWORD) && !mainHandItem.is(Items.STONE_SWORD)) {
				if (this.isVeteran())
					this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
				else
					this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WOODEN_SWORD));
				if (!this.level().isClientSide())
					UnitAnimationClientboundPacket.sendEntityPacket(UnitAnimationAction.NON_KEYFRAME_START, this, ((Unit) this).getTargetGoal().getTarget());
			}
		}
	}

	@Override
	public void remove(@NotNull RemovalReason pReason) {
		if (this.level() instanceof ServerLevel serverLevel) {
			String command = this.getOnDeathCommand();
			if (command != null && !command.isEmpty()) {
				CommandSourceStack source;
				source = serverLevel.getServer()
					.createCommandSourceStack()
					.withEntity(this)
					.withPosition(this.position())
					.withLevel(serverLevel)
					.withPermission(2);
				serverLevel.getServer().getCommands().performPrefixedCommand(source, command);
			}
		}
		super.remove(pReason);
	}

	@Override
	public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
		super.addAdditionalSaveData(pCompound);
		this.addUnitSaveData(pCompound);
	}

	@Override
	public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
		super.readAdditionalSaveData(pCompound);
		this.readUnitSaveData(pCompound);
	}

	public void initialiseGoals() {
		this.usePortalGoal = new UsePortalGoal(this);
		this.moveGoal = new MoveToTargetBlockGoal(this, false, 0);
		this.targetGoal = new SelectedTargetGoal<>(this, true, true);
		this.garrisonGoal = new GarrisonGoal(this);
		this.attackGoal = new MeleeAttackUnitGoal(this, true);
		this.buildRepairGoal = new BuildRepairGoal(this);
		this.gatherResourcesGoal = new WineFoxGatherResourcesGoal(this);
		this.returnResourcesGoal = new ReturnResourcesGoal(this);
		this.exploreBuildLocationGoal = new ExploreBuildLocationGoal(this);
	}

	@Override
	protected @NotNull Brain<?> makeBrain(@NotNull Dynamic<?> dynamicIn) {
		return this.brainProvider().makeBrain(dynamicIn);
	}

	@Override
	public void refreshBrain(@NotNull ServerLevel serverWorldIn) {
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

	@Override
	@Nullable
	public SpawnGroupData finalizeSpawn(
		@NotNull ServerLevelAccessor pLevel,
		@NotNull DifficultyInstance pDifficulty,
		@NotNull MobSpawnType pReason,
		@Nullable SpawnGroupData pSpawnData,
		@Nullable CompoundTag pDataTag
	) {
		return pSpawnData;
	}
}
