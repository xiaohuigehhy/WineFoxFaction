package net.xiaohuige_hhy.winefoxfaction.mixin;

import com.solegendary.reignofnether.registrars.EntityRegistrar;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.xiaohuige_hhy.winefoxfaction.productions.WineFoxBlueProd;
import net.xiaohuige_hhy.winefoxfaction.productions.WineFoxLittleProd;
import net.xiaohuige_hhy.winefoxfaction.productions.WineFoxProd;
import net.xiaohuige_hhy.winefoxfaction.productions.WineFoxSalespersonProd;
import net.xiaohuige_hhy.winefoxfaction.register.ModEntities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRegistrar.class)
public class EntityRegistrarMixin {

	@Inject(at = @At("TAIL"), method = "getEntityType", cancellable = true, remap = false)
	private static void getEntityType(String unitName, CallbackInfoReturnable<EntityType<? extends Mob>> cir) {
		if (cir.getReturnValue() == null)
			cir.setReturnValue(
				switch (unitName) {
					case WineFoxProd.itemName -> ModEntities.WINE_FOX.get();
					case WineFoxBlueProd.itemName -> ModEntities.WINEFOX_BLUE.get();
					case WineFoxSalespersonProd.itemName -> ModEntities.WINEFOX_SALESPERSON.get();
					case WineFoxLittleProd.itemName -> ModEntities.WINEFOX_LITTLE.get();

					default -> null;
				});
	}

}
