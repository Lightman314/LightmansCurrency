package io.github.lightman314.lightmanscurrency.common.menus;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.items.data.FilterData;
import io.github.lightman314.lightmanscurrency.common.menus.providers.EasyMenuProvider;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemFilterMenu extends LazyMessageMenu {

    private final int itemIndex;
    public FilterData getData() { return FilterData.parse(this.getTargetedStack()); }
    public ItemFilterMenu(int id, Inventory inventory, int itemIndex) { this(ModMenus.ITEM_FILTER.get(),id,inventory,itemIndex); }
    protected ItemFilterMenu(MenuType<?> type, int id, Inventory inventory, int itemIndex) {
        super(type, id, inventory);
        this.itemIndex = itemIndex;
        this.addValidator(() -> !this.getTargetedStack().isEmpty());

        //Create Inventory Slots
        for(int y = 0; y < 3; y++)
        {
            for(int x = 0; x < 9; x++)
            {
                int index = x + (y * 9) + 9;
                this.addSlot(new Slot(inventory,index,8 + x * 18, 134 + (y * 18)));
            }
        }
        for(int x = 0; x < 9; x++)
        {
            this.addSlot(new Slot(inventory,x,8 + x * 18, 192));
        }

    }

    public final ItemStack getTargetedStack() {
        if(this.itemIndex < 0 || this.itemIndex >= this.inventory.getContainerSize())
            return ItemStack.EMPTY;
        return this.inventory.getItem(this.itemIndex);
    }

    private Consumer<ItemStack> quickMoveConsumer = s -> {};
    public void setQuickMoveConsumer(Consumer<ItemStack> consumer) { this.quickMoveConsumer = consumer; }

    @Override
    protected void HandleMessage(LazyPacketData message) {
        if(message.contains("MoveToSlot"))
            this.quickMoveConsumer.accept(message.getItem("MoveToSlot"));
        if(message.contains("AddEntry"))
            this.editData(filter -> filter.addEntry(message.getResourceLocation("AddEntry")));
        if(message.contains("RemoveEntry"))
            this.editData(filter -> filter.removeEntry(message.getResourceLocation("RemoveEntry")));
        if(message.contains("AddTag"))
            this.editData(filter -> filter.addTag(message.getResourceLocation("AddTag")));
        if(message.contains("RemoveTag"))
            this.editData(filter -> filter.removeTag(message.getResourceLocation("RemoveTag")));
    }

    protected final void editData(UnaryOperator<FilterData> action)
    {
        ItemStack target = this.getTargetedStack();
        if(target.isEmpty())
            this.player.closeContainer();
        else
        {
            FilterData filter = FilterData.parse(target);
            FilterData newFilter = action.apply(filter);
            if(newFilter.equals(filter))
                return;
            newFilter.write(target);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if(this.isClient())
            return ItemStack.EMPTY;
        if(index >= 0 && index < this.slots.size())
        {
            ItemStack quickMove = this.slots.get(index).getItem();
            if(!quickMove.isEmpty())
                this.SendMessage(this.builder().setItem("MoveToSlot",quickMove));
        }
        return ItemStack.EMPTY;
    }

    public static void OpenMenu(Player player, int itemIndex)
    {
        if(player instanceof ServerPlayer sp)
            NetworkHooks.openScreen(sp,new Provider(itemIndex),buf -> buf.writeInt(itemIndex));
    }

    private record Provider(int itemIndex) implements EasyMenuProvider
    {
        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) { return new ItemFilterMenu(containerId,inventory,this.itemIndex); }
    }

}