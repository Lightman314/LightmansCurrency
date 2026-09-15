package io.github.lightman314.lightmanscurrency.api.upgrades;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.List;
import java.util.function.Consumer;

public record UpgradeHolder(Holder<UpgradeType> holder) implements TooltipProvider {

    public static final Codec<UpgradeHolder> CODEC = LCRegistries.Upgrades.UPGRADES.holderByNameCodec().xmap(UpgradeHolder::new,UpgradeHolder::holder);
    public static final StreamCodec<RegistryFriendlyByteBuf,UpgradeHolder> STREAM_CODEC = ByteBufCodecs.holderRegistry(LCRegistries.Upgrades.UPGRADES_KEY).map(UpgradeHolder::new,UpgradeHolder::holder);

    public UpgradeType get() { return this.holder.value(); }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components) {
        try {
            UpgradeType type = this.get();
            //Add upgrade-specific tooltips first
            type.additionalTooltips(context,consumer,flag,components);
            //Add Unique flag
            if(type.isUnique())
                consumer.accept(UpgradeType.TOOLTIP_UPGRADE_UNIQUE.getWithStyle(ChatFormatting.BOLD,ChatFormatting.GOLD));
            //Add Targets
            List<Component> targets = type.getTargets();
            if(!targets.isEmpty())
            {
                consumer.accept(UpgradeType.TOOLTIP_UPGRADE_TARGETS.getWithStyle(ChatFormatting.GRAY));
                for(Component target : targets)
                    consumer.accept(target.copy().withStyle(ChatFormatting.GRAY));
            }
        } catch (Exception ignored) {}

    }

    @Override
    public String toString() { return this.holder.getKey().identifier().toString(); }

}
