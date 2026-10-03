package net.xiaohuige_hhy.winefoxfaction.entities;

import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.addon.GarrisonableBuildingAddon;
import com.solegendary.reignofnether.unit.interfaces.Unit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.xiaohuige_hhy.winefoxfaction.register.ModEntities;
import net.xiaohuige_hhy.winefoxfaction.register.ModItems;

import org.jetbrains.annotations.NotNull;

public class WineFoxSnowball extends Snowball {
	
	public float damage = 0.0f;
	
	public WineFoxSnowball(EntityType<? extends WineFoxSnowball> entityType, Level level) {
		super(entityType, level);
	}
	
	public WineFoxSnowball(Level level, LivingEntity livingEntity) {
		super(ModEntities.WINEFOX_SNOWBALL.get(), level);
		this.setOwner(livingEntity);
		this.setPos(livingEntity.getX(), livingEntity.getEyeY() - 0.1, livingEntity.getZ());
	}
	
	public void setDamage(float damage) {
		this.damage = damage;
	}
	
	public boolean isNoPhysics() {
		if (this.getOwner() instanceof Unit unit) {
			BuildingPlacement building = GarrisonableBuildingAddon.getGarrison(unit);
			
			if (building != null) {
				GarrisonableBuildingAddon garr = building.getBuilding().getActiveAddon(GarrisonableBuildingAddon.class);
				
				return garr != null && building.isPosInsideBuilding(this.blockPosition()) &&
					this.blockPosition().getY() > building.originPos.getY() + 5;
			}
		}
		return false;
	}
	
	protected @NotNull Item getDefaultItem() {
		return ModItems.WINEFOX_SNOWBALL.get();
	}
	
	@Override
	protected void onHitEntity(EntityHitResult p_37404_) {
		Entity entity = p_37404_.getEntity();
		float i = ((entity instanceof Blaze ? 3 : 0) + damage);
		entity.hurt(this.damageSources().thrown(this, this.getOwner()), i);
	}
	
	@Override
	protected float getGravity() {
		return 0.05F;
	}
	
	@Override
	protected void onHit(HitResult result) {
		HitResult.Type hitresult$type = result.getType();
		if (hitresult$type == HitResult.Type.ENTITY) {
			this.onHitEntity((EntityHitResult) result);
			this.level().gameEvent(GameEvent.PROJECTILE_LAND, result.getLocation(), GameEvent.Context.of(this, null));
			if (!this.level.isClientSide) {
				this.level().broadcastEntityEvent(this, (byte) 3);
				this.discard();
			}
		} else if (hitresult$type == HitResult.Type.BLOCK && !isNoPhysics()) {
			BlockHitResult blockhitresult = (BlockHitResult) result;
			this.onHitBlock(blockhitresult);
			BlockPos blockpos = blockhitresult.getBlockPos();
			this.level().gameEvent(GameEvent.PROJECTILE_LAND, blockpos, GameEvent.Context.of(this, this.level().getBlockState(blockpos)));
			if (!this.level.isClientSide) {
				this.level().broadcastEntityEvent(this, (byte) 3);
				this.discard();
			}
		}
	}
}
