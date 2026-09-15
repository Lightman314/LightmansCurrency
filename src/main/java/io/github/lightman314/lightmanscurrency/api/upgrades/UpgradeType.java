package io.github.lightman314.lightmanscurrency.api.upgrades;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.AbstractType;
import io.github.lightman314.lightmanscurrency.api.text.MultiLineTextEntry;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.upgrades.event.UpgradeEvent;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class UpgradeType extends AbstractType<UpgradeType> {

    public static final Identifier EMPTY_SLOT_SPRITE = LCApi.id("container/slot/upgrade");

    public static final TextEntry TOOLTIP_UPGRADE_TARGETS = TextEntry.tooltip(LCApi.MODID,"upgrade.targets");
    public static final TextEntry TOOLTIP_UPGRADE_UNIQUE = TextEntry.tooltip(LCApi.MODID,"upgrade.unique");

    private List<Component> targets = null;

    private final boolean unique;
    protected UpgradeType(boolean unique) { this.unique = unique; }

    public static UpgradeType create() { return new UpgradeType(false); }
    public static UpgradeType createUnique(boolean unique) { return new UpgradeType(true); }

    public static UpgradeType createWithTooltip(Supplier<Component> tooltip) { return createWithTooltip(false,tooltip); }
    public static UpgradeType createWithTooltip(boolean unique,Supplier<Component> tooltip) { return new WithTooltips(unique,() -> List.of(tooltip.get())); }
    public static UpgradeType createWithTooltips(Supplier<List<Component>> tooltips) { return createWithTooltips(false,tooltips); }
    public static UpgradeType createWithTooltips(boolean unique,Supplier<List<Component>> tooltips) { return new WithTooltips(unique,tooltips); }

    //Text Entry Helpers
    public static UpgradeType createWithTooltip(TextEntry tooltip) { return createWithTooltips(tooltip::getAsList); }
    public static UpgradeType createWithTooltip(boolean unique,TextEntry tooltip) { return createWithTooltips(unique,tooltip::getAsList); }
    public static UpgradeType createWithTooltip(MultiLineTextEntry tooltip) { return createWithTooltips(tooltip::get); }
    public static UpgradeType createWithTooltip(boolean unique,MultiLineTextEntry tooltip) { return createWithTooltips(unique,tooltip::get); }

    public final boolean isUnique() { return this.unique; }

    protected final List<Component> getTargets() {
        if(this.targets == null)
        {
            List<Component> targets = new ArrayList<>();
            //Post the collect upgrade targets event
            NeoForge.EVENT_BUS.post(new UpgradeEvent.CollectUpgradeTargetsEvent(this,targets));
            this.targets = ImmutableList.copyOf(targets);
        }
        return this.targets;
    }

    //To be overridden by children to add more tooltips for the upgrade
    protected void additionalTooltips(Item.TooltipContext context,Consumer<Component> builder,TooltipFlag flag,DataComponentGetter components) { }

    public static UnaryOperator<Item.Properties> buildProperties(Holder<UpgradeType> holder) { return buildProperties(holder,UnaryOperator.identity()); }
    public static UnaryOperator<Item.Properties> buildProperties(Holder<UpgradeType> holder,UnaryOperator<Item.Properties> additional) {
        return p -> additional.apply(p.component(LCDataComponents.UPGRADE_TYPE,new UpgradeHolder(holder)));
    }

    @Override
    protected final UpgradeType getEntry() { return this; }
    @Override
    protected final Registry<UpgradeType> getRegistry() { return LCRegistries.Upgrades.UPGRADES; }
    @Override
    protected final String getName() { return "UpgradeType"; }

    private static class WithTooltips extends UpgradeType {

        private final Supplier<List<Component>> tooltips;
        public WithTooltips(boolean unique,Supplier<List<Component>> tooltips) { super(unique); this.tooltips = tooltips; }

        @Override
        protected void additionalTooltips(Item.TooltipContext context, Consumer<Component> builder, TooltipFlag flag, DataComponentGetter components) {
            this.tooltips.get().forEach(builder);
        }

    }

}