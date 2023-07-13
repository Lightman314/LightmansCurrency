package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.capability.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;

public class SPacketSyncWallet {
	
	int entityID;
	ItemStack walletItem;
	boolean visible;
	
	public SPacketSyncWallet(int entityID, ItemStack wallet, boolean visible)
	{
		this.entityID = entityID;
		this.walletItem = wallet;
		this.visible = visible;
	}
	
	public static void encode(SPacketSyncWallet message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.entityID);
		buffer.writeItemStack(message.walletItem, false);
		buffer.writeBoolean(message.visible);
	}

	public static SPacketSyncWallet decode(FriendlyByteBuf buffer) {
		return new SPacketSyncWallet(buffer.readInt(), buffer.readItem(), buffer.readBoolean());
	}

	public static void handle(SPacketSyncWallet message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			Minecraft minecraft = Minecraft.getInstance();
			if(minecraft != null)
			{
				Entity entity = minecraft.level.getEntity(message.entityID);
				if(entity instanceof LivingEntity livingEntity)
				{
					IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(livingEntity);
					if(walletHandler != null)
					{
						walletHandler.syncWallet(message.walletItem);
						walletHandler.setVisible(message.visible);
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
