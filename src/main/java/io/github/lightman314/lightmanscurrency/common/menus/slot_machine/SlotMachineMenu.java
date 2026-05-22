package io.github.lightman314.lightmanscurrency.common.menus.slot_machine;

import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.MoneyInventory;
import io.github.lightman314.lightmanscurrency.api.misc.menus.slots.MoneySlot;
import io.github.lightman314.lightmanscurrency.api.traders.ITraderSource;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;

import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.menu.customer.AbstractTraderMenu;
import io.github.lightman314.lightmanscurrency.api.traders.tracking.TrackingLevel;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SlotMachineMenu extends AbstractTraderMenu {

    private final long traderID;
    private long trackingKey = -1;

    @Nullable
    public final TraderData getTrader() { return TraderAPI.getApi().GetTrader(this.isClient(),this.traderID); }
    @Nullable
    public final <T extends TraderNode> T getNode(TraderNodeType<T> type) {
        TraderData trader = this.getTrader();
        if(trader != null)
            return trader.getNode(type);
        return null;
    }

    private final MoneyInventory coins;

    List<Slot> coinSlots = new ArrayList<>();

    private final List<ResultHolder> rewards = new ArrayList<>();
    public final boolean hasPendingReward() { return !this.rewards.isEmpty(); }
    public final ResultHolder getNextReward() { if(this.rewards.isEmpty()) return null; return this.rewards.getFirst(); }

    public final ResultHolder getAndRemoveNextReward()
    {
        if(this.rewards.isEmpty())
            return null;
        return this.rewards.removeFirst();
    }

    public SlotMachineMenu(int windowID, Inventory inventory, long traderID, MenuValidator validator) {
        super(ModMenus.SLOT_MACHINE.get(), windowID, inventory,validator);
        this.traderID = traderID;
        this.coins = new MoneyInventory(this.player,5);

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
            this.trackingKey = trader.requestTracking(this.player, TrackingLevel.CUSTOMER);
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
        for(ResultHolder reward : this.rewards)
            reward.giveToPlayer(this.player);
        this.rewards.clear();
        //Clear the coin slots
        this.clearContainer(player, this.coins);
        //Close the trader
        TraderData trader = this.getTrader();
        if(trader != null)
        {
            trader.userClose(this.player);
            trader.endTracking(this.player,this.trackingKey);
        }
    }

    @Nullable
    @Override
    public ITraderSource getTraderSource() { return TraderAPI.getApi().GetTrader(this,this.traderID); }

    @Override
    public TradeContext getContext(@Nullable TraderData trader) { return this.getContext(); }

    public final TradeContext getContext() { return this.getContextForHolder(null); }

    public final TradeContext getContextForHolder(@Nullable ResultHolder rewardHolder)
    {
        TradeContext.Builder builder = TradeContext.create(this.getTrader(),this.player,this.validator.isThroughNetwork).withCoinSlots(this.coins);
        if(rewardHolder != null)
            builder.withItemHandler(rewardHolder.itemHandler()).withMoneyHolder(rewardHolder.moneyHolder()).withCustomData(ResultHolder.CONTEXT_KEY,rewardHolder);
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
                ResultHolder result = new ResultHolder();
                if(trader.TryExecuteTrade(this.getContextForHolder(result), 0).isSuccess())
                    this.rewards.add(result); //Always add the reward now, as "failing" is now a valid result
                else
                    flag = false;
            }
            if(!this.rewards.isEmpty())
                this.SendMessageToClient(this.builder().setList("SyncRewards",this.rewards,ModLazyPackets.SLOT_MACHINE_RESULT));
        }

    }

    public boolean GiveNextReward()
    {
        ResultHolder nextReward = this.getAndRemoveNextReward();
        if(nextReward != null)
        {
            nextReward.giveToPlayer(this.player);
            return true;
        }
        return false;
    }

    @Override
    public void processMessage(LazyPacketData message) {
        super.processMessage(message);
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
        if(message.contains("SyncRewards") && this.isClient())
        {
            this.rewards.clear();
            this.rewards.addAll(message.getList("SyncRewards",ModLazyPackets.SLOT_MACHINE_RESULT));
        }
        if(message.contains("OpenStorage"))
            this.openStorage();
        if(message.contains("CollectMoney"))
            this.collectMoney();
        if(message.contains("OpenTerminal"))
            this.openTerminal();
    }

}
