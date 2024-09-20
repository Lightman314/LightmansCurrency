package io.github.lightman314.lightmanscurrency.common.menus;

import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.items.experimental.ATMCardItem;
import io.github.lightman314.lightmanscurrency.common.menus.providers.EasyMenuProvider;
import io.github.lightman314.lightmanscurrency.common.menus.slots.DisplaySlot;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ATMCardMenu extends LazyMessageMenu {


    private final int inventorySlot;

    public ATMCardMenu(int id, Inventory inventory, int inventorySlot) {
        super(ModMenus.ATM_CARD.get(), id, inventory);
        this.addValidator(this::isBankCardValid);
        this.addValidator(player -> !this.getAccountLocked() || LCAdminMode.isAdminPlayer(player));
        this.inventorySlot = inventorySlot;
        //Add slot with the card item to force it to be synced with the client?
        this.addSlot(new DisplaySlot(inventory,inventorySlot,6,6));
    }

    private boolean isBankCardValid() { return this.getBankCard().getItem() instanceof ATMCardItem; }

    @Nonnull
    protected final ItemStack getBankCard() { return this.inventory.getItem(this.inventorySlot); }

    @Nullable
    public BankReference getSelectedAccount() {
        CompoundTag tag = this.getBankCard().getOrCreateTag();
        if(tag.contains("BankAccount"))
            return BankReference.load(tag.getCompound("BankAccount")).flagAsClient(this);
        return null;
    }
    public void setSelectedAccount(@Nullable BankReference reference)
    {
        ItemStack card = this.getBankCard();
        if(card.getItem() instanceof ATMCardItem)
        {
            CompoundTag tag = card.getOrCreateTag();
            if(reference != null)
            {
                IBankAccount account = reference.flagAsClient(this).get();
                if(account != null && reference.allowedAccess(this.player))
                {
                    tag.put("BankAccount",reference.save());
                    tag.putInt("AccountValidation",account.getCardValidation());
                }
            }
            else
            {
                tag.remove("BankAccount");
                tag.remove("AccountValidation");
            }
        }
        //Send message to server
        if(this.isClient())
        {
            if(reference == null)
                this.SendMessage(this.builder().setFlag("SelectEmptyAccount"));
            else
                this.SendMessage(this.builder().setCompound("SelectAccount",reference.save()));
        }
    }
    public int getAccountValidation() {
        CompoundTag tag = this.getBankCard().getOrCreateTag();
        return tag.contains("AccountValidation") ? tag.getInt("AccountValidation") : -1;
    }
    public boolean isAccountValid()
    {
        BankReference br = this.getSelectedAccount();
        int validation = this.getAccountValidation();
        if(br != null)
        {
            IBankAccount account = br.get();
            if(account != null)
                return account.isCardValid(validation);
        }
        return false;
    }
    public boolean getAccountLocked() {
        CompoundTag tag = this.getBankCard().getOrCreateTag();
        return tag.getBoolean("CardLocked");
    }
    public void setAccountLocked(boolean locked)
    {
        ItemStack card = this.getBankCard();
        if(card.getItem() instanceof ATMCardItem)
        {
            CompoundTag tag = card.getOrCreateTag();
            //Can only lock if a bank account is assigned
            if(!locked)
                tag.remove("CardLocked");
            else if(tag.contains("BankAccount"))
                tag.putBoolean("CardLocked",true);
        }
        if(this.isClient())
            this.SendMessage(this.builder().setBoolean("SetLocked",locked));
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int slot) { return ItemStack.EMPTY; }

    @Override
    public void HandleMessage(@Nonnull LazyPacketData message) {
        if(message.contains("SelectEmptyAccount"))
            this.setSelectedAccount(null);
        if(message.contains("SelectAccount"))
            this.setSelectedAccount(BankReference.load(message.getNBT("SelectAccount")));
        if(message.contains("SetLocked"))
            this.setAccountLocked(message.getBoolean("SetLocked"));
    }

    @Nonnull
    public static MenuProvider getProvider(int inventorySlot) { return new Provider(inventorySlot); }

    private record Provider(int inventorySlot) implements EasyMenuProvider
    {
        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int id, @Nonnull Inventory inventory, @Nonnull Player player) {
            return new ATMCardMenu(id,inventory,this.inventorySlot);
        }
    }

}