package io.github.lightman314.lightmanscurrency.api.helpers.time;

import io.github.lightman314.lightmanscurrency.api.text.TimeUnitTextEntry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class TickTimeHelper {

    private TickTimeHelper() {}


    public static MutableComponent formatDurationShort(int duration) {

        int ticks = duration % 20;
        int seconds = (duration / 20) % 60;
        int minutes = (duration / 1200 ) % 60;
        int hours = (duration / 72000);
        MutableComponent result = Component.empty();
        if(hours > 0)
            result.append(formatUnitShort(hours,TimeUnit.HOUR));
        if(minutes > 0)
            result.append(formatUnitShort(minutes,TimeUnit.MINUTE));
        if(seconds > 0)
            result.append(formatUnitShort(seconds,TimeUnit.SECOND));
        if(ticks > 0 || result.getString().isBlank())
            result.append(formatUnitShort(ticks,TimeUnit.TICK));
        return result;
    }

    public static MutableComponent formatDurationDisplay(int duration) {

        int ticks = duration % 20;
        int seconds = (duration / 20) % 60;
        int minutes = (duration / 1200 ) % 60;
        int hours = (duration / 72000);
        if(hours > 0)
            return formatUnitShort(hours,TimeUnit.HOUR);
        if(minutes > 0)
            return formatUnitShort(minutes,TimeUnit.MINUTE);
        if(seconds > 0)
            return formatUnitShort(seconds,TimeUnit.SECOND);
        return formatUnitShort(ticks,TimeUnit.TICK);
    }

    public static MutableComponent formatDuration(int duration) {

        int ticks = duration % 20;
        int seconds = (duration / 20) % 60;
        int minutes = (duration / 1200 ) % 60;
        int hours = (duration / 72000);
        MutableComponent result = Component.empty();
        boolean addSpacer = false;
        if(hours > 0)
        {
            appendUnit(result,false,hours,TimeUnit.HOUR);
            addSpacer = true;
        }
        if(minutes > 0)
        {
            appendUnit(result,addSpacer,minutes,TimeUnit.MINUTE);
            addSpacer = true;
        }
        if(seconds > 0)
        {
            appendUnit(result,addSpacer,seconds,TimeUnit.SECOND);
            addSpacer = true;
        }
        if(ticks > 0)
        {
            appendUnit(result,addSpacer,ticks,TimeUnit.TICK);
            //addSpacer = true;
        }
        return result;
    }

    private static void appendUnit(MutableComponent result,boolean addSpacer,int count,TimeUnit unit) { appendUnit(result,addSpacer,count,unit.getUnitText()); }
    private static void appendUnit(MutableComponent result,boolean addSpacer,int count,TimeUnitTextEntry entry)
    {
        if(addSpacer)
            result.append(CommonComponents.SPACE);
        result.append(Component.literal(String.valueOf(count)));
        if(count > 1)
            result.append(entry.pluralText.get());
        else
            result.append(entry.fullText.get());
    }


    private static MutableComponent formatUnitShort(int count,TimeUnit unit) { return formatUnitShort(count,unit.getUnitText()); }
    private static MutableComponent formatUnitShort(int count,TimeUnitTextEntry entry) { return Component.literal(String.valueOf(count)).append(entry.shortText.get()); }


}
