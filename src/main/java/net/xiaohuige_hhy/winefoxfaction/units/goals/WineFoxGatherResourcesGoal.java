package net.xiaohuige_hhy.winefoxfaction.units.goals;

import static com.solegendary.reignofnether.blocks.BlockUtils.isFallingLogBlock;
import static com.solegendary.reignofnether.blocks.BlockUtils.isLogBlock;

import com.solegendary.reignofnether.ability.heroAbilities.enchanter.CivilEnchantment;
import com.solegendary.reignofnether.building.BuildingBlock;
import com.solegendary.reignofnether.building.BuildingUtils;
import com.solegendary.reignofnether.registrars.BlockRegistrar;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.resources.ResourceIndex;
import com.solegendary.reignofnether.resources.ResourceName;
import com.solegendary.reignofnether.resources.ResourceSource;
import com.solegendary.reignofnether.resources.ResourceSources;
import com.solegendary.reignofnether.resources.Resources;
import com.solegendary.reignofnether.resources.ResourcesServerEvents;
import com.solegendary.reignofnether.unit.goals.GatherResourcesGoal;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.interfaces.WorkerUnit;
import com.solegendary.reignofnether.unit.packets.UnitSyncClientboundPacket;
import com.solegendary.reignofnether.util.MiscUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.xiaohuige_hhy.winefoxfaction.units.WineFoxSalespersonUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;


public class WineFoxGatherResourcesGoal extends GatherResourcesGoal {
	
	
	private static final int REACH_RANGE = 5;
	
	private static final int MAX_REACH_DOWN = 4;
	private static final int DEFAULT_MAX_GATHER_TICKS = 600;
	private static final int MAX_SEARCH_CD_TICKS = 40;
	private static final int MAX_FAILED_SEARCHES = 4;
	private static final float TICK_CD = 20;
	
	
	private float gatherTicksLeft = DEFAULT_MAX_GATHER_TICKS;
	private int searchCdTicksLeft = 0;
	private int failedSearches = 0;
	private float cdTicksLeft = TICK_CD;
	private int ticksWithoutTarget = 0;
	private int ticksStationaryWithTarget = 0;
	private BlockPos lastOnPos = null;
	private BlockPos altSearchPos = null;
	
	private final Predicate<BlockPos> BLOCK_CONDITION = bp -> {
		BlockState bs = mob.level().getBlockState(bp);
		BlockState bsAbove = mob.level().getBlockState(bp.above());
		ResourceSource resBlock = ResourceSources.getFromBlockPos(bp, mob.level());
		
		if (!mob.level().getWorldBorder().isWithinBounds(bp))
			return false;
		
		if (resBlock == null || resBlock.resourceName != data.targetResourceName)
			return false;
		if (!resBlock.blockStateTest.test(bs))
			return false;
		
		
		if (data.targetFarm != null && !data.targetFarm.isPosInsideBuilding(bp))
			return false;
		
		if (bs.getBlock() == Blocks.FARMLAND || bs.getBlock() == Blocks.SOUL_SAND) {
			if (!bsAbove.isAir() || !canAffordReplant() || !BuildingUtils.isPosInsideAnyBuilding(mob.level().isClientSide(), bp))
				return false;
		} else if (data.targetFarm == null && BuildingUtils.isPosInsideAnyBuilding(mob.level().isClientSide(), bp))
			return false;
		
		
		boolean hasClearNeighbour = false;
		for (BlockPos adjBp : List.of(bp.above(), bp.north(), bp.south(), bp.east(), bp.west())) {
			if (ResourceSources.isClearMaterial(mob.level().getBlockState(adjBp))) {
				hasClearNeighbour = true;
				break;
			}
		}
		if (!hasClearNeighbour)
			return false;
		
		
		if (!hasReachableStandSpot(bp))
			return false;
		
		
		AABB aabb = AABB.ofSize(this.mob.position(), REACH_RANGE * 2, REACH_RANGE * 2, REACH_RANGE * 2);
		for (LivingEntity entity : this.mob.level().getNearbyEntities(LivingEntity.class, TargetingConditions.forNonCombat(), this.mob, aabb)) {
			if (entity instanceof Unit unit) {
				if (unit instanceof WorkerUnit workerUnit && workerUnit.getGatherResourceGoal() != null && entity.getId() != this.mob.getId()) {
					BlockPos otherUnitTarget = workerUnit.getGatherResourceGoal().getGatherTarget();
					if (otherUnitTarget != null && otherUnitTarget.equals(bp)) {
						altSearchPos = bp;
						return false;
					}
				}
			}
		}
		return true;
	};
	
	public WineFoxGatherResourcesGoal(Mob mob) {
		super(mob);
	}
	
	private static int resourceToHoldingStat(ResourceName resourceName, int current) {
		return switch (resourceName) {
			case FOOD -> WineFoxSalespersonUnit.HOLD_FOOD;
			case WOOD -> WineFoxSalespersonUnit.HOLD_WOOD;
			case ORE -> WineFoxSalespersonUnit.HOLD_ORE;
			default -> current;
		};
	}
	
	@Override
	public void start() {
		super.start();
		if (this.mob.level().isClientSide() || !(this.mob instanceof WineFoxSalespersonUnit winefox))
			return;
		if (!isGathering()) {
			winefox.setHoldingStat(resourceToHoldingStat(this.getTargetResourceName(), winefox.getHoldingStat()));
		}
	}
	
	@Override
	public void setTargetResourceName(ResourceName resourceName) {
		super.setTargetResourceName(resourceName);
		if (this.mob.level().isClientSide() || !(this.mob instanceof WineFoxSalespersonUnit winefox))
			return;
		winefox.setHoldingStat(resourceToHoldingStat(resourceName, winefox.getHoldingStat()));
	}
	
	public void tick() {
		if (this.mob.level().isClientSide()) {
			tickClient();
			return;
		}
		
		cdTicksLeft -= 1;
		if (cdTicksLeft <= 0)
			cdTicksLeft = TICK_CD;
		else
			return;
		
		if (data.gatherTarget == null && data.targetResourceName != ResourceName.NONE) {
			searchCdTicksLeft -= (int) (TICK_CD / 2);
			
			for (BlockPos todoBp : data.todoGatherTargets)
				if (BLOCK_CONDITION.test(todoBp)) {
					data.gatherTarget = todoBp;
					break;
				}
			
			if (data.gatherTarget != null)
				data.todoGatherTargets.remove(data.gatherTarget);
			
			if (data.gatherTarget == null && searchCdTicksLeft <= 0) {
				if (data.targetFarm != null) {
					for (BuildingBlock block : data.targetFarm.getBlocks()) {
						if (BLOCK_CONDITION.test(block.getBlockPos())) {
							data.gatherTarget = block.getBlockPos();
							break;
						}
					}
				} else {
					Optional<BlockPos> bpOpt;
					if (altSearchPos != null) {
						bpOpt = ResourceIndex.get(mob.level()).findClosest(
							mob.level(), altSearchPos, REACH_RANGE / 2, data.targetResourceName, BLOCK_CONDITION);
						altSearchPos = null;
					} else {
						
						int range = REACH_RANGE * (failedSearches + 1);
						if (failedSearches > MAX_FAILED_SEARCHES) {
							
							stopGathering();
						}
						
						bpOpt = ResourceIndex.get(mob.level()).findClosest(
							mob.level(),
							new BlockPos(
								(int) mob.getEyePosition().x,
								(int) mob.getEyePosition().y,
								(int) mob.getEyePosition().z
							), range, data.targetResourceName,
							BLOCK_CONDITION);
					}
					
					bpOpt.ifPresentOrElse(
						blockPos -> {
							data.gatherTarget = blockPos;
							failedSearches = 0;
						},
						() -> failedSearches += 1
					);
				}
				searchCdTicksLeft = MAX_SEARCH_CD_TICKS;
			}
			if (data.gatherTarget != null)
				data.targetResourceSource = ResourceSources.getFromBlockPos(data.gatherTarget, mob.level());
		}
		
		if (data.gatherTarget != null) {
			
			if (this.mob.getOnPos().equals(lastOnPos) && !isGathering())
				ticksStationaryWithTarget += (int) TICK_CD;
			else
				ticksStationaryWithTarget = 0;
			
			if (ticksStationaryWithTarget >= TICKS_STATIONARY_TIMEOUT) {
				ticksStationaryWithTarget = 0;
				data.gatherTarget = null;
				return;
			}
			lastOnPos = this.mob.getOnPos();
			
			if (!BLOCK_CONDITION.test(this.data.gatherTarget))
				removeGatherTarget();
			else
				setMoveTarget(data.gatherTarget);
			
			if (isGathering()) {
				
				mob.getLookControl().setLookAt(data.gatherTarget.getX(), data.gatherTarget.getY(), data.gatherTarget.getZ());
				mob.getLookControl().lookAtCooldown = 20;
				
				BlockState bsTarget = mob.level().getBlockState(data.gatherTarget);
				
				if (bsTarget.getBlock() == Blocks.FARMLAND || bsTarget.getBlock() == Blocks.SOUL_SAND) {
					gatherTicksLeft -= (TICK_CD / 2);
					gatherTicksLeft = Math.min(gatherTicksLeft, ResourceSources.REPLANT_TICKS_MAX);
					if (gatherTicksLeft <= 0) {
						gatherTicksLeft = DEFAULT_MAX_GATHER_TICKS;
						
						if (canAffordReplant()) {
							ResourcesServerEvents.addSubtractResources(new Resources(((Unit) mob).getOwnerName(), 0, -getReplantWoodCost(), 0));
							mob.level().setBlockAndUpdate(data.gatherTarget.above(), ((WorkerUnit) mob).getReplantBlockState());
							removeGatherTarget();
						}
					}
				} else {
					float ticksToProgress;
					
					if (ResearchServerEvents.playerHasCheat(((Unit) mob).getOwnerName(), "operationcwal"))
						ticksToProgress = (TICK_CD / 2) * 10;
					else
						ticksToProgress = (TICK_CD / 2);
					
					if (mob instanceof WineFoxSalespersonUnit vUnit) {
						if (ResourceSources.getBlockResourceName(getGatherTarget(), mob.level()) == ResourceName.WOOD && vUnit.isVeteran())
							ticksToProgress *= WineFoxSalespersonUnit.LUMBERJACK_SPEED_MULT_VETERAN;
						else if (ResourceSources.getBlockResourceName(getGatherTarget(), mob.level()) == ResourceName.ORE && vUnit.isVeteran())
							ticksToProgress *= WineFoxSalespersonUnit.MINER_SPEED_MULT_VETERAN;
						ticksToProgress *= CivilEnchantment.getEfficiencyMultiplier(vUnit);
					}
					
					this.gatherTicksLeft -= ticksToProgress;
					
					gatherTicksLeft = Math.min(gatherTicksLeft, data.targetResourceSource != null ? data.targetResourceSource.ticksToGather : Integer.MAX_VALUE);
					if (gatherTicksLeft <= 0) {
						gatherTicksLeft = DEFAULT_MAX_GATHER_TICKS;
						
						BlockState bs = this.mob.level().getBlockState(data.gatherTarget);
						boolean isLogBlock = isLogBlock(bs);
						boolean isFallingLogBlock = isFallingLogBlock(bs);
						if (isLogBlock)
							ResourcesServerEvents.fellAdjacentLogs(data.gatherTarget, new ArrayList<>(), this.mob.level());
						
						ResourceName expName = ResourceName.NONE;
						if (ResourceSources.getBlockResourceName(getGatherTarget(), mob.level()) == ResourceName.FOOD && isFarming())
							expName = ResourceName.FOOD;
						else if (ResourceSources.getBlockResourceName(getGatherTarget(), mob.level()) == ResourceName.WOOD && (isLogBlock || isFallingLogBlock))
							expName = ResourceName.WOOD;
						else if (ResourceSources.getBlockResourceName(getGatherTarget(), mob.level()) == ResourceName.ORE && bs.getBlock() != Blocks.POINTED_DRIPSTONE)
							expName = ResourceName.ORE;
						
						if (mob.level().destroyBlock(data.gatherTarget, false)) {
							
							if (mob instanceof WineFoxSalespersonUnit vUnit) {
								if (expName == ResourceName.FOOD)
									vUnit.incrementExp(7);
								else if (expName == ResourceName.WOOD)
									vUnit.incrementExp(20);
								else if (expName == ResourceName.ORE)
									vUnit.incrementExp(100);
							}
							
							if (data.targetResourceSource != null && data.targetResourceSource.resourceName == ResourceName.ORE && bsTarget.getBlock() != Blocks.POINTED_DRIPSTONE) {
								BlockState replaceBs;
								if (BuildingUtils.isInNetherRange(mob.level().isClientSide(), data.gatherTarget))
									replaceBs = BlockRegistrar.WALKABLE_MAGMA_BLOCK.get().defaultBlockState();
								else if (bsTarget.getBlock().getName().getString().toLowerCase().contains("deepslate"))
									replaceBs = Blocks.COBBLED_DEEPSLATE.defaultBlockState();
								else
									replaceBs = Blocks.COBBLESTONE.defaultBlockState();
								this.mob.level().setBlockAndUpdate(data.gatherTarget, replaceBs);
							}
							
							
							data.todoGatherTargets.remove(data.gatherTarget);
							for (BlockPos pos : MiscUtil.findAdjacentBlocks(data.gatherTarget, BLOCK_CONDITION))
								if (!data.todoGatherTargets.contains(pos))
									data.todoGatherTargets.add(pos);
							
							Unit unit = (Unit) mob;
							unit.getItems().add(new ItemStack(data.targetResourceSource.items.get(0)));
							UnitSyncClientboundPacket.sendSyncResourcesPacket(unit);
							
							
							if (Unit.atThresholdResources(unit))
								saveAndReturnResources();
							
							removeGatherTarget();
						}
					}
				}
			} else {
				if (mob.getNavigation().isDone())
					ticksWithoutTarget += (int) (TICK_CD / 2);
				if (ticksWithoutTarget >= NO_TARGET_TIMEOUT)
					this.removeGatherTarget();
			}
		}
	}
	
	private boolean hasReachableStandSpot(BlockPos bp) {
		var level = mob.level();
		for (BlockPos side : List.of(bp.north(), bp.south(), bp.east(), bp.west())) {
			for (int dy = 1; dy >= -MAX_REACH_DOWN; dy--) {
				BlockPos feet = side.above(dy);
				if (!MiscUtil.isSolidBlocking(level, feet)
					&& !MiscUtil.isSolidBlocking(level, feet.above())
					&& MiscUtil.isSolidBlocking(level, feet.below()))
					return true;
			}
		}
		return false;
	}
	
	private boolean canAffordReplant() {
		return ResourcesServerEvents.canAfford(((Unit) mob).getOwnerName(), ResourceName.WOOD, getReplantWoodCost());
	}
	
	private int getReplantWoodCost() {
		return ResourceCosts.REDUCED_REPLANT_WOOD_COST;
	}
	
	@Override
	public void stop() {
		super.stop();
		if (this.mob.level().isClientSide() || !(this.mob instanceof WineFoxSalespersonUnit winefox))
			return;
		if (winefox.getHoldingStat() >= WineFoxSalespersonUnit.HOLD_FOOD) {
			winefox.setHoldingStat(WineFoxSalespersonUnit.HOLD_NONE);
			winefox.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
		}
	}
}
