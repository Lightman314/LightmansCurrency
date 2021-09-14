package io.github.lightman314.lightmanscurrency.network.message.extendedinventory;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.extendedinventory.IWalletInventory;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageUpdateWallet implements IMessage<MessageUpdateWallet>{

	private int entityId;
	private ItemStack wallet;
	
	public MessageUpdateWallet()
	{
		
	}
	
	public MessageUpdateWallet(int entityId, ItemStack wallet)
	{
		this.entityId = entityId;
		this.wallet = wallet;
	}
	
	@Override
	public void encode(MessageUpdateWallet message, PacketBuffer buffer)
	{
		buffer.writeInt(message.entityId);
		buffer.writeItemStack(message.wallet);
	}
	
	@Override
	public MessageUpdateWallet decode(PacketBuffer buffer)
	{
		return new MessageUpdateWallet(buffer.readInt(), buffer.readItemStack());
	}
	
	@Override
	public void handle(MessageUpdateWallet message, Supplier<NetworkEvent.Context> supplier)
	{
		supplier.get().enqueueWork(() ->
		{
			Minecraft minecraft = Minecraft.getInstance();
			if(minecraft.world != null)
			{
				Entity entity = minecraft.world.getEntityByID(message.entityId);
				if(entity instanceof PlayerEntity)
				{
					PlayerEntity player = (PlayerEntity)entity;
					if(player.inventory instanceof IWalletInventory)
					{
						((IWalletInventory)player.inventory).getWalletItems().set(0, message.wallet);
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}
	
	
}
