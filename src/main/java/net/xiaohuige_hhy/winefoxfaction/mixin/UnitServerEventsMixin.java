package net.xiaohuige_hhy.winefoxfaction.mixin;

import com.solegendary.reignofnether.resources.ResourceSources;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.packets.UnitSyncClientboundPacket;

import net.minecraft.world.entity.animal.Chicken;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.xiaohuige_hhy.winefoxfaction.entities.WineFoxSnowball;
import net.xiaohuige_hhy.winefoxfaction.units.WineFoxSalespersonUnit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(UnitServerEvents.class)
public class UnitServerEventsMixin {

	@Inject(at = @At("HEAD"), method = "shouldIgnoreKnockback", cancellable = true, remap = false)
	private static void shouldIgnoreKnockback(LivingDamageEvent evt, CallbackInfoReturnable<Boolean> cir) {
		if (evt.getSource().getDirectEntity() instanceof WineFoxSnowball)
			cir.setReturnValue(true);
	}

	@Inject(at = @At("HEAD"), method = "onLivingDeath", remap = false)
	private static void injectSalespersonHunterExp(LivingDeathEvent evt, CallbackInfo ci) {
		if (evt.getSource().getEntity() instanceof WineFoxSalespersonUnit sUnit &&
			ResourceSources.isHuntableAnimal(evt.getEntity())) {
			sUnit.incrementExp(1);
			if (!(evt.getEntity() instanceof Chicken))
				sUnit.incrementExp(1);
		}
	}

	@Redirect(
		method = "onWorldTick",
		remap = false,
		at = @At(
			value = "INVOKE",
			target = "Lcom/solegendary/reignofnether/unit/packets/UnitSyncClientboundPacket;sendSyncResourcesPacket(Lcom/solegendary/reignofnether/unit/interfaces/Unit;)V"
		)
	)
	private static void redirectResyncSalespersonVeteran(Unit unit) {
		UnitSyncClientboundPacket.sendSyncResourcesPacket(unit);
		if (unit instanceof WineFoxSalespersonUnit sUnit && sUnit.isVeteran())
			UnitSyncClientboundPacket.makeVillagerVeteran(sUnit);
	}

}
