package io.github.lightman314.lightmanscurrency.features.cash_register;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public record TraderPositionSet(List<BlockPos> positions) implements TooltipProvider {
    public TraderPositionSet(List<BlockPos> positions) { this.positions = forceImmutability(positions); }

    public boolean isEmpty() { return this.positions.isEmpty(); }

    public static TraderPositionSet EMPTY = new TraderPositionSet(List.of());

    public static final TextEntry TOOLTIP_INFO = TextEntry.tooltip(LCApi.MODID,"cash_register.info");
    public static final TextEntry TOOLTIP_INSTRUCTIONS = TextEntry.tooltip(LCApi.MODID,"cash_register.instructions");
    public static final TextEntry TOOLTIP_HOLD_CRTL = TextEntry.message(LCApi.MODID,"cash_register.hold_ctrl");
    public static final TextEntry TOOLTIP_DETAILS = TextEntry.message(LCApi.MODID,"cash_register.details");


    public static final Codec<TraderPositionSet> CODEC = BlockPos.CODEC.listOf()
            .xmap(TraderPositionSet::new, TraderPositionSet::positions);
    public static final StreamCodec<ByteBuf, TraderPositionSet> STREAM_CODEC = BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list())
            .map(TraderPositionSet::new, TraderPositionSet::positions);

    private static List<BlockPos> forceImmutability(List<BlockPos> set) {
        List<BlockPos> temp = new ArrayList<>();
        for(BlockPos pos : set)
            temp.add(pos.immutable());
        return List.copyOf(temp);
    }

    public TraderPositionSet withPosition(BlockPos addedPosition) {
        if(this.positions.contains(addedPosition))
            return this;
        List<BlockPos> temp = new ArrayList<>(this.positions);
        temp.add(addedPosition);
        return new TraderPositionSet(temp);
    }

    @Override
    public void addToTooltip(Item.TooltipContext context,Consumer<Component> builder,TooltipFlag flag,DataComponentGetter components) {
        builder.accept(TOOLTIP_INFO.get(this.positions.size()));
        if(flag.hasControlDown() || this.isEmpty())
            builder.accept(TOOLTIP_INSTRUCTIONS.get());

        if(flag.hasControlDown()) {
            //Display details of the registered blocks
            int index = 1;
            for(BlockPos pos : this.positions)
                builder.accept(TOOLTIP_DETAILS.get(index++,pos.getX(),pos.getY(),pos.getZ()));
        }
        else if(!this.isEmpty())
            builder.accept(TOOLTIP_HOLD_CRTL.getWithStyle(ChatFormatting.YELLOW));
    }
}