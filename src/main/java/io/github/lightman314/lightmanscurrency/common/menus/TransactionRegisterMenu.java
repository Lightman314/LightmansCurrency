package io.github.lightman314.lightmanscurrency.common.menus;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.items.TransactionRegisterItem;
import io.github.lightman314.lightmanscurrency.common.items.data.register.TransactionData;
import io.github.lightman314.lightmanscurrency.common.items.data.register.TransactionList;
import io.github.lightman314.lightmanscurrency.common.items.data.register.TransactionType;
import io.github.lightman314.lightmanscurrency.common.menus.providers.EasyMenuProvider;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.UnaryOperator;

public class TransactionRegisterMenu extends LazyMessageMenu {

    private final int itemIndex;
    public TransactionRegisterMenu(int id, Inventory inventory, int itemIndex) { this(ModMenus.TRANSACTION_REGISTER.get(),id,inventory,itemIndex); }
    protected TransactionRegisterMenu(MenuType<?> type, int id, Inventory inventory, int itemIndex) {
        super(type, id, inventory);
        this.itemIndex = itemIndex;
        this.addValidator(() -> this.player.getInventory().getItem(this.itemIndex).getItem() instanceof TransactionRegisterItem);
    }

    public Component getTitle() { return this.player.getInventory().getItem(this.itemIndex).getHoverName(); }

    public TransactionList getData() {
        ItemStack item = this.player.getInventory().getItem(this.itemIndex);
        CompoundTag tag = item.getTag();
        if(tag != null && tag.contains("TransactionData"))
            return TransactionList.load(tag.getCompound("TransactionData"));
        return TransactionList.EMPTY;
    }

    public int getColor() {
        ItemStack item = this.player.getInventory().getItem(this.itemIndex);
        if(item.getItem() instanceof DyeableLeatherItem dli)
            return dli.getColor(item);
        return 0xFFFFFF;
    }

    private void editData(UnaryOperator<TransactionList> editor)
    {
        ItemStack stack = this.player.getInventory().getItem(this.itemIndex);
        TransactionList data = this.getData();
        TransactionList newData = editor.apply(data);
        CompoundTag tag = stack.getOrCreateTag();
        tag.put("TransactionData",newData.save());
    }

    private void editTransaction(int index, UnaryOperator<TransactionData> editor)
    {
        this.editData(list -> {
            if(index >= 0 && index <= list.transactions.size())
            {
                TransactionData data = list.transactions.get(index);
                return list.withEditedTransaction(index,editor.apply(data));
            }
            return list;
        });
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }

    public void RedoCalculations()
    {
        this.editData(TransactionList::performAllCalculations);
        if(this.isClient())
            this.SendMessage(this.builder().setFlag("RedoCalculations"));
    }

    public void CreateTransaction()
    {
        this.editData(TransactionList::withAddedTransaction);
        if(this.isClient())
            this.SendMessage(this.builder().setFlag("CreateTransaction"));
    }

    public void ChangeStartingValue(MoneyValue startingValue)
    {
        this.editData(list -> list.withStartingValue(startingValue));
        if(this.isClient())
            this.SendMessage(this.builder().setMoneyValue("ChangeStartingValue",startingValue));
    }

    public void ChangeTransactionComment(int index, String newComment)
    {
        this.editTransaction(index,data -> data.withComment(newComment));
        if(this.isClient())
        {
            this.SendMessage(this.builder()
                    .setInt("EditTransaction",index)
                    .setString("ChangeComment",newComment));
        }
    }

    public void ChangeTransactionType(int index, TransactionType newType)
    {
        this.editTransaction(index,data -> data.withType(newType));
        if(this.isClient())
        {
            this.SendMessage(this.builder()
                    .setInt("EditTransaction",index)
                    .setInt("ChangeType",newType.ordinal()));
        }
    }

    public void ChangeTransactionValue(int index, MoneyValue newValue)
    {
        this.editTransaction(index,data -> data.withArgument(newValue));
        if(this.isClient())
        {
            this.SendMessage(this.builder()
                    .setInt("EditTransaction",index)
                    .setMoneyValue("ChangeValue",newValue));
        }
    }

    public void ChangeTransactionNumber(int index, double newNumber)
    {
        this.editTransaction(index,data -> data.withArgument(newNumber));
        if(this.isClient())
        {
            this.SendMessage(this.builder()
                    .setInt("EditTransaction",index)
                    .setDouble("ChangeNumber",newNumber));
        }
    }

    public void DeleteTransaction(int index)
    {
        this.editData(data -> data.withDeletedTransaction(index));
        if(this.isClient())
            this.SendMessage(this.builder().setInt("DeleteTransaction",index));
    }

    @Override
    public void HandleMessage(LazyPacketData message) {
        if(message.contains("RedoCalculations"))
            this.RedoCalculations();
        if(message.contains("CreateTransaction"))
            this.CreateTransaction();
        if(message.contains("ChangeStartingValue"))
            this.ChangeStartingValue(message.getMoneyValue("ChangeStartingValue"));
        if(message.contains("EditTransaction"))
        {
            int index = message.getInt("EditTransaction");
            if(message.contains("ChangeComment"))
                this.ChangeTransactionComment(index,message.getString("ChangeComment"));
            if(message.contains("ChangeType"))
                this.ChangeTransactionType(index, EnumUtil.enumFromOrdinal(message.getInt("ChangeType"),TransactionType.values(),TransactionType.ADD));
            if(message.contains("ChangeValue"))
                this.ChangeTransactionValue(index,message.getMoneyValue("ChangeValue"));
            if(message.contains("ChangeNumber"))
                this.ChangeTransactionNumber(index,message.getDouble("ChangeNumber"));
        }
        if(message.contains("DeleteTransaction"))
            this.DeleteTransaction(message.getInt("DeleteTransaction"));
    }

    public static void openMenu(Player player, int itemSlot)
    {
        if(player instanceof ServerPlayer sp)
            NetworkHooks.openScreen(sp,(EasyMenuProvider) (containerId, playerInventory, player1) -> new TransactionRegisterMenu(containerId,playerInventory,itemSlot), b -> b.writeInt(itemSlot));
    }

}