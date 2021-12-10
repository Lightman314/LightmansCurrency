package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

public class SPacketGrabbedItem {
	
	ItemStack stack;
	
	public SPacketGrabbedItem(ItemStack stack)
	{
		this.stack = stack;
	}
	
	public static void encode(SPacketGrabbedItem message, FriendlyByteBuf buffer) {
		buffer.writeItemStack(message.stack, false);
	}

	public static SPacketGrabbedItem decode(FriendlyByteBuf buffer) {
		return new SPacketGrabbedItem(buffer.readItem());
	}

	public static void handle(SPacketGrabbedItem message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			Minecraft minecraft = Minecraft.getInstance();
			if(minecraft != null)
			{
				minecraft.player.containerMenu.setCarried(message.stack);
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
