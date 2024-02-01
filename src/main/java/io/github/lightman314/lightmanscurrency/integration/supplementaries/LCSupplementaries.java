package io.github.lightman314.lightmanscurrency.integration.supplementaries;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.VillagerTradeManager;
import net.mehvahdjukaar.supplementaries.api.forge.RedMerchantTradesEvent;
import net.minecraftforge.common.MinecraftForge;

public final class LCSupplementaries {

    private LCSupplementaries() {}

    public static void setup() {
        MinecraftForge.EVENT_BUS.addListener(LCSupplementaries::modifyRedMerchant);
        LightmansCurrency.LogDebug("Registered Red Merchant Trade event listener!");
    }

    private static void modifyRedMerchant(RedMerchantTradesEvent event) {
        try {
            LightmansCurrency.LogDebug("Red Merchant Trades Event was called. Applying modifications as configured!");
            if(LCConfig.COMMON.changeModdedTrades.get())
                VillagerTradeManager.replaceExistingTrades("supplementaries:red_merchant", event.getTrades());
        } catch (Throwable t) { LightmansCurrency.LogError("Error occurred modifying Red Merchant Trades", t); }
    }

}
