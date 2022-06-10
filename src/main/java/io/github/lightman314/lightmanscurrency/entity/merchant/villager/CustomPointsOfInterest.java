package io.github.lightman314.lightmanscurrency.entity.merchant.villager;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.core.ModRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

public class CustomPointsOfInterest {
	
	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	static {
		
		RegistryObject<PoiType> temp = ModRegistries.POI_TYPES.register("banker", () -> new PoiType(getBlockStates(ModBlocks.MACHINE_ATM.get()), 1, 1));
		BANKER = temp;
		BANKER_KEY = temp.getKey();
		
		temp = ModRegistries.POI_TYPES.register("cashier", () -> new PoiType(getBlockStates(ModBlocks.CASH_REGISTER.get()), 1, 1));
		CASHIER = temp;
		CASHIER_KEY = temp.getKey();
		
	}
	
	public static final ResourceKey<PoiType> BANKER_KEY;
	public static final ResourceKey<PoiType> CASHIER_KEY;
	
	public static final RegistryObject<PoiType> BANKER;
	public static final RegistryObject<PoiType> CASHIER;
	
	private static Set<BlockState> getBlockStates(Block block) {
		return ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates());
	}
	
}
