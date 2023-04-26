package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.function.Supplier;

public class MessageChestQuickCollect {

    private final boolean allowHidden;

    public MessageChestQuickCollect(boolean allowHidden) { this.allowHidden = allowHidden; }

    public static void encode(MessageChestQuickCollect message, FriendlyByteBuf buffer) { buffer.writeBoolean(message.allowHidden); }

    public static MessageChestQuickCollect decode(FriendlyByteBuf buffer) { return new MessageChestQuickCollect(buffer.readBoolean()); }

    public static void handle(MessageChestQuickCollect message, Supplier<Context> supplier) {
        supplier.get().enqueueWork(() ->
        {
            ServerPlayer player = supplier.get().getSender();
            if(player != null)
            {
                if(player.containerMenu instanceof ChestMenu menu)
                    WalletItem.QuickCollect(player, menu.getContainer(), message.allowHidden);
            }
        });
        supplier.get().setPacketHandled(true);
    }

}