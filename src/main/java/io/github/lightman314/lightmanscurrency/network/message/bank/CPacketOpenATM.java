package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.common.items.PortableATMItem;
import io.github.lightman314.lightmanscurrency.common.menus.validation.EasyMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.ItemValidator;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketOpenATM extends ClientToServerPacket {

	private static final Type<CPacketOpenATM> TYPE = cType("open_atm");
    private static final StreamCodec<RegistryFriendlyByteBuf,CPacketOpenATM> STREAM_CODEC = ByteBufCodecs.registry(Registries.ITEM)
            .map(CPacketOpenATM::new,p -> p.portableATM);
	public static Handler<CPacketOpenATM> HANDLER = new H();

    private final Item portableATM;
	public CPacketOpenATM(Item portableATM) { super(TYPE); this.portableATM = portableATM; }

	private static class H extends Handler<CPacketOpenATM>
	{
		protected H() { super(TYPE,STREAM_CODEC); }
		@Override
		public void handle(CPacketOpenATM message, IPayloadContext context, Player player) {
			if(QuarantineAPI.IsDimensionQuarantined(player))
				EasyText.sendMessage(player, LCText.MESSAGE_DIMENSION_QUARANTINED_BANK.getWithStyle(ChatFormatting.GOLD));
			else
				player.openMenu(PortableATMItem.getMenuProvider(message.portableATM), EasyMenu.encoder(new ItemValidator(message.portableATM)));
		}
	}

}
