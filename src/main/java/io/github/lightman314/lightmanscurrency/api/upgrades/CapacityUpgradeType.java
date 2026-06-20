package io.github.lightman314.lightmanscurrency.api.upgrades;

import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.upgrades.data.NumberSource;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

import java.util.function.Consumer;
import java.util.function.Function;

public class CapacityUpgradeType extends UpgradeType {

    private final Function<Integer,Component> tooltip;
    public CapacityUpgradeType(TextEntry tooltip) { this(tooltip,false); }
    public CapacityUpgradeType(TextEntry tooltip,boolean unique) { this(tooltip::get,unique); }
    public CapacityUpgradeType(Function<Integer,Component> tooltip) { this(tooltip,false); }
    public CapacityUpgradeType(Function<Integer,Component> tooltip,boolean unique) { super(unique); this.tooltip = tooltip; }

    public static int getBonusCapacity(DataComponentGetter item) {
        NumberSource source = item.get(LCDataComponents.CAPACITY_BONUS);
        return source == null ? 0 : source.getInt();
    }

    @Override
    public void additionalTooltips(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components) {
        consumer.accept(this.tooltip.apply(getBonusCapacity(components)));
    }

}