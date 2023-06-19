package io.github.lightman314.lightmanscurrency.common.menus;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SlotMachineMenu extends LazyMessageMenu implements IClientTracker {

    private final long traderID;

    public final Player player;

    @Override
    public boolean isClient() { return this.player.level.isClientSide; }

    @Nullable
    public final SlotMachineTraderData getTrader() { if(TraderSaveData.GetTrader(this.isClient(), this.traderID) instanceof SlotMachineTraderData trader) return trader; return null; }

    Container coins = new SimpleContainer(5);

    List<Slot> coinSlots = new ArrayList<>();

    private final List<RewardCache> rewards = new ArrayList<>();
    public final boolean hasPendingReward() { return this.rewards.size() > 0; }
    public final RewardCache getNextReward() { if(this.rewards.size() == 0) return null; return this.rewards.get(0); }

    public final RewardCache getAndRemoveNextReward()
    {
        if(this.rewards.size() == 0)
            return null;
        return this.rewards.remove(0);
    }

    public SlotMachineMenu(int windowID, Inventory inventory, long traderID) {
        super(ModMenus.SLOT_MACHINE.get(), windowID, inventory);
        this.player = inventory.player;
        this.traderID = traderID;

        //Player inventory
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
        for(int x = 0; x < coins.getContainerSize(); x++)
        {
            this.coinSlots.add(this.addSlot(new CoinSlot(this.coins, x, 8 + (x + 4) * 18, 108)));
        }

        SlotMachineTraderData trader = this.getTrader();
        if(trader != null)
            trader.userOpen(this.player);

    }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(@Nonnull Player playerEntity, int index)
    {

        ItemStack clickedStack = ItemStack.EMPTY;

        Slot slot = this.slots.get(index);

        if(slot != null && slot.hasItem())
        {
            ItemStack slotStack = slot.getItem();
            clickedStack = slotStack.copy();
            if(index < 36)
            {
                //Move from inventory to coin slots
                if(!this.moveItemStackTo(slotStack, 36, this.slots.size(), false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if(index < this.slots.size())
            {
                //Move from coin slots to inventory
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
    public boolean stillValid(@Nonnull Player player) { return this.getTrader() != null; }

    @Override
    public void removed(@Nonnull Player player) {
        super.removed(player);
        MinecraftForge.EVENT_BUS.unregister(this);
        //Force-give rewards if closed before reward is handled
        for(RewardCache reward : this.rewards)
            reward.giveToPlayer();
        this.rewards.clear();
        //Clear the coin slots
        this.clearContainer(player, this.coins);
        //Close the trader
        SlotMachineTraderData trader = this.getTrader();
        if(trader != null)
            trader.userClose(this.player);
    }

    public final void clearContainer(Container container) { this.clearContainer(this.player, container); }

    public final TradeContext getContext() { return this.getContext(null); }

    public final TradeContext getContext(@Nullable RewardCache rewardHolder)
    {
        TradeContext.Builder builder = TradeContext.create(this.getTrader(), this.player).withCoinSlots(this.coins);
        if(rewardHolder != null)
        {
            builder.withItemHandler(new InvWrapper(rewardHolder.itemHolder));
            builder.withStoredCoins(rewardHolder.moneyHolder);
        }

        return builder.build();
    }

    public void CollectCoinStorage() {
        if(this.getTrader() != null)
        {
            TraderData trader = this.getTrader();
            if(trader.hasPermission(this.player, Permissions.COLLECT_COINS))
            {
                CoinValue payment = trader.getInternalStoredMoney();
                if(this.getContext(null).givePayment(payment))
                    trader.clearStoredMoney();
            }
            else
                Permissions.PermissionWarning(this.player, "collect stored coins", Permissions.COLLECT_COINS);
        }
    }

    private void ExecuteTrades(int count)
    {
        if(this.rewards.size() > 0)
            return;
        SlotMachineTraderData trader = this.getTrader();
        if(trader != null)
        {
            boolean flag = true;
            for(int i = 0; flag && i < count; ++i)
            {
                RewardCache holder = new RewardCache();
                if(trader.ExecuteTrade(this.getContext(holder), 0).isSuccess())
                {
                    if(holder.itemHolder.isEmpty() && holder.moneyHolder.getRawValue() <= 0)
                        LightmansCurrency.LogError("Successful Slot Machine Trade executed, but no items or money were received!");
                    else
                        this.rewards.add(holder);
                }
                else
                    flag = false;
            }
            if(this.rewards.size() > 0)
            {
                CompoundTag rewardData = new CompoundTag();
                ListTag resultList = new ListTag();
                for(RewardCache result : this.rewards)
                    resultList.add(result.save());
                rewardData.put("Rewards", resultList);
                this.SendMessageToClient(LazyPacketData.builder().setCompound("SyncRewards", rewardData));
            }
        }

    }

    public boolean GiveNextReward()
    {
        RewardCache nextReward = this.getAndRemoveNextReward();
        if(nextReward != null)
        {
            nextReward.giveToPlayer();
            return true;
        }
        return false;
    }

    @Override
    public void HandleMessage(LazyPacketData message) {
        if(message.contains("ExecuteTrade"))
        {
            if(this.rewards.size() > 0)
                return;
            ExecuteTrades(message.getInt("ExecuteTrade"));
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
            CompoundTag rewardData = message.getNBT("SyncRewards");
            ListTag rewardList = rewardData.getList("Rewards", Tag.TAG_COMPOUND);
            for(int i = 0; i < rewardList.size(); ++i)
                this.rewards.add(this.loadReward(rewardList.getCompound(i)));
        }
    }

    public final RewardCache loadReward(CompoundTag tag) { return new RewardCache(InventoryUtil.loadAllItems("Items", tag, SlotMachineEntry.ITEM_LIMIT), CoinValue.from(tag, "Money")); }

    public final class RewardCache
    {
        public final Container itemHolder;
        public final CoinValue moneyHolder;
        public RewardCache() { this.itemHolder = new SimpleContainer(SlotMachineEntry.ITEM_LIMIT); this.moneyHolder = new CoinValue(); }
        public RewardCache(Container itemHolder, CoinValue moneyHolder) { this.itemHolder = itemHolder; this.moneyHolder = moneyHolder; }
        public void giveToPlayer()
        {
            SlotMachineMenu.this.clearContainer(this.itemHolder);
            this.itemHolder.clearContent();
            MoneyUtil.ProcessChange(SlotMachineMenu.this.coins, SlotMachineMenu.this.player, this.moneyHolder);
            this.moneyHolder.loadFromOldValue(0);
        }

        public List<ItemStack> getDisplayItems()
        {
            if(this.moneyHolder.hasAny())
                return MoneyUtil.getCoinsOfValue(this.moneyHolder);
            else
            {
                List<ItemStack> items = new ArrayList<>();
                for(int i = 0; i < this.itemHolder.getContainerSize(); ++i)
                {
                    ItemStack item = this.itemHolder.getItem(i);
                    if(!item.isEmpty())
                        items.add(item.copy());
                }
                return items;
            }
        }

        public CompoundTag save()
        {
            CompoundTag tag = new CompoundTag();
            InventoryUtil.saveAllItems("Items", tag, this.itemHolder);
            this.moneyHolder.save(tag, "Money");
            return tag;
        }

    }

}