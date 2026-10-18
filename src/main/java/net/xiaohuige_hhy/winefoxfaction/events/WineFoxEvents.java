package net.xiaohuige_hhy.winefoxfaction.events;

import com.solegendary.reignofnether.resources.ResourceSources;
import com.solegendary.reignofnether.unit.UnitClientEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.xiaohuige_hhy.winefoxfaction.WineFoxFactionMod;
import net.xiaohuige_hhy.winefoxfaction.units.AbstractWineFoxUnit;
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

	public static void startWineFoxVictoryAnimation(String winnerName) {
		if (FMLEnvironment.dist != Dist.CLIENT || winnerName == null || winnerName.isEmpty()
			|| Minecraft.getInstance().level == null)
			return;
		for (LivingEntity entity : UnitClientEvents.getAllUnits())
			if (entity instanceof AbstractWineFoxUnit unit && winnerName.equals(unit.getOwnerName()))
				unit.setVictory(true);
	}

	public static void stopWineFoxVictoryAnimation() {
		if (FMLEnvironment.dist != Dist.CLIENT || Minecraft.getInstance().level == null)
			return;
		for (LivingEntity entity : UnitClientEvents.getAllUnits())
			if (entity instanceof AbstractWineFoxUnit unit)
				unit.setVictory(false);
	}
}
