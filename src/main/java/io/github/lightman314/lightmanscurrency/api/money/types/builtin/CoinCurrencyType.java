package io.github.lightman314.lightmanscurrency.api.money.types.builtin;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.coin.CoinEntry;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValueParser;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValueParser;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.WalletCapability;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class CoinCurrencyType extends CurrencyType {

    public static final ResourceLocation TYPE = new ResourceLocation(MoneyAPI.MODID, "coins");
    public static final CoinCurrencyType INSTANCE = new CoinCurrencyType();

    protected CoinCurrencyType() { super(TYPE); }

    @Nonnull
    public static String getUniqueName(@Nonnull String chain) { return TYPE.toString() + "_" + chain; }
    @Nonnull
    @Override
    protected MoneyValue sumValuesInternal(@Nonnull List<MoneyValue> values) {
        long totalValue = 0;
        ChainData chain = null;
        for(MoneyValue val : values)
        {
            if(val instanceof CoinValue cv)
            {
                //Coin value's will be included for stored money
                if(chain == null)
                    chain = CoinAPI.getChainData(cv.getChain());
                if(chain != null && chain.chain.equals(cv.getChain()))
                    totalValue += cv.getCoreValue();
            }
        }
        if(chain != null)
            return CoinValue.fromNumber(chain.chain, totalValue);
        return MoneyValue.empty();
    }

    @Override
    public void getAvailableMoney(@Nonnull Player player, @Nonnull MoneyView.Builder builder) {
        ItemStack wallet = CoinAPI.getWalletStack(player);
        if(wallet.getItem() instanceof WalletItem)
            this.getValueInContainer(WalletItem.getWalletInventory(wallet), builder);
    }

    @Override
    public boolean hasPlayersMoneyChanged(@Nonnull Player player) {
        IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(player);
        if(walletHandler != null)
            return walletHandler.hasStoredMoneyChanged(player);
        return false;
    }

    @Override
    public void giveMoneyToPlayer(@Nonnull Player player, @Nonnull MoneyValue value) {
        if(value instanceof CoinValue coinValue)
        {
            List<ItemStack> coins = coinValue.getAsItemList();
            List<ItemStack> extraCoins = new ArrayList<>();
            ItemStack wallet = CoinAPI.getWalletStack(player);
            if(wallet.getItem() instanceof WalletItem)
            {
                for(ItemStack coin : coins)
                {
                    ItemStack extra = WalletItem.PickupCoin(wallet, coin);
                    if(!extra.isEmpty())
                        extraCoins.add(extra);
                }
                WalletMenuBase.OnWalletUpdated(player);
            }
            else
                extraCoins = coins;
            for(ItemStack extra : extraCoins)
                ItemHandlerHelper.giveItemToPlayer(player, extra);
        }
    }

    @Override
    public boolean takeMoneyFromPlayer(@Nonnull Player player, @Nonnull MoneyValue value) {
        if(value instanceof CoinValue coinValue)
        {
            IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(player);
            if(walletHandler != null)
            {
                ItemStack wallet = walletHandler.getWallet();
                if(WalletItem.isWallet(wallet.getItem()))
                {
                    Container walletInventory = WalletItem.getWalletInventory(wallet);
                    long change = takeObjectsOfValue(coinValue, walletInventory, true);
                    WalletItem.putWalletInventory(wallet, walletInventory);
                    if(change < 0)
                    {
                        this.giveMoneyToPlayer(player, CoinValue.fromNumber(coinValue.getChain(), -change));
                        return true;
                    }
                    else if(change == 0)
                    {
                        WalletMenuBase.OnWalletUpdated(player);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public MoneyValue loadMoneyValue(@Nonnull CompoundTag valueTag) { return CoinValue.loadCoinValue(valueTag); }

    @Override
    public MoneyValue loadMoneyValueJson(@Nonnull JsonObject json) { return CoinValue.loadCoinValue(json); }

    @Override
    public void getValueInContainer(@Nonnull Container container, @Nonnull MoneyView.Builder builder) {
        for(ChainData chain : CoinAPI.getAllChainData())
        {
            long totalValue = 0;
            for(int i = 0; i < container.getContainerSize(); ++i)
            {
                ItemStack stack = container.getItem(i);
                totalValue += chain.getCoreValue(stack) * stack.getCount();
            }
            if(totalValue > 0)
                builder.add(CoinValue.fromNumber(chain.chain, totalValue));
        }
    }

    @Override
    public boolean canAddValueToContainer(@Nonnull Container container, @Nonnull MoneyValue value) { return value instanceof CoinValue && container.canPlaceItem(0, new ItemStack(ModItems.COIN_COPPER.get())); }

    @Override
    public boolean addValueToContainer(@Nonnull Container container, @Nonnull MoneyValue value, @Nonnull Consumer<ItemStack> overflowHandler) {
        if(value instanceof CoinValue coinValue)
        {
            for(ItemStack stack : coinValue.getAsSeperatedItemList())
            {
                ItemStack extra = InventoryUtil.TryPutItemStack(container, stack);
                if(!extra.isEmpty())
                    overflowHandler.accept(extra);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean takeValueFromContainer(@Nonnull Container container, @Nonnull MoneyValue amount, @Nonnull Consumer<ItemStack> overflowHandler) {
        if(amount instanceof CoinValue coinValue)
        {
            long change = this.takeObjectsOfValue(coinValue, container, false);
            if(change > 0)
                return false;
            if(change < 0)
                this.addValueToContainer(container, CoinValue.fromNumber(coinValue.getChain(), -change), overflowHandler);
            return true;
        }
        return false;
    }

    private long takeObjectsOfValue(@Nonnull CoinValue valueToTake, @Nonnull Container container, boolean forceTake)
    {
        MoneyView funds = MoneyView.builder().build();
        if(!forceTake) //Don't bother collecting the current value if we're going to forcibly take the coins anyway
            funds = this.valueInContainer(container);
        //Check to ensure that the inventory has enough 'value' to remove
        if(forceTake || funds.containsValue(valueToTake))
        {
            long value = valueToTake.getCoreValue();
            ChainData chainData = CoinAPI.getChainData(valueToTake.getChain());
            if(chainData == null)
                return value;
            List<CoinEntry> coinList = chainData.getAllEntries(true);
            coinList.sort(ChainData.SORT_HIGHEST_VALUE_FIRST);
            //Remove objects from the inventory.
            for(CoinEntry coinEntry : coinList)
            {
                long coinValue = coinEntry.getCoreValue();
                if(coinValue <= value)
                {
                    //Search the inventory for this coin
                    for(int i = 0; i < container.getContainerSize() && coinValue <= value; i++)
                    {
                        ItemStack itemStack = container.getItem(i);
                        if(coinEntry.matches(itemStack))
                        {
                            //Remove the coins until they would be too much money or until the stack is empty.
                            while(coinValue <= value && !itemStack.isEmpty())
                            {
                                value -= coinValue;
                                itemStack.shrink(1);
                                if(itemStack.isEmpty())
                                    container.setItem(i, ItemStack.EMPTY);
                            }
                        }
                    }
                }
            }
            //Took all we could without over-taking, so we'll just go through the coinList backwards until we have what we need.
            if(value > 0)
            {
                coinList.sort(ChainData.SORT_LOWEST_VALUE_FIRST);
                for(CoinEntry coinEntry : coinList)
                {
                    long coinValue = coinEntry.getCoreValue();
                    //Search the inventory for this coin
                    for(int i = 0; i < container.getContainerSize() && value > 0; i++)
                    {
                        ItemStack itemStack = container.getItem(i);
                        if(coinEntry.matches(itemStack))
                        {
                            //Remove the coins until they would be too much money or until the stack is empty.
                            while(value > 0 && !itemStack.isEmpty())
                            {
                                value -= coinValue;
                                itemStack.shrink(1);
                                if(itemStack.isEmpty())
                                    container.setItem(i, ItemStack.EMPTY);
                            }
                        }
                    }
                }
            }
            //Inform the user if we were exact, or if too many items were taken and a refund is required via the getObjectsOfValue function
            return value;
        }
        return valueToTake.getCoreValue();
    }

    @Nonnull
    @Override
    public MoneyValueParser getValueParser() { return CoinValueParser.INSTANCE; }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Object> getInputHandlers(@Nullable Player player) {
        List<Object> results = new ArrayList<>();
        for(ChainData chain : CoinAPI.getAllChainData())
        {
            //Only add input handler if the chain is visible to the player
            if(player == null || chain.isVisibleTo(player))
            {
                Object i = chain.getInputHandler();
                if(i != null)
                    results.add(i);
            }
        }
        return results;
    }

}
