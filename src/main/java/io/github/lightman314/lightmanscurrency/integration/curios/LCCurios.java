package io.github.lightman314.lightmanscurrency.integration.curios;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import top.theillusivec4.curios.api.CuriosApi;

@Mod(value = LCApi.MODID,depends = "curios")
public final class LCCurios {

    public LCCurios(IEventBus modBus) {

        //Replace the helper with a properly implemented one
        LCCuriosHelperImpl.init();
        //Listen to the common setup event
        modBus.addListener(LCCurios::commonSetup);
        
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        for(var holder : LCItems.REGISTER.getEntries()) {
            if(holder.get() instanceof WalletItem item)
                CuriosApi.registerCurio(item,WalletCurio.INSTANCE);
        }
    }

}