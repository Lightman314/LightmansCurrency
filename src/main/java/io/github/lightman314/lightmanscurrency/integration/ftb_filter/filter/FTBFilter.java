package io.github.lightman314.lightmanscurrency.integration.ftb_filter.filter;

import dev.ftb.mods.ftbfiltersystem.api.FTBFilterSystemAPI;
import dev.ftb.mods.ftbfiltersystem.api.FilterException;
import dev.ftb.mods.ftbfiltersystem.api.filter.DumpedFilter;
import dev.ftb.mods.ftbfiltersystem.api.filter.SmartFilter;
import io.github.lightman314.lightmanscurrency.api.filter.IItemTradeFilter;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.integration.ftb_filter.LCFTBFilterSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class FTBFilter implements IItemTradeFilter {

    public static FTBFilter INSTANCE = new FTBFilter();

    private FTBFilter() {}

    @Nullable
    @Override
    public Predicate<ItemStack> getFilter(ItemStack stack) {
        if(LCFTBFilterSystem.hasFilter(stack))
            return other -> FTBFilterSystemAPI.api().doesFilterMatch(stack,other);
        return null;
    }

    @Nullable
    @Override
    public List<Component> getCustomTooltip(ItemStack stack) {
        if(FTBFilterSystemAPI.api().isFilterItem(stack))
        {
            try {
                List<DumpedFilter> dump = FTBFilterSystemAPI.api().dump(FTBFilterSystemAPI.api().parseFilter(stack));
                List<Component> tooltip = new ArrayList<>();
                for(DumpedFilter filter : dump)
                {
                    MutableComponent line = indent(filter);
                    SmartFilter f = filter.filter();
                    line.append(f.getDisplayName());
                    //Display argument as well so long as this isn't a Compound filter (as then the argument is the children, and they'll be displayed on a different line)
                    if(!(f instanceof SmartFilter.Compound))
                    {
                        //Don't bother displaying the arg if it's empty
                        Component arg = f.getDisplayArg();
                        if(!arg.getString().isEmpty())
                            line.append("(").append(arg).append(")");
                    }
                }
                return tooltip;
            } catch (FilterException ignored) {}
        }
        return null;
    }

    private static MutableComponent indent(DumpedFilter filter)
    {
        MutableComponent line = EasyText.empty();
        if(filter.indent() > 0)
        {
            StringBuilder b = new StringBuilder(" ");
            while(b.length() < filter.indent())
                b.append(" ");
            line.append(b.toString());
        }
        return line;
    }

}
