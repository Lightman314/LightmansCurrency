package io.github.lightman314.lightmanscurrency.common.menus.gacha_machine;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.menus.MoneySlot;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.IMoneyCollectionMenu;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.menus.LazyMessageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.IValidatedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.GachaTrader;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GachaMachineMenu extends LazyMessageMenu implements IValidatedMenu, IMoneyCollectionMenu {

    private final long traderID;

    @Nullable
    public GachaTrader getTrader() { if(TraderAPI.getApi().GetTrader(this,this.traderID) instanceof GachaTrader trader) return trader; return null; }

    private final Container coins;

    List<Slot> coinSlots = new ArrayList<>();

    private final List<ItemStack> rewards = new ArrayList<>();
    public final boolean hasPendingReward() { return !this.rewards.isEmpty(); }
    public final ItemStack getNextReward() { if(this.rewards.isEmpty()) return null; return this.rewards.get(0); }

    public final ItemStack getAndRemoveNextReward()
    {
        if(this.rewards.isEmpty())
            return ItemStack.EMPTY;
        return this.rewards.remove(0);
    }

    private final MenuValidator validator;
    @Nonnull
    @Override
    public MenuValidator getValidator() { return this.validator; }

    public GachaMachineMenu(int windowID, Inventory inventory, long traderID, @Nonnull MenuValidator validator) {
        super(ModMenus.GACHA_MACHINE.get(), windowID, inventory);
        this.validator = validator;
        this.traderID = traderID;
        this.coins = new SimpleContainer(5);

        this.addValidator(this.validator);
        this.addValidator(() -> this.getTrader() != null);

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
            this.coinSlots.add(this.addSlot(new MoneySlot(this.coins, x, 8 + (x + 4) * 18, 108,this.player)));
        }

        GachaTrader trader = this.getTrader();
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
    public void removed(@Nonnull Player player) {
        super.removed(player);
        //Force-give rewards if closed before reward is handled
        for(ItemStack reward : this.rewards)
            ItemHandlerHelper.giveItemToPlayer(player,reward);
        this.rewards.clear();
        //Clear the coin slots
        this.clearContainer(player, this.coins);
        //Close the trader
        GachaTrader trader = this.getTrader();
        if(trader != null)
            trader.userClose(this.player);
    }

    public final TradeContext getContext() { return this.getContext(null); }

    public final TradeContext getContext(@Nullable Container rewardHolder)
    {
        TradeContext.Builder builder = TradeContext.create(this.getTrader(), this.player).withCoinSlots(this.coins);
        if(rewardHolder != null)
            builder.withItemHandler(new InvWrapper(rewardHolder));
        return builder.build();
    }

    @Override
    public void CollectStoredMoney() {
        if(this.getTrader() != null)
        {
            TraderData trader = this.getTrader();
            trader.CollectStoredMoney(this.player);
        }
    }

    private void ExecuteTrades(int count)
    {
        if(!this.rewards.isEmpty())
            return;
        GachaTrader trader = this.getTrader();
        if(trader != null)
        {
            boolean flag = true;
            for(int i = 0; flag && i < count; ++i)
            {
                Container result = new SimpleContainer(1);
                if(trader.TryExecuteTrade(this.getContext(result),0).isSuccess())
                {
                    if(result.isEmpty())
                        LightmansCurrency.LogError("Successful Gacha Machine Trade executed, but no item was received!");
                    else
                        this.rewards.add(result.getItem(0));
                }
                else
                    flag = false;
            }
            if(!this.rewards.isEmpty())
            {
                CompoundTag rewardData = new CompoundTag();
                InventoryUtil.saveAllItems("Rewards",rewardData,InventoryUtil.buildInventory(this.rewards));
                this.SendMessageToClient(this.builder().setInt("RewardCount",this.rewards.size()).setCompound("SyncRewards",rewardData));
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
    protected void HandleMessage(@Nonnull LazyPacketData message) {
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
            CompoundTag rewardData = message.getNBT("SyncRewards");
            this.rewards.addAll(InventoryUtil.buildList(InventoryUtil.loadAllItems("Rewards",rewardData,message.getInt("RewardCount"))));
        }
    }

}