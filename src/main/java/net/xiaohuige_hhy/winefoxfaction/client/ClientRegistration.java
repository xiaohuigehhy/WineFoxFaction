package net.xiaohuige_hhy.winefoxfaction.client;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntityMaidRenderer;
import net.xiaohuige_hhy.winefoxfaction.WineFoxFactionMod;
import net.xiaohuige_hhy.winefoxfaction.register.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WineFoxFactionMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientRegistration {
	private ClientRegistration() { }
	
	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(ModEntities.WINE_FOX.get(), EntityMaidRenderer::new);
		event.registerEntityRenderer(ModEntities.WINEFOX_BLUE.get(), EntityMaidRenderer::new);
	}
}
