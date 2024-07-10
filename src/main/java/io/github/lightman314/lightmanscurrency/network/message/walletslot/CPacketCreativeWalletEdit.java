package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketCreativeWalletEdit extends ClientToServerPacket {

    private static final Type<CPacketCreativeWalletEdit> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"c_wallet_creative_edit"));
    public static final Handler<CPacketCreativeWalletEdit> HANDLER = new H();

    private final ItemStack newWallet;
    public CPacketCreativeWalletEdit(@Nonnull ItemStack wallet) { super(TYPE); this.newWallet = wallet; }

    private static void encode(@Nonnull RegistryFriendlyByteBuf buffer, @Nonnull CPacketCreativeWalletEdit message) { writeItem(buffer,message.newWallet); }
    private static CPacketCreativeWalletEdit decode(@Nonnull RegistryFriendlyByteBuf buffer) { return new CPacketCreativeWalletEdit(readItem(buffer)); }

    private static class H extends Handler<CPacketCreativeWalletEdit>
    {
        protected H() { super(TYPE, fancyCodec(CPacketCreativeWalletEdit::encode,CPacketCreativeWalletEdit::decode)); }
        @Override
        protected void handle(@Nonnull CPacketCreativeWalletEdit message, @Nonnull IPayloadContext context, @Nonnull Player player) {
            if(player.isCreative())
            {
                WalletHandler walletHandler = WalletHandler.get(player);
                //LightmansCurrency.LogDebug("Updated wallet stack on server from client-side interaction.");
                walletHandler.setWallet(message.newWallet);
            }
            else
            {
                LightmansCurrency.LogWarning(player.getName().getString() + " attempted to set their wallet stack from the client, but they're not currently in creative mode!");
            }
        }
    }

}
