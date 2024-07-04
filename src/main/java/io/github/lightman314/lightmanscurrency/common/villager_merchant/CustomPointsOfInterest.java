package io.github.lightman314.lightmanscurrency.common.villager_merchant;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.ITallBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class CustomPointsOfInterest {
	
	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	static {

		BANKER = ModRegistries.POI_TYPES.register("banker", () -> new PoiType(getBlockStates(ModBlocks.ATM.get()), 1, 1));

		CASHIER = ModRegistries.POI_TYPES.register("cashier", () -> new PoiType(getBlockStates(ModBlocks.CASH_REGISTER.get()), 1, 1));
		
	}

	public static final Supplier<PoiType> BANKER;
	public static final ResourceKey<PoiType> BANKER_KEY = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "banker"));
	public static final Supplier<PoiType> CASHIER;
	public static final ResourceKey<PoiType> CASHIER_KEY = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "cashier"));


	private static Set<BlockState> getBlockStates(Block block) {
		if(block instanceof ITallBlock tallBlock)
			return ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates().stream().filter(tallBlock::getIsBottom).collect(Collectors.toSet()));
		return ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates());
	}
	
}
