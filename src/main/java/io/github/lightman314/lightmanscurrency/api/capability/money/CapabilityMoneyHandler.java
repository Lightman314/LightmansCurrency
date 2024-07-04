package io.github.lightman314.lightmanscurrency.api.capability.money;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.Nullable;

public class CapabilityMoneyHandler {

    public static final ItemCapability<IMoneyHandler,Void> MONEY_HANDLER_ITEM = ItemCapability.create(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"money_handler_item"),IMoneyHandler.class,Void.class);
    public static final BlockCapability<IMoneyHandler,@Nullable Direction> MONEY_HANDLER_BLOCK = BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"money_handler_block"),IMoneyHandler.class);
    public static final EntityCapability<IMoneyHandler,Void> MONEY_HANDLER_ENTITY = EntityCapability.create(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"money_handler_entity"),IMoneyHandler.class,Void.class);

}
