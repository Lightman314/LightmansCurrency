package io.github.lightman314.lightmanscurrency.integration.curios.client;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.client.ICurioRenderer;

@Mod(value = LCApi.MODID,dist = Dist.CLIENT,depends = "curios")
public class LCCuriosClient {

    public LCCuriosClient(IEventBus modBus) {
        modBus.addListener(LCCuriosClient::clientSetup);
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for(var holder : LCItems.REGISTER.getEntries()) {
                if(holder.get() instanceof WalletItem item)
                    ICurioRenderer.register(item,WalletCurioRenderer.SOURCE);
            }
        });
    }

}
