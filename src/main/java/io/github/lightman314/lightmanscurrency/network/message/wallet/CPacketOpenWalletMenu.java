package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletItem;
import io.github.lightman314.lightmanscurrency.features.wallet.menu.WalletMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketOpenWalletMenu extends ClientToServerPacket {

    public static final Type<CPacketOpenWalletMenu> TYPE = cType("wallet_open_menu");
    public static final CPacketOpenWalletMenu INSTANCE = new CPacketOpenWalletMenu();

    private CPacketOpenWalletMenu() { super(TYPE); }

    public static void sendToServer() { INSTANCE.send(); }

    @Override
    protected void handle(IPayloadContext context, Player player) {
        if(WalletItem.isWallet(LCApi.getCoinAPI().getEquippedWallet(player)))
            WalletMenu.openMenu(player,-1);
        else
            player.sendSystemMessage(WalletItem.MESSAGE_WALLET_NONE_EQUIPPED.get());
    }

}