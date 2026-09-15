package io.github.lightman314.lightmanscurrency.api.trader.world.menu.slot;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.resource.access.SidedItemAccess;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.BuiltInResourceTypes;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.ResourceType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.transfer.access.ItemAccess;

import javax.annotation.Nullable;

public class CapabilitySlotData<T> extends InteractionSlotData {

    public static final InteractionSlotData ITEMS = new CapabilitySlotData<>(Identifier.fromNamespaceAndPath("neoforge","items"),Capabilities.Item.ITEM,BuiltInResourceTypes.ITEM,LCApi.id("container/slot/bundle"));
    public static final InteractionSlotData FLUIDS = new CapabilitySlotData<>(Identifier.fromNamespaceAndPath("neoforge","fluids"),Capabilities.Fluid.ITEM,BuiltInResourceTypes.FLUID,LCApi.id("container/slot/bucket"));
    public static final InteractionSlotData ENERGY = new CapabilitySlotData<>(Identifier.fromNamespaceAndPath("neoforge","energy"),Capabilities.Energy.ITEM,BuiltInResourceTypes.ENERGY,LCApi.id("container/slot/battery"));

    private final Identifier id;
    private final ItemCapability<T,ItemAccess> capability;
    private final ResourceType<T,?> resourceType;
    @Nullable
    private final Identifier background;
    public CapabilitySlotData(Identifier id,ItemCapability<T,ItemAccess> capability,ResourceType<T,?> resourceType) { this(id,capability,resourceType,null); }
    public CapabilitySlotData(Identifier id,ItemCapability<T,ItemAccess> capability,ResourceType<T,?> resourceType,@Nullable Identifier background) {
        this.id = id;
        this.capability = capability;
        this.resourceType = resourceType;
        this.background = background;
    }

    @Override
    public Identifier getType() { return this.id; }
    @Override
    public Identifier getNoItemIcon() { return this.background; }

    @Override
    public boolean allowItemInSlot(ItemStack item,SidedItemAccess access) { return access.getCapability(this.capability) != null; }
    @Override
    public void wrapResource(TradeContext.Builder builder,SidedItemAccess access) {
        T resource = access.getCapability(this.capability);
        if(resource != null)
            builder.withResource(this.resourceType,resource);
    }

}
