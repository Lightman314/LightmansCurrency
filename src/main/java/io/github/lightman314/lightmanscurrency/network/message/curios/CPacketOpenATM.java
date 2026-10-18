package io.github.lightman314.lightmanscurrency.network.message.curios;

import io.github.lightman314.lightmanscurrency.api.LCTags;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.terminal.TradingTerminalMenu;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.builtin.ItemInInventoryValidator;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCuriosHelper;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketOpenATM extends ClientToServerPacket {

    public static final Type<CPacketOpenATM> TYPE = cType("curios_open_atm");
    private static final CPacketOpenATM INSTANCE = new CPacketOpenATM();
    public static final StreamCodec<ByteBuf,CPacketOpenATM> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private CPacketOpenATM() { super(TYPE); }

    public static void sendPacket() { INSTANCE.send(); }

    @Override
    protected void handle(IPayloadContext context,Player player) {
        if(player.containerMenu == player.inventoryMenu) {
            ItemStack terminalItem = LCCuriosHelper.get().getRandomItem(player, s -> s.is(LCTags.Items.NETWORK_TERMINAL));
            if(!terminalItem.isEmpty())
                player.openMenu(TradingTerminalMenu.createProvider(new ItemInInventoryValidator(terminalItem)));
        }
    }

}