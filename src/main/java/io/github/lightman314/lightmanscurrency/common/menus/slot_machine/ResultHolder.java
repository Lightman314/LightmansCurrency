package io.github.lightman314.lightmanscurrency.common.menus.slot_machine;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.LCItemStackHandler;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.builtin.MoneyStorage;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade.SlotMachineEntry;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.List;

public class ResultHolder {

    public static final ResourceLocation CONTEXT_KEY = LightmansCurrency.id("slot_machine_result_holder");

    public static final StreamCodec<RegistryFriendlyByteBuf,ResultHolder> STREAM_CODEC = StreamCodec.composite(
            LCItemStackHandler.STREAM_CODEC,h -> h.items,
            MoneyStorage.STREAM_CODEC,h -> h.money,
            IconData.LIST_STREAM_CODEC,h -> h.icons,
            ResultHolder::new);

    private final LCItemStackHandler items;
    private final MoneyStorage money;
    private final NonNullList<IconData> icons;

    public ResultHolder() {
        this.items = new LCItemStackHandler(SlotMachineEntry.ITEM_LIMIT);
        this.money = new MoneyStorage().withPriority(Integer.MAX_VALUE);
        this.icons = NonNullList.withSize(SlotMachineEntry.ITEM_LIMIT, SlotMachineEntry.DEFAULT_ICON);
    }
    private ResultHolder(LCItemStackHandler items, MoneyStorage money, NonNullList<IconData> icons)
    {
        this.items = items;
        this.money = money;
        this.icons = icons;
    }

    public IItemHandler itemHandler() { return this.items; }
    public IMoneyHolder moneyHolder() { return this.money; }

    public boolean isEmpty() { return this.items.isEmpty() && this.money.isEmpty(); }

    public void giveToPlayer(Player player)
    {
        for(int i = 0; i < this.items.getSlots(); ++i)
        {
            ItemStack item = this.items.getStackInSlot(i);
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

}
