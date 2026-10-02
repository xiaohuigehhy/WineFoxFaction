package net.xiaohuige_hhy.winefoxfaction.units;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.projectile.DanmakuShoot;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.github.tartaricacid.touhoulittlemaid.world.data.MaidWorldData;
import com.mojang.serialization.Dynamic;
import com.solegendary.reignofnether.ability.Abilities;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.fogofwar.FogOfWarClientboundPacket;
import com.solegendary.reignofnether.registrars.AttributeRegistrar;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.unit.Checkpoint;
import com.solegendary.reignofnether.unit.EnemySearchBehaviour;
import com.solegendary.reignofnether.unit.UnitAnimationAction;
import com.solegendary.reignofnether.unit.goals.GarrisonGoal;
import com.solegendary.reignofnether.unit.goals.MeleeAttackBuildingGoal;
import com.solegendary.reignofnether.unit.goals.MoveToTargetBlockGoal;
import com.solegendary.reignofnether.unit.goals.RandomLookAroundUnitGoal;
import com.solegendary.reignofnether.unit.goals.ReturnResourcesGoal;
import com.solegendary.reignofnether.unit.goals.SelectedTargetGoal;
import com.solegendary.reignofnether.unit.goals.UnitRangedAttackGoal;
import com.solegendary.reignofnether.unit.goals.UsePortalGoal;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.RangedAttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.packets.UnitAnimationClientboundPacket;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

public class WineFoxBlueUnit extends EntityMaid implements IWineFoxUnit, AttackerUnit, RangedAttackerUnit {
	
	public static final Abilities ABILITIES = new Abilities();
	
	//region
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
	public Goal getAttackBuildingGoal() {return attackBuildingGoal;}
	public Goal getAttackGoal() {return attackGoal;}
	public ReturnResourcesGoal getReturnResourcesGoal() {return returnResourcesGoal;}
	public int getMaxResources() {return maxResources;}
	
	private EnemySearchBehaviour attackSearchBehaviour = EnemySearchBehaviour.NONE;
	public EnemySearchBehaviour getEnemySearchBehaviour() { return attackSearchBehaviour; }
	public void setEnemySearchBehaviour(EnemySearchBehaviour behaviour) { attackSearchBehaviour = behaviour; }
	
	private MoveToTargetBlockGoal moveGoal;
	private SelectedTargetGoal<? extends LivingEntity> targetGoal;
	private ReturnResourcesGoal returnResourcesGoal;
	
	public BlockPos getAttackMoveTarget() { return attackMoveTarget; }
	public LivingEntity getFollowTarget() { return followTarget; }
	public boolean getHoldPosition() { return holdPosition; }
	public void setHoldPosition(boolean holdPosition) { this.holdPosition = holdPosition; }
	
	// if true causes moveGoal and attackGoal to work together to allow attack moving
	// moves to a block but will chase/attack nearby monsters in range up to a certain distance away
	private BlockPos attackMoveTarget = null;
	private LivingEntity followTarget = null; // if nonnull, continuously moves to the target
	private boolean holdPosition = false;
	
	// which player owns this unit? this format ensures its synched to client without having to use packets
	public String getOwnerName() { return this.entityData.get(ownerDataAccessor); }
	public void setOwnerName(String name) { this.entityData.set(ownerDataAccessor, name); }
	public static final EntityDataAccessor<String> ownerDataAccessor =
		SynchedEntityData.defineId(WineFoxBlueUnit.class, EntityDataSerializers.STRING);
	
	// which scenario role does this unit use?
	public int getScenarioRoleIndex() { return this.entityData.get(scenarioRoleDataAccessor); }
	public void setScenarioRoleIndex(int index) { this.entityData.set(scenarioRoleDataAccessor, index); }
	public static final EntityDataAccessor<Integer> scenarioRoleDataAccessor =
		SynchedEntityData.defineId(WineFoxBlueUnit.class, EntityDataSerializers.INT);
	
	public String getOnDeathCommand() { return this.entityData.get(onDeathCommandDataAccessor); }
	public void setOnDeathCommand(String command) { this.entityData.set(onDeathCommandDataAccessor, command); }
	public static final EntityDataAccessor<String> onDeathCommandDataAccessor =
		SynchedEntityData.defineId(WineFoxBlueUnit.class, EntityDataSerializers.STRING);
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(ownerDataAccessor, "");
		this.entityData.define(scenarioRoleDataAccessor, -1);
		this.entityData.define(onDeathCommandDataAccessor, "");
		setModelId("geckolib:winefox_blue");
	}
	
	// combat stats
	public boolean getWillRetaliate() {return willRetaliate;}
	public boolean getAggressiveWhenIdle() {return aggressiveWhenIdle && !isVehicle();}
	public float getUnitAttackDamage() {return AttackerUnit.super.getUnitAttackDamage();}
	
	@Nullable
	public ResourceCost getCost() {return ResourceCosts.VINDICATOR;}
	public boolean canAttackBuildings() {return getAttackBuildingGoal() != null;}
	
	public void setAttackMoveTarget(@Nullable BlockPos bp) { this.attackMoveTarget = bp; }
	public void setFollowTarget(@Nullable LivingEntity target) { this.followTarget = target; }
	
	// endregion
	
	final static public float attackDamage = 7.0f;
	final static public float attacksPerSecond = 0.632f; // excludes crossbow charge time
	final static public float maxHealth = 45.0f;
	final static public float armorValue = 0.0f;
	final static public float movementSpeed = 0.24f;
	final static public float attackRange = 16.0F; // only used by ranged units or melee building attackers
	final static public float aggroRange = 16;
	final static public boolean willRetaliate = true; // will attack when hurt by an enemy
	final static public boolean aggressiveWhenIdle = true;
	
	public int maxResources = 100;
	
	public int fogRevealDuration = 0; // set > 0 for the client who is attacked by this unit
	public int getFogRevealDuration() { return fogRevealDuration; }
	public void setFogRevealDuration(int duration) { fogRevealDuration = duration; }
	
	private UnitRangedAttackGoal<? extends LivingEntity> attackGoal;
	private MeleeAttackBuildingGoal attackBuildingGoal;
	
	private Abilities abilities = ABILITIES.clone();
	private final List<ItemStack> items = new ArrayList<>();
	
	@SuppressWarnings("unchecked")
	public WineFoxBlueUnit(EntityType<? extends EntityMaid> entityType, Level level) {
		super((EntityType<EntityMaid>) entityType, level);
		updateAbilityButtons();
	}
	
	@Override
	protected void dropEquipment() {
	}
	
	// all for animation syncing...
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
	
	public void tick() {
		this.setCanPickUpLoot(true);
		super.tick();
		Unit.tick(this);
		AttackerUnit.tick(this);
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
		this.attackGoal = new UnitRangedAttackGoal<>(this, 0);
		this.attackBuildingGoal = new MeleeAttackBuildingGoal(this);
		this.returnResourcesGoal = new ReturnResourcesGoal(this);
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
		this.goalSelector.addGoal(2, attackBuildingGoal);
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
		float speed = (0.3f * (velocity + 1));
		float distance = this.distanceTo(pTarget);
		speed = speed + Mth.clamp(distance / 40f - 0.4f, 0, 2.4f);
		DanmakuShoot.create().setWorld(this.level()).setThrower(this)
			.setTarget(pTarget).setRandomColor().setRandomType()
			.setDamage(this.getUnitAttackDamage()).setGravity(0)
			.setVelocity(speed)
			.setInaccuracy(0)
			.aimedShot();
		if (!level().isClientSide() && pTarget instanceof Unit unit)
			FogOfWarClientboundPacket.revealRangedUnit(unit.getOwnerName(), this.getId());
		getMainHandItem().setDamageValue(0);
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
