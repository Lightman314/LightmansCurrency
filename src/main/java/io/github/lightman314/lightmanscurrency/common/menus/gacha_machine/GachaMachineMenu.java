package io.github.lightman314.lightmanscurrency.common.menus.gacha_machine;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.LCItemStackHandler;
import io.github.lightman314.lightmanscurrency.api.misc.menus.slots.MoneySlot;
import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.MoneyInventory;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.ITraderSource;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.menu.customer.AbstractTraderMenu;
import io.github.lightman314.lightmanscurrency.api.traders.tracking.TrackingLevel;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GachaMachineMenu extends AbstractTraderMenu {

    private final long traderID;
    private long trackingID = -1;

    @Nullable
    public TraderData getTrader() { return TraderAPI.getApi().GetTrader(this,this.traderID); }
    @Nullable
    public final <T extends TraderNode> T getNode(TraderNodeType<T> type) {
        TraderData trader = this.getTrader();
        if(trader != null)
            return trader.getNode(type);
        return null;
    }

    private final MoneyInventory coins;

    List<Slot> coinSlots = new ArrayList<>();

    private final List<ItemStack> rewards = new ArrayList<>();
    public final boolean hasPendingReward() { return !this.rewards.isEmpty(); }
    public final ItemStack getNextReward() { if(this.rewards.isEmpty()) return null; return this.rewards.getFirst(); }

    public final ItemStack getAndRemoveNextReward()
    {
        if(this.rewards.isEmpty())
            return ItemStack.EMPTY;
        return this.rewards.removeFirst();
    }

    public GachaMachineMenu(int windowID, Inventory inventory, long traderID, MenuValidator validator) {
        super(ModMenus.GACHA_MACHINE.get(), windowID, inventory,validator);
        this.traderID = traderID;
        this.coins = new MoneyInventory(this.player,5);

        this.addValidator(this.validator);
        this.addValidator(() -> this.getTrader() != null);

        //Player items
        for(int y = 0; y < 3; y++)
        {
            for(int x = 0; x < 9; x++)
            {
                this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 140 + y * 18));
            }
        }
        //Player hotbar
        for(int x = 0; x < 9; x++)
        {
            this.addSlot(new Slot(inventory, x, 8 + x * 18, 198));
        }

        //Coin Slots
        for(int x = 0; x < this.coins.getSlots(); x++)
        {
            this.coinSlots.add(this.addSlot(new MoneySlot(this.coins, x, 8 + (x + 4) * 18, 108)));
        }

        TraderData trader = this.getTrader();
        if(trader != null)
        {
            trader.userOpen(this.player);
            this.trackingID = trader.requestTracking(this.player, TrackingLevel.CUSTOMER);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player playerEntity, int index)
    {

        ItemStack clickedStack = ItemStack.EMPTY;

        Slot slot = this.slots.get(index);

        if(slot != null && slot.hasItem())
        {
            ItemStack slotStack = slot.getItem();
            clickedStack = slotStack.copy();
            if(index < 36)
            {
                //Move from items to coin slots
                if(!this.moveItemStackTo(slotStack, 36, this.slots.size(), false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if(index < this.slots.size())
            {
                //Move from coin slots to items
                if(!this.moveItemStackTo(slotStack, 0, 36, false))
                {
                    return ItemStack.EMPTY;
                }
            }

            if(slotStack.isEmpty())
            {
                slot.set(ItemStack.EMPTY);
            }
            else
            {
                slot.setChanged();
            }
        }

        return clickedStack;

    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        //Force-give rewards if closed before reward is handled
        for(ItemStack reward : this.rewards)
            ItemHandlerHelper.giveItemToPlayer(player,reward);
        this.rewards.clear();
        //Clear the coin slots
        this.clearContainer(player,this.coins);
        //Close the trader
        TraderData trader = this.getTrader();
        if(trader != null)
        {
            trader.userClose(this.player);
            trader.endTracking(this.player,this.trackingID);
        }
    }

    @Nullable
    @Override
    public ITraderSource getTraderSource() { return TraderAPI.getApi().GetTrader(this,this.traderID); }

    @Override
    public TradeContext getContext(@Nullable TraderData trader) { return this.getContext(); }

    public final TradeContext getContext() { return this.getContextForHolder(null); }

    public final TradeContext getContextForHolder(@Nullable IItemHandler rewardHolder)
    {
        TradeContext.Builder builder = TradeContext.create(this.getTrader(),this.player,this.validator.isThroughNetwork).withCoinSlots(this.coins);
        if(rewardHolder != null)
            builder.withItemHandler(rewardHolder);
        return builder.build();
    }

    @Override
    protected void executeTrade(int traderIndex, int tradeIndex) { }

    private void ExecuteTrades(int count)
    {
        if(!this.rewards.isEmpty())
            return;
        TraderData trader = this.getTrader();
        if(trader != null)
        {
            boolean flag = true;
            for(int i = 0; flag && i < count; ++i)
            {
                LCItemStackHandler result = new LCItemStackHandler(1);
                if(trader.TryExecuteTrade(this.getContextForHolder(result),0).isSuccess())
                {
                    if(result.isEmpty())
                        LightmansCurrency.LogError("Successful Gacha Machine Trade executed, but no item was received!");
                    else
                        this.rewards.add(result.getStackInSlot(0));
                }
                else
                    flag = false;
            }
            if(!this.rewards.isEmpty())
            {
                CompoundTag rewardData = new CompoundTag();
                this.SendMessageToClient(this.builder().setList("SyncRewards",this.rewards,ModLazyPackets.ITEM_STACK));
            }
        }
    }

    public boolean GiveNextReward()
    {
        ItemStack reward = this.getAndRemoveNextReward();
        if(reward.isEmpty())
            return false;
        ItemHandlerHelper.giveItemToPlayer(this.player,reward);
        return true;
    }

    @Override
    public void processMessage(LazyPacketData message) {
        if(message.contains("CollectMoney"))
            this.collectMoney();
        if(message.contains("ExecuteTrade"))
        {
            if(!this.rewards.isEmpty())
                return;
            this.ExecuteTrades(message.getInt("ExecuteTrade"));
        }
        if(message.contains("GiveNextReward"))
        {
            this.GiveNextReward();
        }
        if(message.contains("AnimationsCompleted"))
        {
            //Give next reward while a reward is still present
            while(this.GiveNextReward()) { }
        }
        if(message.contains("SyncRewards"))
        {
            this.rewards.clear();
            CompoundTag rewardData = message.getTag("SyncRewards");
            this.rewards.addAll(message.getList("SyncRewards",ModLazyPackets.ITEM_STACK));
        }
    }

}
