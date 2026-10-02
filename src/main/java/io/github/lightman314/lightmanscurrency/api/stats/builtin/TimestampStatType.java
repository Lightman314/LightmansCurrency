package io.github.lightman314.lightmanscurrency.api.stats.builtin;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeHelper;
import io.github.lightman314.lightmanscurrency.api.stats.StatType;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;

import javax.annotation.Nullable;
import java.util.List;

public class TimestampStatType extends StatType.Singleton<Long> {

    public static final StatType.Singleton<Long> TYPE = new TimestampStatType();

    public static final TextEntry GUI_TIME_NEVER = TextEntry.gui(LCApi.MODID,"lightmanscurrency.stat.timestamp.never");

    protected TimestampStatType() { super(Codec.LONG,ByteBufCodecs.LONG); }

    @Override
    protected Class<Long> getValueClass() { return Long.class; }
    @Override
    public Long addToValue(Long currentValue, Long addition) { return Math.max(addition,0); }
    @Override
    public Long getEmptyValue() { return 0L; }
    @Override
    public boolean isEmptyValue(Long value) { return value == 0; }
    @Override
    public Component getValueText(Long value) {
        if(value <= 0)
            return GUI_TIME_NEVER.get();
        return Component.literal(TimeHelper.formatTime(value));
    }

    @Nullable
    @Override
    public List<Component> getValueTooltip(Long value) {
        if(value <= 0)
            return null;
        return List.of(Component.literal(TimeHelper.formatTime(value)));
    }
}
