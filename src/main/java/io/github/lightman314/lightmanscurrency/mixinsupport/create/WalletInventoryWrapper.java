package io.github.lightman314.lightmanscurrency.mixinsupport.create;

import com.mojang.datafixers.util.Pair;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyStorage;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.WalletCapability;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WalletInventoryWrapper extends Inventory {

    public static MoneyView expectedCost = MoneyView.empty();

    private final Inventory inventory;

    private final List<Pair<Integer,ItemStack>> paymentItemsInInventory = new ArrayList<>();
    private final List<ItemStack> paymentItems = new ArrayList<>();

    public WalletInventoryWrapper(Inventory inventory, IWalletHandler walletHandler, InventorySummary paymentItems) {
        super(inventory.player);
        this.inventory = inventory;
        this.setupPaymentItems(walletHandler,paymentItems);
    }

    private void setupPaymentItems(IWalletHandler walletHandler, InventorySummary paymentItems)
    {
        ItemStack wallet = walletHandler.getWallet();
        Container walletContents = WalletItem.getWalletInventory(wallet);
        MoneyStorage cost = new MoneyStorage(() -> {});
        IMoneyHandler walletMoney = MoneyAPI.API.GetContainersMoneyHandler(walletContents,this.player);
        List<ItemStack> coinsToAdd = new ArrayList<>();
        for(BigItemStack stack : paymentItems.getStacks())
        {
            if(CoinAPI.API.IsCoin(stack.stack,false))
            {
                int countToExtract = stack.count;
                countToExtract -= this.queryPaymentItems(stack);
                if(countToExtract > 0)
                {
                    ChainData chain = CoinAPI.API.ChainDataOfCoin(stack.stack);
                    cost.addValue(CoinValue.fromNumber(chain,chain.getCoreValue(stack.stack) * stack.count));
                    coinsToAdd.add(stack.stack.copyWithCount(countToExtract));
                }
            }
            else
                this.queryPaymentItems(stack);
        }
        if(!cost.isEmpty())
        {
            MoneyView availableFunds = walletMoney.getStoredMoney();
            for(MoneyValue val : cost.allValues())
            {
                if(!availableFunds.containsValue(val))
                    return;
            }
            List<MoneyValue> taken = new ArrayList<>();
            for(MoneyValue val : cost.allValues())
            {
                if(walletMoney.extractMoney(val,true).isEmpty())
                {
                    walletMoney.extractMoney(val,false);
                    taken.add(val);
                }
                else
                {
                    for(MoneyValue v : taken)
                        walletMoney.insertMoney(v,false);
                    return;
                }
            }
            WalletItem.putWalletInventory(wallet,walletContents);
            walletHandler.setWallet(wallet);
            this.paymentItems.addAll(coinsToAdd);
        }
    }

    private int queryPaymentItems(BigItemStack stack)
    {
        int foundCount = 0;
        for(int i = 0; i < this.inventory.items.size(); ++i)
        {
            ItemStack item = this.inventory.getItem(i);
            if(item.is(stack.stack.getItem()))
            {
                this.paymentItemsInInventory.add(Pair.of(i,item));
                foundCount += item.getCount();
            }
        }
        return foundCount;
    }

    public void clearContents()
    {
        //Give any leftover payment items back to the players wallet
        IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(this.player);
        if(walletHandler != null)
        {
            ItemStack wallet = walletHandler.getWallet();
            Container walletContents = WalletItem.getWalletInventory(wallet);
            for(ItemStack item : this.paymentItems)
            {
                item = InventoryUtil.TryPutItemStack(walletContents,item);
                if(!item.isEmpty())
                    ItemHandlerHelper.giveItemToPlayer(this.player,item);
            }
            WalletItem.putWalletInventory(wallet,walletContents);
            walletHandler.setWallet(wallet);
            return;
        }
        for(ItemStack item : this.paymentItems)
        {
            if(!item.isEmpty())
                ItemHandlerHelper.giveItemToPlayer(this.player,item);
        }
    }

    @Override
    public ItemStack getItem(int slot) {
        if(slot < this.paymentItemsInInventory.size())
            return this.paymentItemsInInventory.get(slot).getSecond();
        slot -= this.paymentItemsInInventory.size();
        if(slot < this.paymentItems.size())
            return this.paymentItems.get(slot);
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if(slot < this.paymentItemsInInventory.size())
        {
            Pair<Integer,ItemStack> pair = this.paymentItemsInInventory.get(slot);
            this.inventory.setItem(pair.getFirst(),stack);
            this.paymentItemsInInventory.set(slot,Pair.of(pair.getFirst(),stack));
            return;
        }
        slot -= this.paymentItemsInInventory.size();
        if(slot < this.paymentItems.size())
            this.paymentItems.set(slot,stack);
        else if(!stack.isEmpty())
            ItemHandlerHelper.giveItemToPlayer(this.player,stack);
    }

}