package io.github.lightman314.lightmanscurrency.common.entity.merchant.villager;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModRegistries;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(LightmansCurrency.MODID)
public class CustomPointsOfInterest {
	
	public static void init()
	{
		ModRegistries.POI_TYPES.register("banker", () -> new PointOfInterestType("banker", PointOfInterestType.getBlockStates(ModBlocks.MACHINE_ATM.get()), 1, 1));
		ModRegistries.POI_TYPES.register("cashier", () -> new PointOfInterestType("cashier", PointOfInterestType.getBlockStates(ModBlocks.CASH_REGISTER.get()), 1, 1));
	}
	
	public static final PointOfInterestType BANKER = null;
	public static final PointOfInterestType CASHIER = null;
	
}
