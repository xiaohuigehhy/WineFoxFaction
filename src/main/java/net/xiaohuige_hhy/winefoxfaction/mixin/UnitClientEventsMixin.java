package net.xiaohuige_hhy.winefoxfaction.mixin;

import com.solegendary.reignofnether.unit.UnitClientEvents;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.xiaohuige_hhy.winefoxfaction.units.WineFoxSalespersonUnit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(UnitClientEvents.class)
public class UnitClientEventsMixin {

	@Inject(method = "makeVillagerVeteran", at = @At("TAIL"), remap = false)
	private static void injectSalespersonVeteran(int unitId, CallbackInfo ci) {
		for (LivingEntity entity : UnitClientEvents.getAllUnits())
			if (entity instanceof WineFoxSalespersonUnit sUnit && unitId == entity.getId())
				sUnit.isVeteran = true;
	}

	@Redirect(
		method = "syncUnitAnimation",
		remap = false,
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V",
			ordinal = 1,
			remap = true
		)
	)
	private static void redirectSalespersonWeapon(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {
		if (entity instanceof WineFoxSalespersonUnit sUnit && sUnit.isVeteran())
			entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
		else
			entity.setItemSlot(slot, stack);
	}

}
