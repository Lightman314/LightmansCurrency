package io.github.lightman314.lightmanscurrency.api.trader.trade.resources;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.capabilities.CombinedEnergyHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.SortableMoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.builtin.EmptyMoneyResource;
import io.github.lightman314.lightmanscurrency.api.money.resource.builtin.SortedMoneyResourceHandler;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EmptyEnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public abstract class ResourceType<T,X> {



    public abstract Identifier getType();

    public abstract X empty();
    public abstract X combine(List<T> resources);
    @ApiStatus.Internal
    public final Object tryCombine(List<?> resources)
    {
        try { return this.combine((List<T>)resources);
        } catch (ClassCastException e) { LightmansCurrency.LogError("Error combining a resource!"); return empty(); }
    }

    @Override
    public final int hashCode() { return this.getType().hashCode(); }
    @Override
    public final String toString() { return "ResourceType[" + this.getType() + "]"; }

    public static abstract class SimpleType<T> extends ResourceType<T,T> {}

    public static final class ResourceHandlerType<T extends Resource> extends SimpleType<ResourceHandler<T>>
    {
        private final Identifier type;
        public ResourceHandlerType(Identifier type) { this.type = type; }
        @Override
        public Identifier getType() { return this.type; }

        @Override
        public ResourceHandler<T> empty() { return EmptyResourceHandler.instance(); }

        @Override
        public ResourceHandler<T> combine(List<ResourceHandler<T>> resources) { return new CombinedResourceHandler<>(resources); }
    }

    public static final class EnergyResourceType extends SimpleType<EnergyHandler>
    {
        private static final Identifier TYPE = Identifier.fromNamespaceAndPath("neoforge","energy");
        @Override
        public Identifier getType() { return TYPE; }
        @Override
        public EnergyHandler empty() { return EmptyEnergyHandler.INSTANCE; }
        @Override
        public EnergyHandler combine(List<EnergyHandler> resources) { return new CombinedEnergyHandler(resources); }
    }

    public static final class MoneyResourceType extends ResourceType<SortableMoneyResourceHandler,MoneyResourceHandler>
    {
        private static final Identifier TYPE = LCApi.id("money");
        private static final ResourceType<SortableMoneyResourceHandler,MoneyResourceHandler> INSTANCE = new MoneyResourceType();
        @Override
        public Identifier getType() { return TYPE; }
        @Override
        public MoneyResourceHandler empty() { return EmptyMoneyResource.INSTANCE; }
        @Override
        public MoneyResourceHandler combine(List<SortableMoneyResourceHandler> resources) { return new SortedMoneyResourceHandler(resources); }
    }

}