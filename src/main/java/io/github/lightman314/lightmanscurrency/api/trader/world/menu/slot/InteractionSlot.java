package io.github.lightman314.lightmanscurrency.api.trader.world.menu.slot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.resource.access.SidedItemAccess;
import io.github.lightman314.lightmanscurrency.api.helpers.resource.access.SlotItemAccess;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.world.menu.slots.EasyVanillaSlot;
import io.github.lightman314.lightmanscurrency.api.world.menu.slots.IEasySlot;
import net.minecraft.resources.Identifier;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public final class InteractionSlot extends EasyVanillaSlot {

    public final Map<Identifier,InteractionSlotData> slotData;
    private final List<Identifier> noItemIcons;

    private final ISidedContext context;

    public InteractionSlot(Map<Identifier,InteractionSlotData> slotData, int x, int y, ISidedContext context) {
        super(new SimpleContainer(1), 0, x, y);
        this.context = context;
        this.slotData = ImmutableMap.copyOf(slotData);
        ImmutableList.Builder<Identifier> builder = ImmutableList.builder();
        for(InteractionSlotData data : this.slotData.values())
        {
            Identifier noItemIcon = data.getNoItemIcon();
            if(noItemIcon != null)
                builder.add(noItemIcon);
        }
        this.noItemIcons = builder.build();
    }

    public boolean isType(Identifier type) { return this.slotData.containsKey(type); }

    @Override
    public boolean isActive() { return super.isActive() && !this.slotData.isEmpty(); }
    @Override
    public int getMaxStackSize() { return 1; }

    public void wrapResource(TradeContext.Builder builder) {
        SidedItemAccess access = new SidedItemAccess(SlotItemAccess.of(this),this.context);
        for(InteractionSlotData data : this.slotData.values())
            data.wrapResource(builder,access);
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) { return InteractionSlotData.allowItemInSlot(this.slotData.values(),itemStack,this.context); }

    @Override
    @Nullable
    public Identifier getNoItemIcon() { return IEasySlot.getNoItemIcon(this.noItemIcons); }

}