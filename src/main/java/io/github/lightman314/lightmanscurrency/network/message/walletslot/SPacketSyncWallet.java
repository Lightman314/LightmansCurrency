package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import io.github.lightman314.lightmanscurrency.common.capability.wallet.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.WalletCapability;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SPacketSyncWallet extends ServerToClientPacket {

	public static final Handler<SPacketSyncWallet> HANDLER = new H();

	int entityID;
	ItemStack walletItem;
	boolean visible;
	
	public SPacketSyncWallet(int entityID, ItemStack wallet, boolean visible)
	{
		this.entityID = entityID;
		this.walletItem = wallet;
		this.visible = visible;
	}
	
	public void encode(@Nonnull FriendlyByteBuf buffer) {
		buffer.writeInt(this.entityID);
		buffer.writeItemStack(this.walletItem, false);
		buffer.writeBoolean(this.visible);
	}

	private static class H extends Handler<SPacketSyncWallet>
	{
		@Nonnull
		@Override
		public SPacketSyncWallet decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketSyncWallet(buffer.readInt(), buffer.readItem(), buffer.readBoolean()); }
		@Override
		protected void handle(@Nonnull SPacketSyncWallet message, @Nullable ServerPlayer sender) {
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
						walletHandler.clean();
					}
				}
			}
		}
	}

}
