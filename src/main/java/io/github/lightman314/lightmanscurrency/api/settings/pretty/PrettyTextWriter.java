package io.github.lightman314.lightmanscurrency.api.settings.pretty;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class PrettyTextWriter {

    public static final PrettyTextWriter DEFAULT = new DefaultWriter();

    private final static List<PrettyTextWriter> registry = new ArrayList<>();
    public static void register(PrettyTextWriter writer) { if(!registry.contains(writer)) registry.add(writer); }

    public static PrettyTextWriter getForStack(ItemStack stack)
    {
        for(PrettyTextWriter writer : registry)
        {
            if(writer.worksOnStack(stack))
                return writer;
        }
        return DEFAULT;
    }

    public abstract boolean worksOnStack(ItemStack stack);

    public abstract ItemStack writeLinesToStack(@Nullable Player player, ItemStack stack, PrettyTextData data);

    private static class DefaultWriter extends PrettyTextWriter
    {
        @Override
        public boolean worksOnStack(ItemStack stack) { return true; }
        @Override
        public ItemStack writeLinesToStack(@Nullable Player player, ItemStack stack, PrettyTextData data) {
            //Only change the name of the item
            stack.set(DataComponents.CUSTOM_NAME,data.machineName());
            return stack;
        }
    }

}
