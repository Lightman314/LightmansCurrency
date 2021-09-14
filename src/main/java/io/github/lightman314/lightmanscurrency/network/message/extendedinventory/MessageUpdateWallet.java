package io.github.lightman314.lightmanscurrency.network.message.extendedinventory;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.extendedinventory.IWalletInventory;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

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
	public void encode(MessageUpdateWallet message, FriendlyByteBuf buffer)
	{
		buffer.writeInt(message.entityId);
		buffer.writeItem(message.wallet);
	}
	
	@Override
	public MessageUpdateWallet decode(FriendlyByteBuf buffer)
	{
		return new MessageUpdateWallet(buffer.readInt(), buffer.readItem());
	}
	
	@Override
	public void handle(MessageUpdateWallet message, Supplier<NetworkEvent.Context> supplier)
	{
		supplier.get().enqueueWork(() ->
		{
			Minecraft minecraft = Minecraft.getInstance();
			if(minecraft.level != null)
			{
				Entity entity = minecraft.level.getEntity(message.entityId);
				if(entity instanceof Player)
				{
					Player player = (Player)entity;
					if(player.getInventory() instanceof IWalletInventory)
					{
						((IWalletInventory)player.getInventory()).getWalletItems().set(0, message.wallet);
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}
	
	
}
