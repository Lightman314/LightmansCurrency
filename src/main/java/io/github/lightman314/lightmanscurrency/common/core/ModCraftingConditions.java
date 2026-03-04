package io.github.lightman314.lightmanscurrency.common.core;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.conditions.ConfigCraftingCondition;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModCraftingConditions {

    public static final DeferredRegister<MapCodec<? extends ICondition>> REGISTER = DeferredRegister.create(NeoForgeRegistries.CONDITION_SERIALIZERS,LightmansCurrency.MODID);

	static {
		REGISTER.register("configured", () -> ConfigCraftingCondition.CODEC);
	}

}
