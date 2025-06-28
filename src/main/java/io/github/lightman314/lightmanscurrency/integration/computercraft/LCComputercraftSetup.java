package io.github.lightman314.lightmanscurrency.integration.computercraft;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class LCComputercraftSetup {

    public static void setup()
    {
        try {
            LCComputerHelper.setup(FMLJavaModLoadingContext.get().getModEventBus());
        }catch (Exception e) {
            LightmansCurrency.LogError("An unexpected error occurred!",e);
        }
    }

}