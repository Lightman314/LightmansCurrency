package io.github.lightman314.lightmanscurrency.integration.patchouli;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import vazkii.patchouli.api.IStyleStack;
import vazkii.patchouli.api.PatchouliAPI;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class LCPatchouli {

    public static void init()
    {
        PatchouliAPI.IPatchouliAPI api = PatchouliAPI.get();
        api.registerFunction("lightmanscurrency_wallet_slots",buildWalletSizeFunction());
        api.registerFunction("lightmanscurrency_wallet_exchange",buildWalletFunction(WalletItem::CanExchange));
        api.registerFunction("lightmanscurrency_wallet_pickup",buildWalletFunction(WalletItem::CanPickup));
        api.registerFunction("lightmanscurrency_wallet_bank",buildWalletFunction(WalletItem::HasBankAccess));
        api.registerFunction("lightmanscurrency_wallet_magnet",buildWalletMagnetFunction());
        LightmansCurrency.LogDebug("Registered Custom Patchouli Functions!");
    }

    private static BiFunction<String,IStyleStack,String> buildWalletSizeFunction()
    {
        return (arg,style) -> {
            WalletItem wallet = getWallet(arg);
            if(wallet != null)
                return String.valueOf(WalletItem.InventorySize(wallet.getDefaultInstance()));
            return "0";
        };
    }

    private static BiFunction<String,IStyleStack,String> buildWalletFunction(Predicate<WalletItem> test)
    {
        return (arg,style) -> {
            String[] args = arg.split(";",2);
            WalletItem wallet = getWallet(args[0]);
            if(wallet != null && test.test(wallet))
                return args[1];
            return "";
        };
    }

    private static BiFunction<String,IStyleStack,String> buildWalletMagnetFunction()
    {
        return (arg,style) -> {
            WalletItem wallet = getWallet(arg);
            if (wallet != null)
                return String.valueOf(wallet.bonusMagnet);
            return "0";
        };
    }

    @Nullable
    private static WalletItem getWallet(String walletID)
    {
        ResourceLocation id;
        if(!walletID.contains("-")) //Use LC namespace by default as it's assumed wallets will have that namespace
            id = VersionUtil.lcResource(walletID);
        else
            id = VersionUtil.parseResource(walletID.replace("-",":"));
        if(BuiltInRegistries.ITEM.get(id) instanceof WalletItem wallet)
            return wallet;
        return null;
    }

}