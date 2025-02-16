package io.github.lightman314.lightmanscurrency.common.menus.slot_machine;

import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.IItemBasedValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyStorage;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ResultHolder {

    private final Container items;
    private final IItemHandler itemHandler;
    private final MoneyStorage money;

    public ResultHolder() {
        this.items = new SimpleContainer(4);
        this.itemHandler = new InvWrapper(this.items);
        this.money = new MoneyStorage(() -> {},Integer.MIN_VALUE);
    }
    private ResultHolder(Container items, MoneyStorage money)
    {
        this.items = items;
        this.itemHandler = new InvWrapper(this.items);
        this.money = money;
    }

    public IItemHandler itemHandler() { return this.itemHandler; }
    public IMoneyHolder moneyHolder() { return this.money; }

    public boolean isEmpty() { return this.items.isEmpty() && this.money.isEmpty(); }

    public void giveToPlayer(Player player)
    {
        for(int i = 0; i < this.items.getContainerSize(); ++i)
        {
            ItemStack item = this.items.getItem(i);
            if(!item.isEmpty())
                ItemHandlerHelper.giveItemToPlayer(player,item);
        }
        IMoneyHolder handler = MoneyAPI.API.GetPlayersMoneyHandler(player);
        for(MoneyValue val : money.allValues())
            handler.insertMoney(val,false);
    }

    public List<ItemStack> getDisplayItems()
    {
        if(!this.money.isEmpty())
        {
            List<ItemStack> items = new ArrayList<>();
            for(MoneyValue value : this.money.allValues())
            {
                if(value instanceof IItemBasedValue itemValue)
                    items.addAll(itemValue.getAsSeperatedItemList());
            }
            return items;
        }
        else
        {
            List<ItemStack> items = new ArrayList<>();
            for(int i = 0; i < this.items.getContainerSize(); ++i)
            {
                ItemStack item = this.items.getItem(i);
                if(!item.isEmpty())
                    items.add(item);
            }
            return items;
        }
    }

    public CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        InventoryUtil.saveAllItems("items",tag,this.items);
        tag.put("money",this.money.save());
        return tag;
    }

    public static ResultHolder load(CompoundTag tag)
    {
        Container items = InventoryUtil.loadAllItems("items",tag,4);
        MoneyStorage money = new MoneyStorage(() -> {},Integer.MIN_VALUE);
        money.load(tag.getList("money", Tag.TAG_COMPOUND));
        return new ResultHolder(items,money);
    }


}