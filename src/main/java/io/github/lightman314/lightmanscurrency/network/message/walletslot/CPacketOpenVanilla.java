package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class CPacketOpenVanilla implements IMessage<CPacketOpenVanilla> {
	
	
	@Override
	public void encode(CPacketOpenVanilla message, PacketBuffer buffer) {
		
	}

	@Override
	public CPacketOpenVanilla decode(PacketBuffer buffer) {
		return new CPacketOpenVanilla();
	}

	@Override
	public void handle(CPacketOpenVanilla message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity sender = supplier.get().getSender();
			if(sender != null)
			{
				ItemStack stack = sender.inventory.getItemStack();
				sender.inventory.setItemStack(ItemStack.EMPTY);
				sender.closeContainer();
				
				if(!stack.isEmpty())
				{
					sender.inventory.setItemStack(stack);
					LightmansCurrencyPacketHandler.instance.send(PacketDistributor.PLAYER.with(() -> sender), new SPacketGrabbedItem(stack));
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
