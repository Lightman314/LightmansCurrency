package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import io.github.lightman314.lightmanscurrency.common.capability.wallet.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.WalletCapability;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketCreativeWalletEdit extends ClientToServerPacket {

    public static final Handler<CPacketCreativeWalletEdit> HANDLER = new H();

    private final ItemStack newWallet;
    public CPacketCreativeWalletEdit(ItemStack wallet) { this.newWallet = wallet; }

    @Override
    public void encode(FriendlyByteBuf buffer) { buffer.writeItemStack(this.newWallet, false); }

    private static class H extends Handler<CPacketCreativeWalletEdit>
    {
        @Override
        public CPacketCreativeWalletEdit decode(FriendlyByteBuf buffer) { return new CPacketCreativeWalletEdit(buffer.readItem()); }

        @Override
        protected void handle(CPacketCreativeWalletEdit message, Player player) {
            IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(player);
            if(walletHandler != null && player.isCreative())
            {
                //LightmansCurrency.LogDebug("Updated wallet stack on server from client-side interaction.");
                walletHandler.setWallet(message.newWallet);
            }
        }
    }

}
