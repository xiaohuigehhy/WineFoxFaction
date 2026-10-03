package net.xiaohuige_hhy.winefoxfaction.units;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.world.data.MaidWorldData;
import com.mojang.serialization.Dynamic;
import com.solegendary.reignofnether.ability.Abilities;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.registrars.AttributeRegistrar;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.unit.Checkpoint;
import com.solegendary.reignofnether.unit.goals.GarrisonGoal;
import com.solegendary.reignofnether.unit.goals.MoveToTargetBlockGoal;
import com.solegendary.reignofnether.unit.goals.RandomLookAroundUnitGoal;
import com.solegendary.reignofnether.unit.goals.ReturnResourcesGoal;
import com.solegendary.reignofnether.unit.goals.SelectedTargetGoal;
import com.solegendary.reignofnether.unit.goals.UsePortalGoal;
import com.solegendary.reignofnether.unit.interfaces.Unit;

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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.xiaohuige_hhy.winefoxfaction.register.ModResourceCosts;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

public class WineFoxLittleUnit extends EntityMaid implements IWineFoxUnit {

	public static final Abilities ABILITIES = new Abilities();

	@Override
	public void updateAbilityButtons() {
		abilities = ABILITIES.clone();
	}
	Object2ObjectArrayMap<Ability, Float> cooldowns = Unit.createCooldownMap();
	Object2ObjectArrayMap<Ability, Integer> charges = new Object2ObjectArrayMap<>();

	@Override
	public Object2ObjectArrayMap<Ability, Float> getAbilityCooldowns() { return cooldowns; }
	@Override
	public Object2ObjectArrayMap<Ability, Integer> getAbilityCharges() { return charges; }
	@Override public boolean hasAutocast(Ability ability) { return autocast == ability; }
	@Override public void setAutocast(Ability autocast) { this.autocast = autocast; }

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
	public ReturnResourcesGoal getReturnResourcesGoal() {return null;}
	public int getMaxResources() {return 0;}

	private MoveToTargetBlockGoal moveGoal;
	private SelectedTargetGoal<? extends LivingEntity> targetGoal;

	public LivingEntity getFollowTarget() { return followTarget; }
	public boolean getHoldPosition() { return holdPosition; }
	public void setHoldPosition(boolean holdPosition) { this.holdPosition = holdPosition; }

	private LivingEntity followTarget = null;
	private boolean holdPosition = false;

	public String getOwnerName() { return this.entityData.get(ownerDataAccessor); }
	public void setOwnerName(String name) { this.entityData.set(ownerDataAccessor, name); }
	public static final EntityDataAccessor<String> ownerDataAccessor =
		SynchedEntityData.defineId(WineFoxLittleUnit.class, EntityDataSerializers.STRING);

	public int getScenarioRoleIndex() { return this.entityData.get(scenarioRoleDataAccessor); }
	public void setScenarioRoleIndex(int index) { this.entityData.set(scenarioRoleDataAccessor, index); }
	public static final EntityDataAccessor<Integer> scenarioRoleDataAccessor =
		SynchedEntityData.defineId(WineFoxLittleUnit.class, EntityDataSerializers.INT);

	public String getOnDeathCommand() { return this.entityData.get(onDeathCommandDataAccessor); }
	public void setOnDeathCommand(String command) { this.entityData.set(onDeathCommandDataAccessor, command); }
	public static final EntityDataAccessor<String> onDeathCommandDataAccessor =
		SynchedEntityData.defineId(WineFoxLittleUnit.class, EntityDataSerializers.STRING);

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(ownerDataAccessor, "");
		this.entityData.define(scenarioRoleDataAccessor, -1);
		this.entityData.define(onDeathCommandDataAccessor, "");
		setModelId("geckolib:winefox_little");
	}

	@Nullable
	public ResourceCost getCost() {return ModResourceCosts.WINE_FOX_LITTLE;}

	public void setFollowTarget(@Nullable LivingEntity target) { this.followTarget = target; }

	final static public float maxHealth = 20.0f;
	final static public float armorValue = 0.0f;
	final static public float movementSpeed = 0.25f;

	private Abilities abilities = ABILITIES.clone();
	private final List<ItemStack> items = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public WineFoxLittleUnit(EntityType<? extends EntityMaid> entityType, Level level) {
		super((EntityType<EntityMaid>) entityType, level);
		updateAbilityButtons();
	}

	@Override
	protected void dropEquipment() {
	}
	
	@Override
	protected void hurtArmor(@NotNull DamageSource damageSource, float damage) {
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
			.add(Attributes.MOVEMENT_SPEED, WineFoxLittleUnit.movementSpeed)
			.add(Attributes.MAX_HEALTH, WineFoxLittleUnit.maxHealth)
			.add(Attributes.FOLLOW_RANGE, Unit.getFollowRange())
			.add(Attributes.ARMOR, WineFoxLittleUnit.armorValue)
			.add(AttributeRegistrar.SIGHT_RANGE.get(), Unit.DEFAULT_SIGHT_RANGE)
			.add(AttributeRegistrar.RANGED_DAMAGE_RESIST.get(), 0)
			.add(AttributeRegistrar.MAGIC_DAMAGE_RESIST.get(), 0);
	}

	public void tick() {
		this.setCanPickUpLoot(false);
		super.tick();
		Unit.tick(this);
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
		this.goalSelector.addGoal(2, garrisonGoal);
		this.targetSelector.addGoal(2, targetGoal);
		this.goalSelector.addGoal(3, moveGoal);
		this.goalSelector.addGoal(4, new RandomLookAroundUnitGoal(this));
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
	
	@Override
	public boolean isScout() {
		return true;
	}
}
