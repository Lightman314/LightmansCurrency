package io.github.lightman314.lightmanscurrency.common.capability;

import net.minecraftforge.common.capabilities.CapabilityInject;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

public class CurrencyCapabilities {

	@CapabilityInject(IWalletHandler.class)
	public static final Capability<IWalletHandler> WALLET;
	@CapabilityInject(ISpawnTracker.class)
	public static final Capability<ISpawnTracker> SPAWN_TRACKER;
	
	public static final ResourceLocation ID_WALLET = new ResourceLocation(LightmansCurrency.MODID,"wallet");
	public static final ResourceLocation ID_SPAWNER_TRACKER = new ResourceLocation(LightmansCurrency.MODID,"spawner_tracker");
	
	static {
		WALLET = null;
		SPAWN_TRACKER= null;
	}
	
}
