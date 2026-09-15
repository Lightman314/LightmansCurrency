package io.github.lightman314.lightmanscurrency.api.helpers.time;

import io.github.lightman314.lightmanscurrency.api.text.TimeUnitTextEntry;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public enum TimeUnit {
    SECOND(TimeHelper.DURATION_SECOND),MINUTE(TimeHelper.DURATION_MINUTE),HOUR(TimeHelper.DURATION_HOUR),DAY(TimeHelper.DURATION_DAY);

    public static final TimeUnitTextEntry MILLISECOND = TimeUnitTextEntry.of("millisecond");
    public static final TimeUnitTextEntry TICK = TimeUnitTextEntry.of("tick");

    public static final Map<TimeUnit,TimeUnitTextEntry> ENTRIES = Map.of(
            SECOND,TimeUnitTextEntry.of("second"),
            MINUTE,TimeUnitTextEntry.of("minute"),
            HOUR,TimeUnitTextEntry.of("hour"),
            DAY,TimeUnitTextEntry.of("day"));

    public static final List<TimeUnit> UNITS_SMALL_TO_LARGE = List.of(SECOND,MINUTE,HOUR,DAY);
    public static final List<TimeUnit> UNITS_LARGE_TO_SMALL = List.of(DAY,HOUR,MINUTE,SECOND);

    public static List<TimeUnit> rangeLargeToSmall(TimeUnit unit1,TimeUnit unit2) {
        if(unit1 == unit2)
            return List.of(unit1);
        TimeUnit smallest;
        TimeUnit largest;
        if(unit1.millis < unit2.millis) {
            smallest = unit1;
            largest = unit2;
        }
        else {
            smallest = unit2;
            largest = unit1;
        }
        if(largest == DAY && smallest == SECOND)
            return UNITS_LARGE_TO_SMALL;
        List<TimeUnit> result = new ArrayList<>();
        boolean collect = false;
        for(TimeUnit unit : UNITS_LARGE_TO_SMALL) {
            if(unit == largest)
                collect = true;
            if(collect)
                result.add(unit);
            if(unit == smallest)
                collect = false;
        }
        return Collections.unmodifiableList(result);
    }

    public static List<TimeUnit> rangeSmallToLarge(TimeUnit unit1,TimeUnit unit2) {
        if(unit1 == unit2)
            return List.of(unit1);
        TimeUnit smallest;
        TimeUnit largest;
        if(unit1.millis < unit2.millis) {
            smallest = unit1;
            largest = unit2;
        }
        else {
            smallest = unit2;
            largest = unit1;
        }
        if(largest == DAY && smallest == SECOND)
            return UNITS_SMALL_TO_LARGE;
        List<TimeUnit> result = new ArrayList<>();
        boolean collect = false;
        for(TimeUnit unit : UNITS_SMALL_TO_LARGE) {
            if(unit == smallest)
                collect = true;
            if(collect)
                result.add(unit);
            if(unit == largest)
                collect = false;
        }
        return Collections.unmodifiableList(result);
    }

    private final long millis;
    public long getDurationMillis() { return this.millis; }
    TimeUnit(long millis) { this.millis = millis; }

    public MutableComponent getText() { return ENTRIES.get(this).fullText.get(); }
    public MutableComponent getPluralText() { return ENTRIES.get(this).pluralText.get(); }
    public MutableComponent getShortText() { return ENTRIES.get(this).shortText.get(); }

}
