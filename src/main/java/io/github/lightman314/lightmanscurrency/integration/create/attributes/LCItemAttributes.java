package io.github.lightman314.lightmanscurrency.integration.create.attributes;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class LCItemAttributes {

    public static final DeferredRegister<ItemAttributeType> REGISTRY = DeferredRegister.create(CreateBuiltInRegistries.ITEM_ATTRIBUTE_TYPE, LightmansCurrency.MODID);

    public static void init() {}

    public static final Supplier<ItemAttributeType> VARIANT_ATTRIBUTE;

    static {
        VARIANT_ATTRIBUTE = register("model_variant", VariantAttributeType::new);
    }

    private static Supplier<ItemAttributeType> register(String id,Supplier<ItemAttributeType> builder) { return REGISTRY.register(id,builder); }

}
