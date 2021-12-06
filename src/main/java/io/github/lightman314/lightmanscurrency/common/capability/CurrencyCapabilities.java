package io.github.lightman314.lightmanscurrency.common.capability;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class CurrencyCapabilities {

	public static final Capability<IWalletHandler> WALLET = CapabilityManager.get(new CapabilityToken<>() {});
	
	public static final ResourceLocation ID_WALLET = new ResourceLocation(LightmansCurrency.MODID,"wallet");
	
}
