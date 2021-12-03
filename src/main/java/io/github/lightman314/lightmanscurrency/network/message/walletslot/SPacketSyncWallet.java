package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class SPacketSyncWallet implements IMessage<SPacketSyncWallet> {
	
	int entityID;
	ItemStack walletItem;
	
	public SPacketSyncWallet()
	{
		
	}
	
	public SPacketSyncWallet(int entityID, ItemStack wallet)
	{
		this.entityID = entityID;
		this.walletItem = wallet;
	}
	
	@Override
	public void encode(SPacketSyncWallet message, PacketBuffer buffer) {
		buffer.writeInt(message.entityID);
		buffer.writeItemStack(message.walletItem);
	}

	@Override
	public SPacketSyncWallet decode(PacketBuffer buffer) {
		return new SPacketSyncWallet(buffer.readInt(), buffer.readItemStack());
	}

	@Override
	public void handle(SPacketSyncWallet message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			Minecraft minecraft = Minecraft.getInstance();
			if(minecraft != null)
			{
				Entity entity = minecraft.world.getEntityByID(message.entityID);
				if(entity instanceof LivingEntity)
				{
					WalletCapability.getWalletHandler((LivingEntity)entity).ifPresent(walletHandler ->{
						walletHandler.setWallet(message.walletItem);
						//LightmansCurrency.LogInfo("Synced wallet for " + entity.getName().getString());
					});
					//if(!WalletCapability.getWalletHandler((LivingEntity)entity).isPresent())
						//LightmansCurrency.LogWarning("Unable to sync wallet for " + entity.getName().getString());
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
