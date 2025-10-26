package io.github.lightman314.lightmanscurrency.common.menus.slot_machine;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyStorage;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.common.util.TagUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ResultHolder {

    public static final ResourceLocation CONTEXT_KEY = VersionUtil.lcResource("slot_machine_result_holder");

    private final Container items;
    private final IItemHandler itemHandler;
    private final MoneyStorage money;
    private final NonNullList<IconData> icons;

    public ResultHolder() {
        this.items = new SimpleContainer(SlotMachineEntry.ITEM_LIMIT);
        this.itemHandler = new InvWrapper(this.items);
        this.money = new MoneyStorage(() -> {},Integer.MIN_VALUE);
        this.icons = NonNullList.withSize(SlotMachineEntry.ITEM_LIMIT, IconUtil.ICON_X);
    }
    private ResultHolder(Container items, MoneyStorage money, NonNullList<IconData> icons)
    {
        this.items = items;
        this.itemHandler = new InvWrapper(this.items);
        this.money = money;
        this.icons = icons;
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
        IMoneyHolder handler = MoneyAPI.getApi().GetPlayersMoneyHandler(player);
        for(MoneyValue val : money.allValues())
            handler.insertMoney(val,false);
    }

    public List<IconData> getIcons() { return ImmutableList.copyOf(this.icons); }
    public void setIcons(List<IconData> icons)
    {
        this.icons.clear();
        for(int i = 0; i < this.icons.size() && i < icons.size(); ++i)
            this.icons.set(i,icons.get(i));
    }

    public CompoundTag save(HolderLookup.Provider lookup)
    {
        CompoundTag tag = new CompoundTag();
        InventoryUtil.saveAllItems("items",tag,this.items,lookup);
        tag.put("money",this.money.save());
        tag.put("icons",TagUtil.writeIconList(this.icons,lookup));
        return tag;
    }

    public static ResultHolder load(CompoundTag tag, HolderLookup.Provider lookup)
    {
        Container items = InventoryUtil.loadAllItems("items",tag,SlotMachineEntry.ITEM_LIMIT,lookup);
        MoneyStorage money = new MoneyStorage(() -> {},Integer.MIN_VALUE);
        money.load(tag.getList("money", Tag.TAG_COMPOUND));
        NonNullList<IconData> icons = NonNullList.withSize(SlotMachineEntry.ITEM_LIMIT,SlotMachineEntry.DEFAULT_ICON);
        TagUtil.readIconList(icons,tag.getList("icons",Tag.TAG_COMPOUND),lookup,SlotMachineEntry.DEFAULT_ICON);
        return new ResultHolder(items,money,icons);
    }


}
