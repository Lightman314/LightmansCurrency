package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketChestQuickCollect extends ClientToServerPacket {

	private static final Type<CPacketChestQuickCollect> TYPE = cType("wallet_chest_collection");
    private static final StreamCodec<ByteBuf,CPacketChestQuickCollect> STREAM_CODEC = ByteBufCodecs.BOOL
            .map(CPacketChestQuickCollect::new,p -> p.allowSideChains);
	public static final Handler<CPacketChestQuickCollect> HANDLER = new H();

	private final boolean allowSideChains;

	private CPacketChestQuickCollect(boolean allowSideChains) { super(TYPE); this.allowSideChains = allowSideChains; }

	public static void send() { new CPacketChestQuickCollect(LCConfig.CLIENT.chestButtonAllowSideChains.get()).sendToServer(); }

	private static class H extends Handler<CPacketChestQuickCollect>
	{
		protected H() { super(TYPE,STREAM_CODEC); }
		@Override
		protected void handle(CPacketChestQuickCollect message, IPayloadContext context, Player player) {
			if(player.containerMenu instanceof ChestMenu menu)
				WalletItem.QuickCollect(player, menu.getContainer(), message.allowSideChains);
		}
	}

}
