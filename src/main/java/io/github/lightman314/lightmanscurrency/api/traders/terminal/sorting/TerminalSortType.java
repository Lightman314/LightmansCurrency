package io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting;

import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Comparator;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class TerminalSortType implements Comparator<TraderData> {

    private final SortTypeKey key;
    protected TerminalSortType(ResourceLocation id) { this(new SortTypeKey(id,false)); }
    private TerminalSortType(SortTypeKey key) { this.key = key;}

    public ResourceLocation getID() { return this.key.id(); }
    public SortTypeKey getKey() { return this.key; }

    public int sortPriority() { return 0; }

    protected String getTranslationKey() { return "gui." + this.getID().getNamespace() + ".terminal.sort_type." + this.getID().getPath(); }
    public boolean supportsInverted() { return true; }
    public Component getName() { return EasyText.translatable(this.getTranslationKey()); }
    public Component getInvertedName() { return EasyText.translatable(this.getTranslationKey() + ".inverted"); }

    private InvertedSortType inverted = null;
    @Nullable
    public final TerminalSortType getInverted()
    {
        if(this.supportsInverted() && this.inverted == null)
            this.inverted = new InvertedSortType(this);
        return this.inverted;
    }

    public final int compare(TraderData traderA,TraderData traderB)
    {
        int result = this.sort(traderA,traderB);
        if(result == 0)
            return Long.compare(traderA.getID(),traderB.getID());
        return result;
    }

    protected abstract int sort(TraderData traderA, TraderData traderB);

    private static class InvertedSortType extends TerminalSortType
    {
        private final TerminalSortType parent;
        private InvertedSortType(TerminalSortType parent) { super(new SortTypeKey(parent.getID(),true)); this.parent = parent; }
        @Override
        public boolean supportsInverted() { return false; }
        @Override
        public Component getName() { return this.parent.getInvertedName(); }
        //Sort Backwards by inverting the inputs
        @Override
        protected int sort(TraderData traderA, TraderData traderB) { return this.parent.sort(traderB,traderA); }
    }

}
