package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

public class SPacketOpenVanillaResponse implements IMessage<SPacketOpenVanillaResponse>{
	
	public void encode(SPacketOpenVanillaResponse message, FriendlyByteBuf buffer) { }

	public SPacketOpenVanillaResponse decode(FriendlyByteBuf buffer) {
		return new SPacketOpenVanillaResponse();
	}

	@OnlyIn(Dist.CLIENT)
	public void handle(SPacketOpenVanillaResponse message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			Minecraft minecraft = Minecraft.getInstance();
			if(minecraft != null)
			{
				Player player = minecraft.player;
				LightmansCurrency.PROXY.openInventoryScreen(player);
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
