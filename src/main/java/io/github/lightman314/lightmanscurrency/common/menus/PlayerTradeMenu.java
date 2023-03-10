package io.github.lightman314.lightmanscurrency.common.menus;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import io.github.lightman314.lightmanscurrency.common.playertrading.IPlayerTrade;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.menus.containers.SuppliedContainer;
import io.github.lightman314.lightmanscurrency.common.menus.slots.DisplaySlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class PlayerTradeMenu extends Container {


    public final int tradeID;
    private IPlayerTrade trade;
    public final IPlayerTrade getTradeData() { return this.trade; }

    public final PlayerEntity player;

    public boolean isClient() { return this.player.level.isClientSide; }
    public boolean isServer() { return !this.isClient(); }

    public final boolean isHost() { return this.trade.isHost(this.player); }
    public final int myState() { return this.isHost() ? this.trade.getHostState() : this.trade.getGuestState(); }
    public final int otherState() { return this.isHost() ? this.trade.getGuestState() : this.trade.getHostState(); }

    private final IInventory hostItems;
    private final IInventory guestItems;

    public PlayerTradeMenu(int windowID, PlayerInventory inventory, int tradeID, IPlayerTrade trade) {
        super(ModMenus.PLAYER_TRADE.get(), windowID);
        this.player = inventory.player;
        this.tradeID = tradeID;

        this.trade = trade;

        this.hostItems = new SuppliedContainer(() -> this.trade.getHostItems());
        this.guestItems = new SuppliedContainer(() -> this.trade.getGuestItems());

        IInventory leftSideContainer = this.isHost() ? this.hostItems : this.guestItems;
        IInventory rightSideContainer = this.isHost() ? this.guestItems : this.hostItems;

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
    public @Nonnull ItemStack quickMoveStack(@Nonnull PlayerEntity player, int index) {
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
    public boolean stillValid(@Nonnull PlayerEntity player) { return true; }

    @Override
    public void removed(@Nonnull PlayerEntity player) {
        super.removed(player);
        if(this.isClient() || this.trade.isCompleted())
            return;
        LightmansCurrency.LogWarning("Player Trade Menu was closed by the " + (this.isHost() ? "host" : "guest") + ", but the trade was not completed!");
        //Give items in inventory back to player
        this.clearContainer(player, player.level, this.trade.isHost(this.player) ? this.trade.getHostItems() : this.trade.getGuestItems());
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