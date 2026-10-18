package net.xiaohuige_hhy.winefoxfaction.units;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.world.data.MaidWorldData;
import com.mojang.serialization.Dynamic;
import com.solegendary.reignofnether.ability.Abilities;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.unit.Checkpoint;
import com.solegendary.reignofnether.unit.goals.GarrisonGoal;
import com.solegendary.reignofnether.unit.goals.MoveToTargetBlockGoal;
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
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

public abstract class AbstractWineFoxUnit extends EntityMaid implements Unit {

	public static final Abilities ABILITIES = new Abilities();

	public static final EntityDataAccessor<String> ownerDataAccessor =
		SynchedEntityData.defineId(AbstractWineFoxUnit.class, EntityDataSerializers.STRING);
	public static final EntityDataAccessor<Integer> scenarioRoleDataAccessor =
		SynchedEntityData.defineId(AbstractWineFoxUnit.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<String> onDeathCommandDataAccessor =
		SynchedEntityData.defineId(AbstractWineFoxUnit.class, EntityDataSerializers.STRING);

	Object2ObjectArrayMap<Ability, Float> cooldowns = Unit.createCooldownMap();
	Object2ObjectArrayMap<Ability, Integer> charges = new Object2ObjectArrayMap<>();

	Ability autocast;

	private int eatingTicksLeft = 0;
	private BlockPos anchorPos = new BlockPos(0, 0, 0);

	private final ArrayList<Checkpoint> checkpoints = new ArrayList<>();

	GarrisonGoal garrisonGoal;
	UsePortalGoal usePortalGoal;

	protected MoveToTargetBlockGoal moveGoal;
	protected SelectedTargetGoal<? extends LivingEntity> targetGoal;
	protected ReturnResourcesGoal returnResourcesGoal;

	private LivingEntity followTarget = null;
	private boolean holdPosition = false;
	private boolean isVictory = false;

	public int maxResources = 100;

	private Abilities abilities = ABILITIES.clone();
	private final List<ItemStack> items = new ArrayList<>();

	@SuppressWarnings("unchecked")
	protected AbstractWineFoxUnit(EntityType<? extends EntityMaid> entityType, Level level) {
		super((EntityType<EntityMaid>) entityType, level);
		updateAbilityButtons();
	}

	public abstract @Nullable ResourceCost getCost();

	protected abstract String getDefaultModelId();

	@Override
	public void updateAbilityButtons() {
		abilities = ABILITIES.clone();
	}

	@Override
	public boolean hasAutocast(Ability ability) { return autocast == ability; }

	@Override
	public void setAutocast(Ability autocast) { this.autocast = autocast; }

	@Override
	public Object2ObjectArrayMap<Ability, Float> getAbilityCooldowns() { return cooldowns; }

	@Override
	public Object2ObjectArrayMap<Ability, Integer> getAbilityCharges() { return charges; }

	public void setEatingTicksLeft(int amount) { eatingTicksLeft = amount; }

	public int getEatingTicksLeft() { return eatingTicksLeft; }

	public void setAnchor(BlockPos bp) { anchorPos = bp; }

	public BlockPos getAnchor() { return anchorPos; }

	public ArrayList<Checkpoint> getCheckpoints() { return checkpoints; }

	public GarrisonGoal getGarrisonGoal() { return garrisonGoal; }

	public boolean canGarrison() { return getGarrisonGoal() != null; }

	public UsePortalGoal getUsePortalGoal() { return usePortalGoal; }

	public boolean canUsePortal() { return getUsePortalGoal() != null; }

	public Abilities getAbilities() { return abilities; }

	public List<ItemStack> getItems() { return items; }

	public MoveToTargetBlockGoal getMoveGoal() { return moveGoal; }

	public SelectedTargetGoal<? extends LivingEntity> getTargetGoal() { return targetGoal; }

	public ReturnResourcesGoal getReturnResourcesGoal() { return returnResourcesGoal; }

	public int getMaxResources() { return maxResources; }

	public LivingEntity getFollowTarget() { return followTarget; }

	public void setFollowTarget(@Nullable LivingEntity target) { this.followTarget = target; }

	public boolean getHoldPosition() { return holdPosition; }

	public void setHoldPosition(boolean holdPosition) { this.holdPosition = holdPosition; }

	public void setVictory(boolean victory) { this.isVictory = victory; }

	public boolean isVictory() { return isVictory; }

	public String getOwnerName() { return this.entityData.get(ownerDataAccessor); }

	public void setOwnerName(String name) {
		if (!name.equals(this.getOwnerName()))
			this.isVictory = false;
		this.entityData.set(ownerDataAccessor, name);
	}

	public int getScenarioRoleIndex() { return this.entityData.get(scenarioRoleDataAccessor); }

	public void setScenarioRoleIndex(int index) { this.entityData.set(scenarioRoleDataAccessor, index); }

	public String getOnDeathCommand() { return this.entityData.get(onDeathCommandDataAccessor); }

	public void setOnDeathCommand(String command) { this.entityData.set(onDeathCommandDataAccessor, command); }

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(ownerDataAccessor, "");
		this.entityData.define(scenarioRoleDataAccessor, -1);
		this.entityData.define(onDeathCommandDataAccessor, "");
		setModelId(getDefaultModelId());
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

	public void tick() {
		this.setCanPickUpLoot(true);
		super.tick();
		Unit.tick(this);
	}
	
	@Override
	public void baseTick() {
		float health = this.getHealth();
		super.baseTick();
		if (this.getHealth() > health) {
			this.setHealth(health);
		}
	}
	
	@Override
	public void spawnRestoreHealthParticle(int particleCount) {
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

	@Override
	protected @NotNull Brain<?> makeBrain(@NotNull Dynamic<?> dynamicIn) {
		return this.brainProvider().makeBrain(dynamicIn);
	}

	@Override
	public void refreshBrain(@NotNull ServerLevel serverWorldIn) {
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
