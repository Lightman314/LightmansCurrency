package io.github.lightman314.lightmanscurrency.network.message.curios;


import io.github.lightman314.lightmanscurrency.api.LCTags;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.builtin.ItemInInventoryValidator;
import io.github.lightman314.lightmanscurrency.features.atm.ATMMenu;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCuriosHelper;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketOpenTerminal extends ClientToServerPacket {

    public static final Type<CPacketOpenTerminal> TYPE = cType("curios_open_terminal");
    private static final CPacketOpenTerminal INSTANCE = new CPacketOpenTerminal();
    public static final StreamCodec<ByteBuf,CPacketOpenTerminal> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private CPacketOpenTerminal() { super(TYPE); }

    public static void sendPacket() { INSTANCE.send(); }

    @Override
    protected void handle(IPayloadContext context, Player player) {
        if(player.containerMenu == player.inventoryMenu) {
            ItemStack atmItem = LCCuriosHelper.get().getRandomItem(player,s -> s.is(LCTags.Items.ATM));
            if(!atmItem.isEmpty())
                player.openMenu(ATMMenu.createProvider(new ItemInInventoryValidator(atmItem)));
        }
    }

}