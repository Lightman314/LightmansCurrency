package io.github.lightman314.lightmanscurrency.common.menus;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.WalletCapability;
import io.github.lightman314.lightmanscurrency.common.playertrading.IPlayerTrade;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.menus.containers.SuppliedContainer;
import io.github.lightman314.lightmanscurrency.common.menus.slots.DisplaySlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.SimpleSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerTradeMenu extends LazyMessageMenu {


    public final int tradeID;
    private IPlayerTrade trade;
    public final IPlayerTrade getTradeData() { return this.trade; }

    public final Player player;

    public boolean isClient() { return this.player.level().isClientSide; }
    public boolean isServer() { return !this.isClient(); }

    public final boolean isHost() { return this.trade.isHost(this.player); }
    public final int myState() { return this.isHost() ? this.trade.getHostState() : this.trade.getGuestState(); }
    public final int otherState() { return this.isHost() ? this.trade.getGuestState() : this.trade.getHostState(); }

    private Consumer<Component> chatReceiver = c -> {};
    public final void setChatReceiver(@Nonnull Consumer<Component> chatReceiver) { this.chatReceiver = chatReceiver; }

    public void hideSlots() { SimpleSlot.SetInactive(this); }
    public void showSlots() { SimpleSlot.SetActive(this); }

    public PlayerTradeMenu(int windowID, Inventory inventory, int tradeID, IPlayerTrade trade) {
        super(ModMenus.PLAYER_TRADE.get(), windowID, inventory);
        this.player = inventory.player;
        this.tradeID = tradeID;

        this.trade = trade;

        Container hostItems = new SuppliedContainer(() -> this.getTradeData().getHostItems());
        Container guestItems = new SuppliedContainer(() -> this.getTradeData().getGuestItems());

        Container leftSideContainer = this.isHost() ? hostItems : guestItems;
        Container rightSideContainer = this.isHost() ? guestItems : hostItems;

        //Add Left-Side Slots (Interactable by this player)
        for(int y = 0; y < 4; ++y)
        {
            for(int x = 0; x < 3; ++x)
            {
                this.addSlot(new SimpleSlot(leftSideContainer, x + y * 3, 8 + x * 18, 69 + 30 + y * 18));
            }
        }

        //Add Right-Side Slots (Not Interactable by this player)
        for(int y = 0; y < 4; ++y)
        {
            for(int x = 0; x < 3; ++x)
            {
                this.addSlot(new DisplaySlot(rightSideContainer, x + y * 3, 116 + x * 18, 69 + 30 + y * 18));
            }
        }

        //Player's Inventory
        for(int y = 0; y < 3; ++y) {
            for(int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 69 + 140 + y * 18));
            }
        }

        //Player's hotbar
        for(int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(inventory, x, 8 + x * 18, 69 + 198));
        }

    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack clickedStack = ItemStack.EMPTY;

        Slot slot = this.slots.get(index);

        if(slot.hasItem())
        {
            ItemStack slotStack = slot.getItem();
            clickedStack = slotStack.copy();
            if(index < IPlayerTrade.ITEM_COUNT)
            {
                if(!this.moveItemStackTo(slotStack, IPlayerTrade.ITEM_COUNT * 2, this.slots.size(), true))
                    return ItemStack.EMPTY;
            }
            else if(!this.moveItemStackTo(slotStack, 0, IPlayerTrade.ITEM_COUNT, false))
                return ItemStack.EMPTY;

            if(slotStack.isEmpty())
                slot.set(ItemStack.EMPTY);
            else
                slot.setChanged();
        }
        return clickedStack;
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        if(this.isClient() || this.trade.isCompleted())
            return;
        LightmansCurrency.LogWarning("Player Trade Menu was closed by the " + (this.isHost() ? "host" : "guest") + ", but the trade was not completed!");
        //Give items in inventory back to player
        this.clearContainer(player, this.trade.isHost(this.player) ? this.trade.getHostItems() : this.trade.getGuestItems());
    }

    public MoneyView getAvailableFunds() { return WalletCapability.getWalletMoney(this.player); }

    public void onTradeChange() { }

    public final void reloadTrade(IPlayerTrade trade) {
        if(this.isClient()) {
            this.trade = trade;
            this.onTradeChange();
        }
        else
            LightmansCurrency.LogWarning("Attempted to reload the trade on the server-side menu.");
    }

    public void SendChatToServer(@Nonnull String message) { this.SendMessageToServer(LazyPacketData.simpleString("AddChat", message)); }

    @Override
    public void HandleMessage(@Nonnull LazyPacketData message) {
        if(message.contains("AddChat") && this.isServer())
        {
            //Send chat message to both players
            IPlayerTrade trade = this.getTradeData();
            if(trade != null)
            {
                //Format Chat Message as <PlayerName> Message typed goes here
                Component m = EasyText.literal("<").append(this.player.getName()).append("> ").append(message.getString("AddChat"));
                //Log chat message so that admins can catch innapropriate correspondense
                LightmansCurrency.LogInfo("Player Trade Chat: " + m.getString());
                this.SendChatTo(trade.getHostID(), m);
                this.SendChatTo(trade.getGuestID(), m);
            }
        }
        if(message.contains("ReceiveChat") && this.chatReceiver != null)
            this.chatReceiver.accept(message.getText("ReceiveChat"));
    }

    private void SendChatTo(@Nonnull UUID id, @Nonnull Component message)
    {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server == null)
            return;
        ServerPlayer player = server.getPlayerList().getPlayer(id);
        if(player == null)
            return;
        if(player.containerMenu instanceof PlayerTradeMenu menu)
            menu.SendMessage(LazyPacketData.simpleText("ReceiveChat", message));
    }

}
