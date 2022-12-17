package io.github.lightman314.lightmanscurrency.menus;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import io.github.lightman314.lightmanscurrency.common.playertrading.IPlayerTrade;
import io.github.lightman314.lightmanscurrency.core.ModMenus;
import io.github.lightman314.lightmanscurrency.menus.containers.SuppliedContainer;
import io.github.lightman314.lightmanscurrency.menus.slots.DisplaySlot;
import io.github.lightman314.lightmanscurrency.menus.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PlayerTradeMenu extends AbstractContainerMenu {


    public final int tradeID;
    private IPlayerTrade trade;
    public final IPlayerTrade getTradeData() { return this.trade; }

    public final Player player;

    public boolean isClient() { return this.player.level.isClientSide; }
    public boolean isServer() { return !this.isClient(); }

    public final boolean isHost() { return this.trade.isHost(this.player); }
    public final int myState() { return this.isHost() ? this.trade.getHostState() : this.trade.getGuestState(); }
    public final int otherState() { return this.isHost() ? this.trade.getGuestState() : this.trade.getHostState(); }

    private final Container hostItems;
    private final Container guestItems;

    public PlayerTradeMenu(int windowID, Inventory inventory, int tradeID, IPlayerTrade trade) {
        super(ModMenus.PLAYER_TRADE.get(), windowID);
        this.player = inventory.player;
        this.tradeID = tradeID;

        this.trade = trade;

        this.hostItems = new SuppliedContainer(() -> this.trade.getHostItems());
        this.guestItems = new SuppliedContainer(() -> this.trade.getGuestItems());

        Container leftSideContainer = this.isHost() ? this.hostItems : this.guestItems;
        Container rightSideContainer = this.isHost() ? this.guestItems : this.hostItems;

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
    public boolean stillValid(@NotNull Player player) { return true; }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        if(this.isClient() || this.trade.isCompleted())
            return;
        LightmansCurrency.LogWarning("Player Trade Menu was closed by the " + (this.isHost() ? "host" : "guest") + ", but the trade was not completed!");
        //Give items in inventory back to player
        this.clearContainer(player, this.trade.isHost(this.player) ? this.trade.getHostItems() : this.trade.getGuestItems());
    }

    public CoinValue getAvailableFunds() { return WalletCapability.getWalletMoney(this.player); }

    public void onTradeChange() { }

    public final void reloadTrade(IPlayerTrade trade) {
        if(this.isClient()) {
            this.trade = trade;
            this.onTradeChange();
        }
        else
            LightmansCurrency.LogWarning("Attempted to reload the trade on the server-side menu.");

    }

}