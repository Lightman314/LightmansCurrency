package io.github.lightman314.lightmanscurrency.integration.jade.providers;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.PaygateBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.PaygateBlockEntity.OutputVisibilityData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.List;

public class PaygateComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    public final ResourceLocation ID = VersionUtil.lcResource("paygate");
    public static final PaygateComponentProvider INSTANCE = new PaygateComponentProvider();

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if(accessor.getBlockEntity() instanceof PaygateBlockEntity be && be.isActive() && accessor.getServerData().contains("OutputData"))
        {
            List<OutputVisibilityData> list = PaygateBlockEntity.parseVisibilityData(accessor.getServerData().getCompound("OutputData"));
            for(OutputVisibilityData data : list)
            {
                int seconds = MathUtil.DivideByAndRoundUp(data.timer(),20);
                Component timeText = EasyText.literal(String.valueOf(seconds)).append(LCText.TIME_UNIT_SECOND.shortText.get());
                String name = data.name();
                if(name == null && data.sides().size() == 6)
                    tooltip.add(LCText.TOOLTIP_TRADER_PAYGATE_TIME_REMAINING.get(timeText).withStyle(ChatFormatting.GRAY));
                else
                {
                    Component nameText;
                    if(name == null)
                        nameText = getSidesList(data);
                    else
                        nameText = EasyText.literal(name);
                    tooltip.add(LCText.TOOLTIP_TRADER_PAYGATE_SIDED_TIME_REMAINING.get(nameText,timeText).withStyle(ChatFormatting.GRAY));
                }
            }
        }
    }

    private MutableComponent getSidesList(OutputVisibilityData data)
    {
        MutableComponent text = EasyText.empty();
        for(Direction side : data.sides())
        {
            if(!text.getString().isEmpty())
                text.append(LCText.GUI_SEPERATOR.get());
            text.append(LCText.GUI_INPUT_SIDES.get(side).get());
        }
        return text;
    }

    @Override
    public ResourceLocation getUid() { return ID; }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor accessor) {
        if(accessor.getBlockEntity() instanceof PaygateBlockEntity be)
            be.saveRedstoneData(compoundTag);
    }

}