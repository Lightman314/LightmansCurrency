package io.github.lightman314.lightmanscurrency.common.menus.slot_machine;

import io.github.lightman314.lightmanscurrency.api.misc.menus.MoneySlot;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.menu.IMoneyCollectionMenu;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.menus.LazyMessageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.IValidatedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SlotMachineMenu extends LazyMessageMenu implements IValidatedMenu, IMoneyCollectionMenu {

    private final long traderID;

    @Nullable
    public final SlotMachineTraderData getTrader() { if(TraderAPI.getApi().GetTrader(this.isClient(), this.traderID) instanceof SlotMachineTraderData trader) return trader; return null; }

    private final Container coins;

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

    private final MenuValidator validator;

    @Override
    public MenuValidator getValidator() { return this.validator; }

    public SlotMachineMenu(int windowID, Inventory inventory, long traderID, MenuValidator validator) {
        super(ModMenus.SLOT_MACHINE.get(), windowID, inventory);
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

        SlotMachineTraderData trader = this.getTrader();
        if(trader != null)
            trader.userOpen(this.player);

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
    public void removed(Player player) {
        super.removed(player);
        //Force-give rewards if closed before reward is handled
        for(ResultHolder reward : this.rewards)
            reward.giveToPlayer(this.player);
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

    public final TradeContext getContext(@Nullable ResultHolder rewardHolder)
    {
        TradeContext.Builder builder = TradeContext.create(this.getTrader(), this.player).withCoinSlots(this.coins);
        if(rewardHolder != null)
            builder.withItemHandler(rewardHolder.itemHandler()).withMoneyHolder(rewardHolder.moneyHolder()).withCustomData(ResultHolder.CONTEXT_KEY,rewardHolder);
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
        SlotMachineTraderData trader = this.getTrader();
        if(trader != null)
        {
            boolean flag = true;
            for(int i = 0; flag && i < count; ++i)
            {
                ResultHolder result = new ResultHolder();
                if(trader.TryExecuteTrade(this.getContext(result), 0).isSuccess())
                    this.rewards.add(result); //Always add the reward now, as "failing" is now a valid result
                else
                    flag = false;
            }
            if(!this.rewards.isEmpty())
            {
                CompoundTag rewardData = new CompoundTag();
                ListTag resultList = new ListTag();
                for(ResultHolder result : this.rewards)
                    resultList.add(result.save(this.registryAccess()));
                rewardData.put("Rewards", resultList);
                this.SendMessageToClient(this.builder().setCompound("SyncRewards", rewardData));
            }
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
    public void HandleMessage(LazyPacketData message) {
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
            CompoundTag rewardData = message.getNBT("SyncRewards");
            ListTag rewardList = rewardData.getList("Rewards", Tag.TAG_COMPOUND);
            for(int i = 0; i < rewardList.size(); ++i)
                this.rewards.add(ResultHolder.load(rewardList.getCompound(i),message.lookup));
        }
    }

}
