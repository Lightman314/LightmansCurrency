package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import io.github.lightman314.lightmanscurrency.common.capability.wallet.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.WalletCapability;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
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
	
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeInt(this.entityID);
		buffer.writeItemStack(this.walletItem, false);
		buffer.writeBoolean(this.visible);
	}

	private static class H extends Handler<SPacketSyncWallet>
	{
		@Override
		public SPacketSyncWallet decode(FriendlyByteBuf buffer) { return new SPacketSyncWallet(buffer.readInt(), buffer.readItem(), buffer.readBoolean()); }
		@Override
		protected void handle(SPacketSyncWallet message, Player player) {
            Entity entity = player.level().getEntity(message.entityID);
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
