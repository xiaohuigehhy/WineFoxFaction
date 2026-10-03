package net.xiaohuige_hhy.winefoxfaction.events;

import com.solegendary.reignofnether.resources.ResourceSources;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaohuige_hhy.winefoxfaction.WineFoxFactionMod;
import net.xiaohuige_hhy.winefoxfaction.units.WineFoxSalespersonUnit;

@Mod.EventBusSubscriber(modid = WineFoxFactionMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WineFoxEvents {

	@SubscribeEvent
	public static void onLivingHurt(LivingHurtEvent event) {
		if (event.getSource().getEntity() instanceof WineFoxSalespersonUnit salesperson) {
			LivingEntity target = event.getEntity();
			if (!target.level().isClientSide() && ResourceSources.isHuntableAnimal(target))
				event.setAmount(salesperson.isVeteran() ? 2.0f : salesperson.getUnitAttackDamage());
		}
	}
}
