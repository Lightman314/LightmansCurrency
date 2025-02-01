package io.github.lightman314.lightmanscurrency.integration.curios.client;

import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.integration.curios.client.renderer.WalletCurioRenderer;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

public class LCCuriosClient {

    public static void registerRenderLayers()
    {
        registerWallet(ModItems.WALLET_COPPER);
        registerWallet(ModItems.WALLET_IRON);
        registerWallet(ModItems.WALLET_GOLD);
        registerWallet(ModItems.WALLET_EMERALD);
        registerWallet(ModItems.WALLET_DIAMOND);
        registerWallet(ModItems.WALLET_NETHERITE);
        registerWallet(ModItems.WALLET_NETHER_STAR);
        registerWallet(ModItems.WALLET_ENDER_DRAGON);
    }

    private static void registerWallet(RegistryObject<? extends WalletItem> wallet) { registerWallet(wallet.get()); }
    private static void registerWallet(WalletItem wallet) { CuriosRendererRegistry.register(wallet, WalletCurioRenderer.supplier()); }
}
