package io.github.lightman314.lightmanscurrency.entity.merchant.villager;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.core.ModRegistries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(LightmansCurrency.MODID)
public class CustomPointsOfInterest {
	
	public static void init()
	{
		ModRegistries.POI_TYPES.register("banker", () -> new PoiType("banker", PoiType.getBlockStates(ModBlocks.MACHINE_ATM.get()), 1, 1));
		ModRegistries.POI_TYPES.register("cashier", () -> new PoiType("cashier", PoiType.getBlockStates(ModBlocks.CASH_REGISTER.get()), 1, 1));
	}
	
	public static final PoiType BANKER = null;
	public static final PoiType CASHIER = null;
	
}
