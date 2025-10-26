package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.common.items.PortableATMItem;
import io.github.lightman314.lightmanscurrency.common.menus.validation.EasyMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.ItemValidator;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketOpenATM extends ClientToServerPacket {

	private static final Type<CPacketOpenATM> TYPE = new Type<>(VersionUtil.lcResource("c_open_atm"));
	public static Handler<CPacketOpenATM> HANDLER = new H();

    private final Item portableATM;
	public CPacketOpenATM(Item portableATM) { super(TYPE); this.portableATM = portableATM; }

    private static void encode(FriendlyByteBuf buffer,CPacketOpenATM message) { buffer.writeResourceLocation(BuiltInRegistries.ITEM.getKey(message.portableATM)); }
    private static CPacketOpenATM decode(FriendlyByteBuf buffer) { return new CPacketOpenATM(BuiltInRegistries.ITEM.get(buffer.readResourceLocation())); }

	private static class H extends Handler<CPacketOpenATM>
	{
		protected H() { super(TYPE, StreamCodec.of(CPacketOpenATM::encode,CPacketOpenATM::decode)); }
		@Override
		public void handle(CPacketOpenATM message, IPayloadContext context, Player player) {
			if(QuarantineAPI.IsDimensionQuarantined(player))
				EasyText.sendMessage(player, LCText.MESSAGE_DIMENSION_QUARANTINED_BANK.getWithStyle(ChatFormatting.GOLD));
			else
				player.openMenu(PortableATMItem.getMenuProvider(message.portableATM), EasyMenu.encoder(new ItemValidator(message.portableATM)));
		}
	}

}
