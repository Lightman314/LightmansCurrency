package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class SPacketGrabbedItem implements IMessage<SPacketGrabbedItem> {
	
	
	ItemStack stack;
	
	public SPacketGrabbedItem() {}
	
	public SPacketGrabbedItem(ItemStack stack)
	{
		this.stack = stack;
	}
	
	@Override
	public void encode(SPacketGrabbedItem message, PacketBuffer buffer) {
		buffer.writeItemStack(message.stack);
	}

	@Override
	public SPacketGrabbedItem decode(PacketBuffer buffer) {
		return new SPacketGrabbedItem(buffer.readItemStack());
	}

	@SuppressWarnings("resource")
	@Override
	public void handle(SPacketGrabbedItem message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ClientPlayerEntity clientPlayer = Minecraft.getInstance().player;
			
			if(clientPlayer != null)
			{
				clientPlayer.inventory.setItemStack(message.stack);
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
