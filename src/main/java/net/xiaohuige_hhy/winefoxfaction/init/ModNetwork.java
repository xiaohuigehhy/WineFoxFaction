package net.xiaohuige_hhy.winefoxfaction.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.xiaohuige_hhy.winefoxfaction.WineFoxFactionMod;
import net.xiaohuige_hhy.winefoxfaction.network.WineFoxBlueSyncClientboundPacket;

public final class ModNetwork {

	private static final String PROTOCOL_VERSION = "2";

	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
		ResourceLocation.fromNamespaceAndPath(WineFoxFactionMod.MOD_ID, "main"),
		() -> PROTOCOL_VERSION,
		PROTOCOL_VERSION::equals,
		PROTOCOL_VERSION::equals);

	private static int index = 0;

	private ModNetwork() {
	}

	public static void init() {
		INSTANCE.messageBuilder(WineFoxBlueSyncClientboundPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(WineFoxBlueSyncClientboundPacket::encode)
			.decoder(WineFoxBlueSyncClientboundPacket::new)
			.consumerMainThread(WineFoxBlueSyncClientboundPacket::handle)
			.add();
	}
}
