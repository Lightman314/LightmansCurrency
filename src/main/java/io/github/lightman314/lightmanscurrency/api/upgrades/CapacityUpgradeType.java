package io.github.lightman314.lightmanscurrency.api.upgrades;

import com.google.common.primitives.Ints;
import io.github.lightman314.lightmanscurrency.api.helpers.NumberHelper;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.upgrades.data.NumberSource;
import io.github.lightman314.lightmanscurrency.api.upgrades.world.UpgradeStorage;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

import java.util.function.*;

public class CapacityUpgradeType extends UpgradeType {

    private final Function<String,Component> tooltip;
    public CapacityUpgradeType(TextEntry tooltip) { this(tooltip,false); }
    public CapacityUpgradeType(TextEntry tooltip,boolean unique) { this(tooltip::get,unique); }
    public CapacityUpgradeType(Function<String,Component> tooltip) { this(tooltip,false); }
    public CapacityUpgradeType(Function<String,Component> tooltip,boolean unique) { super(unique); this.tooltip = tooltip; }

    public static int getBonusCapacity(DataComponentGetter item) {
        NumberSource source = item.get(LCDataComponents.CAPACITY_BONUS);
        return source == null ? 0 : source.getInt();
    }

    public static int getTotalCapacity(int defaultCapacity,UpgradeStorage storage,Holder<UpgradeType> type) { return getTotalCapacity(defaultCapacity,storage,r -> r.is(type)); }
    public static int getTotalCapacity(int defaultCapacity,UpgradeStorage storage,Predicate<UpgradeReference> filter) {
        if(storage == null)
            return defaultCapacity;
        long capacity = defaultCapacity;
        for(UpgradeReference r : storage) {
            if(filter.test(r))
                capacity += getBonusCapacity(r.data());
        }
        return Ints.saturatedCast(capacity);
    }

    @Override
    public void additionalTooltips(Item.TooltipContext context, Consumer<Component> builder, TooltipFlag flag, DataComponentGetter components) {
        builder.accept(this.tooltip.apply(NumberHelper.prettyInteger(getBonusCapacity(components))));
    }

    public static UnaryOperator<Item.Properties> buildProperties(Holder<UpgradeType> type, Supplier<NumberSource> numberSource) {
        return buildProperties(type,p -> p.component(LCDataComponents.CAPACITY_BONUS.get(),numberSource.get()));
    }

}