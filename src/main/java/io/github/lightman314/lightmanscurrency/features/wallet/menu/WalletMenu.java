package io.github.lightman314.lightmanscurrency.features.wallet.menu;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import io.github.lightman314.lightmanscurrency.core.LCMenuTypes;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletItem;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletStorage;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class WalletMenu extends AbstractWalletMenu {

    public WalletMenu(int menuID,Player player, int walletSlot) {
        super(LCMenuTypes.WALLET.get(),menuID,player,walletSlot);

        //Player inventory slots
        this.addStandardInventorySlots(player.getInventory(),this.halfBonusWidth + 8,32 + (this.coinSlotHeight * 18));

        //Coin Slots
        this.addCoinSlots(18);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if(index == this.walletSlot)
            return ItemStack.EMPTY;

        ItemStack clickedStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if(slot != null && slot.hasItem())
        {
            ItemStack slotStack = slot.getItem();
            clickedStack = slotStack.copy();
            if(index < 36)
            {
                if(!this.moveItemStackTo(slotStack,36,this.slots.size(),false))
                    return ItemStack.EMPTY;
            }
            else if(!this.moveItemStackTo(slotStack,0,36,true))
                return ItemStack.EMPTY;
            if(slotStack.isEmpty())
                slot.set(ItemStack.EMPTY);
            else
                slot.setChanged();
        }
        return clickedStack;
    }

    public void exchangeCoins() {
        if(this.isClient())
        {
            this.sendToServer(FancyPacketMap.flag("exchangeCoins"));
            return;
        }
        if(this.hasWallet() && this.hasExchangeAbility())
        {
            WalletStorage storage = this.getWalletStorage();
            try(Transaction transaction = Transaction.openRoot())
            {
                this.exchangeCoins(transaction);
                transaction.commit();
            }
        }
    }

    public void setAutoExchange(boolean newState) {
        if(this.isClient())
        {
            this.sendToServer(FancyPacketMap.map()
                    .setBoolean("setAutoExchange",newState));
            return;
        }
        ItemStack wallet = this.getWallet();
        if(newState)
            wallet.remove(LCDataComponents.WALLET_DISABLE_AUTOEXCHANGE);
        else
            wallet.set(LCDataComponents.WALLET_DISABLE_AUTOEXCHANGE,Unit.INSTANCE);
    }

    public void setForceDefaultSound(boolean newState) {
        if(this.isClient()) {
            this.sendToServer(FancyPacketMap.map()
                    .setBoolean("forceDefaultSound",newState));
            return;
        }
        ItemStack wallet = this.getWallet();
        if(newState)
            wallet.remove(LCDataComponents.WALLET_FORCE_DEFAULT_SOUND);
        else
            wallet.set(LCDataComponents.WALLET_FORCE_DEFAULT_SOUND,Unit.INSTANCE);
    }

    public boolean hasExchangeAbility() { return WalletItem.hasExchangeAbility(this.getWallet()); }
    public boolean hasAutoExchangeAbility() { return WalletItem.hasAutoExchangeAbility(this.getWallet()); }
    public boolean hasBankAbility() { return WalletItem.hasBankAbility(this.getWallet()); }

    public boolean getAutoExchange() { return WalletItem.shouldAutoExchange(this.getWallet()); }

    public boolean canToggleSoundSettings() { return WalletItem.hasNonDefaultSound(this.getWallet()); }

    public boolean getForceDefaultSound() { return this.getWallet().has(LCDataComponents.WALLET_FORCE_DEFAULT_SOUND); }

    public void quickInsert() {
        if(this.isClient())
        {
            this.sendToServer(FancyPacketMap.flag("quickCollect"));
            return;
        }
        if(this.hasWallet())
        {
            Inventory inv = this.getPlayer().getInventory();
            WalletStorage storage = this.getWalletStorage();
            try(Transaction transaction = Transaction.openRoot())
            {
                for(int i = 0; i < 36; ++i)
                {
                    ItemStack item = inv.getItem(i);
                    if(!item.isEmpty() && LCApi.getCoinAPI().isAllowedInCoinContainer(item,false))
                    {
                        try(Transaction tx = Transaction.open(transaction))
                        {
                            int inserted = storage.insert(ItemResource.of(item),item.getCount(),tx);
                            if(inserted > 0 && inserted <= item.getCount())
                            {
                                item.shrink(inserted);
                                if(item.isEmpty())
                                    inv.setItem(i,ItemStack.EMPTY);
                                tx.commit();
                            }
                        }
                    }
                }
                //Trigger auto-exchange as well on a quick-insert
                if(this.getAutoExchange())
                    this.exchangeCoins(transaction);
                transaction.commit();
            }
        }
    }

    public void openBankMenu() {
        if(this.isClient())
        {
            this.sendToServer(FancyPacketMap.map()
                    .setFlag("openBank"));
            return;
        }
        Player player = this.getPlayer();
        //TODO open bank menu
    }

    @Override
    protected void handleMessage(FancyPacketMap message) {
        if(message.contains("exchangeCoins"))
            this.exchangeCoins();
        if(message.contains("setAutoExchange"))
            this.setAutoExchange(message.getBoolean("setAutoExchange"));
        if(message.contains("forceDefaultSound"))
            this.setForceDefaultSound(message.getBoolean("forceDefaultSound"));
        if(message.contains("quickCollect"))
            this.quickInsert();
        if(message.contains("openBank"))
            this.openBankMenu();
    }

    public static void openMenu(Player player,int walletSlot) {
        player.openMenu(buildProvider(WalletMenu::new,walletSlot));
    }

}
