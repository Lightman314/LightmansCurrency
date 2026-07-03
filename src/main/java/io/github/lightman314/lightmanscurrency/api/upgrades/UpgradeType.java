package io.github.lightman314.lightmanscurrency.api.upgrades;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import io.github.lightman314.lightmanscurrency.api.upgrades.event.UpgradeEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class UpgradeType implements TooltipProvider {

    public static final Identifier EMPTY_SLOT_SPRITE = LCApi.id("container/slot/upgrade");

    private List<Component> targets = null;

    private final boolean unique;
    public UpgradeType(boolean unique) { this.unique = unique; }

    public final boolean is(Supplier<? extends UpgradeType> type) { return this == type.get(); }

    public final boolean isUnique() { return this.unique; }

    private void assertTargetsCollected() {
        if(this.targets == null)
        {
            List<Component> targets = new ArrayList<>();
            //Post the collect upgrade targets event
            NeoForge.EVENT_BUS.post(new UpgradeEvent.CollectUpgradeTargetsEvent(this,targets));
            this.targets = ImmutableList.copyOf(targets);
        }
    }

    @Override
    public final void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components) {
        //Add upgrade-specific tooltips first
        this.additionalTooltips(context,consumer,flag,components);
        //Add Unique flag
        if(this.isUnique())
            consumer.accept(LCText.Items.TOOLTIP_UPGRADE_UNIQUE.getWithStyle(ChatFormatting.BOLD,ChatFormatting.GOLD));
        //Add Targets
        this.assertTargetsCollected();
        if(!this.targets.isEmpty())
        {
            consumer.accept(LCText.Items.TOOLTIP_UPGRADE_TARGETS.getWithStyle(ChatFormatting.GRAY));
            for(Component target : this.targets)
                consumer.accept(target.copy().withStyle(ChatFormatting.GRAY));
        }
    }

    //To be overridden by children to add more tooltips for the upgrade
    protected void additionalTooltips(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag,DataComponentGetter components) { }

}