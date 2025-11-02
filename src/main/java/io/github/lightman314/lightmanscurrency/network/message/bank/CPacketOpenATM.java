package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.common.items.PortableATMItem;
import io.github.lightman314.lightmanscurrency.common.menus.validation.EasyMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.ItemValidator;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketOpenATM extends ClientToServerPacket {

	public static Handler<CPacketOpenATM> HANDLER = new H();

    private final Item portableATM;
	public CPacketOpenATM(Item portableATM) { this.portableATM = portableATM; }

    @Override
    public void encode(FriendlyByteBuf buffer) { buffer.writeResourceLocation(ForgeRegistries.ITEMS.getKey(this.portableATM)); }

    private static class H extends Handler<CPacketOpenATM>
	{
        @Override
        public CPacketOpenATM decode(FriendlyByteBuf buffer) { return new CPacketOpenATM(ForgeRegistries.ITEMS.getValue(buffer.readResourceLocation())); }

        @Override
		public void handle(CPacketOpenATM message, Player player) {
            if(QuarantineAPI.IsDimensionQuarantined(player))
                EasyText.sendMessage(player, LCText.MESSAGE_DIMENSION_QUARANTINED_BANK.getWithStyle(ChatFormatting.GOLD));
            else if(player instanceof ServerPlayer sp)
                NetworkHooks.openScreen(sp, PortableATMItem.getMenuProvider(message.portableATM), EasyMenu.encoder(new ItemValidator(message.portableATM)));
		}
	}

}
