package net.xiaohuige_hhy.winefoxfaction.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.xiaohuige_hhy.winefoxfaction.init.ModNetwork;
import net.xiaohuige_hhy.winefoxfaction.units.WineFoxBlueUnit;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class WineFoxBlueSyncClientboundPacket {

	private final boolean attacking;
	private final int unitId;

	public WineFoxBlueSyncClientboundPacket(boolean attacking, int unitId) {
		this.attacking = attacking;
		this.unitId = unitId;
	}

	public WineFoxBlueSyncClientboundPacket(FriendlyByteBuf buffer) {
		this.attacking = buffer.readBoolean();
		this.unitId = buffer.readInt();
	}

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeBoolean(this.attacking);
		buffer.writeInt(this.unitId);
	}
	
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		final var success = new AtomicBoolean(false);
		
		ctx.get().enqueueWork(() -> {
			if (Minecraft.getInstance().level == null) return;
			Entity entity = Minecraft.getInstance().level.getEntity(this.unitId);
			if (entity instanceof WineFoxBlueUnit wineFoxBlueUnit) {    
				wineFoxBlueUnit.isHolding = this.attacking;
			}
		});
		ctx.get().setPacketHandled(true);
		success.get();
	}

	public static void send(boolean attacking, Entity entity) {
		if (!(entity instanceof WineFoxBlueUnit))
			return;
		if (entity.level().isClientSide())
			return;
		ModNetwork.INSTANCE.send(PacketDistributor.ALL.noArg(), new WineFoxBlueSyncClientboundPacket(attacking, entity.getId()));
	}
}
