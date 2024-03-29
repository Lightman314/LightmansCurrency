package io.github.lightman314.lightmanscurrency.integration.supplementaries;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.VillagerTradeManager;
import net.mehvahdjukaar.supplementaries.api.forge.RedMerchantTradesEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;

public final class LCSupplementaries {

    public static final String RED_MERCHANT_ID = "supplementaries:red_merchant";

    public static boolean triggerMixin = true;

    private LCSupplementaries() {}

    public static void setup() {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, LCSupplementaries::modifyRedMerchant);
        LightmansCurrency.LogDebug("Registered Red Merchant Trade event listener!");
    }

    private static void modifyRedMerchant(RedMerchantTradesEvent event) {
        try {
            LightmansCurrency.LogDebug("Red Merchant Trades Event was called. Applying modifications as configured!");
            //Disable mixin, as supplementaries finally
            triggerMixin = false;
            if(LCConfig.COMMON.changeModdedTrades.get())
                VillagerTradeManager.replaceExistingTrades(RED_MERCHANT_ID, event.getTrades());
        } catch (Throwable t) { LightmansCurrency.LogError("Error occurred modifying Red Merchant Trades", t); }
    }

}
