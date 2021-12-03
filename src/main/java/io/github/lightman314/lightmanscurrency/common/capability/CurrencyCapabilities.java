package io.github.lightman314.lightmanscurrency.common.capability;

import net.minecraftforge.common.capabilities.CapabilityInject;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

public class CurrencyCapabilities {

	@CapabilityInject(IWalletHandler.class)
	public static final Capability<IWalletHandler> WALLET;
	
	public static final ResourceLocation ID_WALLET = new ResourceLocation(LightmansCurrency.MODID,"wallet");
	
	static {
		WALLET = null;
	}
	
}
