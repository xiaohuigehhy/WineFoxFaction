package net.xiaohuige_hhy.winefoxfaction.mixin;

import com.github.tartaricacid.touhoulittlemaid.api.entity.IMaid;
import com.github.tartaricacid.touhoulittlemaid.capability.GeckoMaidEntityCapabilityProvider;
import com.github.tartaricacid.touhoulittlemaid.client.entity.GeckoMaidEntity;

import net.minecraft.world.entity.Mob;
import net.xiaohuige_hhy.winefoxfaction.client.WineFoxBlueGeckoEntity;
import net.xiaohuige_hhy.winefoxfaction.client.WineFoxGeckoMaidEntity;
import net.xiaohuige_hhy.winefoxfaction.client.WineFoxSalesPersonGeckoEntity;
import net.xiaohuige_hhy.winefoxfaction.units.AbstractWineFoxUnit;
import net.xiaohuige_hhy.winefoxfaction.units.WineFoxBlueUnit;
import net.xiaohuige_hhy.winefoxfaction.units.WineFoxSalespersonUnit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GeckoMaidEntityCapabilityProvider.class)
public class GeckoMaidEntityCapabilityProviderMixin {
	
	@Redirect(
		method = "createCapability",
		remap = false,
		at = @At(
			value = "NEW",
			target = "Lcom/github/tartaricacid/touhoulittlemaid/client/entity/GeckoMaidEntity;"
		)
	)
	private GeckoMaidEntity<Mob> createWineFoxGecko(Mob entity, IMaid maid) {
		if (entity instanceof WineFoxBlueUnit unit)
			return new WineFoxBlueGeckoEntity<>(entity, unit);
		if (entity instanceof WineFoxSalespersonUnit)
			return new WineFoxSalesPersonGeckoEntity<>(entity, maid);
		if (entity instanceof AbstractWineFoxUnit)
			return new WineFoxGeckoMaidEntity<>(entity, maid);
		return new GeckoMaidEntity<>(entity, maid);
	}
}
