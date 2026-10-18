package net.xiaohuige_hhy.winefoxfaction.units;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.solegendary.reignofnether.registrars.AttributeRegistrar;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.unit.goals.GarrisonGoal;
import com.solegendary.reignofnether.unit.goals.MoveToTargetBlockGoal;
import com.solegendary.reignofnether.unit.goals.RandomLookAroundUnitGoal;
import com.solegendary.reignofnether.unit.goals.SelectedTargetGoal;
import com.solegendary.reignofnether.unit.goals.UsePortalGoal;
import com.solegendary.reignofnether.unit.interfaces.Unit;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.level.Level;
import net.xiaohuige_hhy.winefoxfaction.init.ModResourceCosts;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class WineFoxLittleUnit extends AbstractWineFoxUnit {

	final static public float maxHealth = 20.0f;
	final static public float armorValue = 0.0f;
	final static public float movementSpeed = 0.25f;

	public WineFoxLittleUnit(EntityType<? extends EntityMaid> entityType, Level level) {
		super(entityType, level);
	}

	@Nullable
	public ResourceCost getCost() {return ModResourceCosts.WINE_FOX_LITTLE;}

	@Override
	protected String getDefaultModelId() {
		return "geckolib:winefox_little";
	}

	public int getMaxResources() {return 0;}

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
		super.tick();
		this.setCanPickUpLoot(false);
	}

	public void initialiseGoals() {
		this.usePortalGoal = new UsePortalGoal(this);
		this.moveGoal = new MoveToTargetBlockGoal(this, false, 0);
		this.targetGoal = new SelectedTargetGoal<>(this, true, true);
		this.garrisonGoal = new GarrisonGoal(this);
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
	public boolean isScout() {
		return true;
	}
}
