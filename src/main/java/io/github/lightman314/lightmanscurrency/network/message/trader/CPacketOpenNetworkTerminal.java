package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.common.menus.providers.TerminalMenuProvider;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.ItemValidator;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet to request the terminal menu be opened via a key-bind<br>
 * Included is the "Terminal" item that must be present in the players inventory or curios slot in order for the menu to remain open
 */
public class CPacketOpenNetworkTerminal extends ClientToServerPacket {

	private static final Type<CPacketOpenNetworkTerminal> TYPE = cType("open_terminal");
    private static final StreamCodec<RegistryFriendlyByteBuf,CPacketOpenNetworkTerminal> STREAM_CODEC = ByteBufCodecs.registry(Registries.ITEM)
            .map(CPacketOpenNetworkTerminal::new,p -> p.item);
	public static final Handler<CPacketOpenNetworkTerminal> HANDLER = new H();

	private final Item item;

	public CPacketOpenNetworkTerminal(Item terminal) { super(TYPE); this.item = terminal; }

	private static class H extends Handler<CPacketOpenNetworkTerminal>
	{
		protected H() { super(TYPE,STREAM_CODEC); }
		@Override
		protected void handle(CPacketOpenNetworkTerminal message, IPayloadContext context, Player player) {
            if(message.item != Items.AIR)
			    TerminalMenuProvider.OpenMenu(player,new ItemValidator(message.item));
		}
	}

}
