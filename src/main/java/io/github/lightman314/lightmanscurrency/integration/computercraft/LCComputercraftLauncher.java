package io.github.lightman314.lightmanscurrency.integration.computercraft;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.neoforged.bus.api.IEventBus;

public class LCComputercraftLauncher {

    public static void setup(IEventBus modBus)
    {
        try {
            LCComputerHelper.setup(modBus);
        }catch (Exception e) {
            LightmansCurrency.LogError("An unexpected error occurred!",e);
        }
    }

}
