package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketSyncWallet extends ServerToClientPacket {

	private static final Type<SPacketSyncWallet> TYPE = new Type<>(VersionUtil.lcResource("s_wallet_sync"));
	public static final Handler<SPacketSyncWallet> HANDLER = new H();

	int entityID;
	ItemStack walletItem;
	boolean visible;
	
	public SPacketSyncWallet(int entityID, ItemStack wallet, boolean visible)
	{
		super(TYPE);
		this.entityID = entityID;
		this.walletItem = wallet;
		this.visible = visible;
	}

	private static void encode(@Nonnull RegistryFriendlyByteBuf buffer, @Nonnull SPacketSyncWallet message) {
		buffer.writeInt(message.entityID);
		writeItem(buffer,message.walletItem);
		buffer.writeBoolean(message.visible);
	}
	private static SPacketSyncWallet decode(@Nonnull RegistryFriendlyByteBuf buffer) { return new SPacketSyncWallet(buffer.readInt(),readItem(buffer),buffer.readBoolean()); }

	private static class H extends Handler<SPacketSyncWallet>
	{
		protected H() { super(TYPE, fancyCodec(SPacketSyncWallet::encode,SPacketSyncWallet::decode)); }
		@Override
		protected void handle(@Nonnull SPacketSyncWallet message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			if(player.level().getEntity(message.entityID) instanceof LivingEntity entity)
			{
				WalletHandler walletHandler = WalletHandler.get(entity);
				walletHandler.syncWallet(message.walletItem);
				walletHandler.setVisible(message.visible);
			}
		}
	}

}
