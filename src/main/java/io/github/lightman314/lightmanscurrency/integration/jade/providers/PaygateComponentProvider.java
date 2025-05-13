package io.github.lightman314.lightmanscurrency.integration.jade.providers;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.PaygateBlockEntity;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class PaygateComponentProvider implements IBlockComponentProvider {

    public final ResourceLocation ID = VersionUtil.lcResource("paygate");
    public static final PaygateComponentProvider INSTANCE = new PaygateComponentProvider();

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if(accessor.getBlockEntity() instanceof PaygateBlockEntity be && be.getTimer() > 0)
        {
            int seconds = MathUtil.DivideByAndRoundUp(be.getTimer(),20);
            tooltip.add(LCText.TOOLTIP_TRADER_PAYGATE_TIME_REMAINING.get(EasyText.empty().append(String.valueOf(seconds)).append(LCText.TIME_UNIT_SECOND.shortText.get())).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public ResourceLocation getUid() { return ID; }

}
