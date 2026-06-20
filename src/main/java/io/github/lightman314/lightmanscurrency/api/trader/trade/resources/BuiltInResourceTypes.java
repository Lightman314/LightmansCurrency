package io.github.lightman314.lightmanscurrency.api.trader.trade.resources;

import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.SortableMoneyResourceHandler;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class BuiltInResourceTypes {

    public static final ResourceType.SimpleType<ResourceHandler<ItemResource>> ITEM = new ResourceType.ResourceHandlerType<>(Identifier.fromNamespaceAndPath("neoforge","items"));
    public static final ResourceType.SimpleType<ResourceHandler<FluidResource>> FLUID = new ResourceType.ResourceHandlerType<>(Identifier.fromNamespaceAndPath("neoforge","fluids"));
    public static final ResourceType.SimpleType<EnergyHandler> ENERGY = new ResourceType.EnergyResourceType();
    public static final ResourceType<SortableMoneyResourceHandler, MoneyResourceHandler> MONEY = new ResourceType.MoneyResourceType();

}
