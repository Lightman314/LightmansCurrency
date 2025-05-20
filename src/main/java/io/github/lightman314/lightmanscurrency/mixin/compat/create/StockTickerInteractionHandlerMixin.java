package io.github.lightman314.lightmanscurrency.mixin.compat.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.StockTickerInteractionHandler;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
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
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.mixinsupport.create.WalletInventoryWrapper;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = StockTickerInteractionHandler.class,remap = false)
public class StockTickerInteractionHandlerMixin {

    @Unique
    private static WalletInventoryWrapper lightmanscurrency$wrapper;

    @Inject(at=@At(value="FIELD", target="net/createmod/catnip/data/Iterate.trueAndFalse:[Z"),method="interactWithShop", cancellable=true, remap = false)
    private static void interactWithShop(Player player, Level level, BlockPos targetPos, ItemStack mainHandItem, CallbackInfo ci, @Local(name = "paymentEntries") InventorySummary paymentEntries)
    {
        lightmanscurrency$clearWrapper();
        //If no wallet equipped nothing to check
        IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(player);
        ItemStack wallet = walletHandler == null ? ItemStack.EMPTY : walletHandler.getWallet();
        if(!WalletItem.isWallet(wallet))
            return;
        Map<Item,Integer> map = new HashMap<>();
        for(BigItemStack stack : paymentEntries.getStacks())
        {
            if(CoinAPI.API.IsCoin(stack.stack,false))
                map.put(stack.stack.getItem(),stack.count);
            else
                return;
        }
        //Check for items already in the players inventory
        Inventory inventory = player.getInventory();
        for(int i = 0; i < inventory.items.size(); ++i)
        {
            ItemStack item = inventory.getItem(i);
            if(map.containsKey(item.getItem()))
                map.put(item.getItem(),map.get(item.getItem()) - item.getCount());
        }

        //Get the total cost requirement excluding the amount of money in the players inventory
        MoneyStorage cost = new MoneyStorage(() -> {});
        for(Item coin : map.keySet())
        {
            ChainData chain = CoinAPI.API.ChainDataOfCoin(coin);
            int count = map.get(coin);
            if(count > 0)
                cost.addValue(CoinValue.fromNumber(chain,count));
        }
        if(!cost.isEmpty())
        {
            //Get money stored in the wallet
            MoneyView available = MoneyAPI.API.GetContainersMoneyHandler(WalletItem.getWalletInventory(wallet),s -> {}, IClientTracker.forServer()).getStoredMoney();
            for(MoneyValue c : cost.allValues())
            {
                if(!available.containsValue(c))
                {
                    player.playNotifySound(ForgeRegistries.SOUND_EVENTS.getValue(VersionUtil.modResource("create","deny")), SoundSource.PLAYERS, 1, 0.5f);
                    player.displayClientMessage(EasyText.translatable("create.stock_keeper.too_broke").withStyle(ChatFormatting.RED),true);
                    ci.cancel();
                    return;
                }
            }
        }
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "net/minecraft/world/entity/player/Player.getInventory()Lnet/minecraft/world/entity/player/Inventory;"),method = "interactWithShop",remap = false)
    private static Inventory wrapInventory(Player player, Operation<Inventory> next, @Local(name = "paymentEntries") InventorySummary paymentEntries)
    {
        if(player.level().isClientSide)
            return next.call(player);
        lightmanscurrency$clearWrapper();
        //If no wallet equipped, don't wrap the inventory
        IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(player);
        ItemStack wallet = walletHandler == null ? ItemStack.EMPTY : walletHandler.getWallet();
        if(!WalletItem.isWallet(wallet))
            return next.call(player);
        for(BigItemStack stack : paymentEntries.getStacks()) {
            if (CoinAPI.API.IsCoin(stack.stack, false))
            {
                lightmanscurrency$wrapper = new WalletInventoryWrapper(next.call(player),walletHandler,paymentEntries.copy());
                return lightmanscurrency$wrapper;
            }
        }
        return next.call(player);
    }

    @Inject(at = @At("RETURN"),method = "interactWithShop",remap = false)
    private static void interactWithShop(Player player, Level level, BlockPos targetPos, ItemStack mainHandItem, CallbackInfo ci)
    {
        lightmanscurrency$clearWrapper();
    }

    @Unique
    private static void lightmanscurrency$clearWrapper()
    {
        if(lightmanscurrency$wrapper != null)
        {
            lightmanscurrency$wrapper.clearContents();
            lightmanscurrency$wrapper = null;
        }
    }

}