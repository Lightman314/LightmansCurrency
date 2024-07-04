package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketChestQuickCollect extends ClientToServerPacket {

	private static final Type<CPacketChestQuickCollect> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"c_wallet_chest_collection"));
	public static final Handler<CPacketChestQuickCollect> HANDLER = new H();

	private final boolean allowSideChains;

	private CPacketChestQuickCollect(boolean allowSideChains) { super(TYPE); this.allowSideChains = allowSideChains; }

	public static void sendToServer() { new CPacketChestQuickCollect(LCConfig.CLIENT.chestButtonAllowSideChains.get()).send(); }

	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull CPacketChestQuickCollect message) { buffer.writeBoolean(message.allowSideChains); }
	private static CPacketChestQuickCollect decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketChestQuickCollect(buffer.readBoolean()); }

	private static class H extends Handler<CPacketChestQuickCollect>
	{
		protected H() { super(TYPE, easyCodec(CPacketChestQuickCollect::encode,CPacketChestQuickCollect::decode)); }
		@Override
		protected void handle(@Nonnull CPacketChestQuickCollect message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			if(player.containerMenu instanceof ChestMenu menu)
				WalletItem.QuickCollect(player, menu.getContainer(), message.allowSideChains);
		}
	}

}
