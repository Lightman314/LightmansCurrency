package io.github.lightman314.lightmanscurrency.common.event_coins;

import io.github.lightman314.lightmanscurrency.common.advancements.date.DatePredicate;

import javax.annotation.Nonnull;

public class EventRange
{

    private final DatePredicate start;
    private final DatePredicate end;
    private EventRange(@Nonnull DatePredicate start, @Nonnull DatePredicate end) { this.start = start; this.end = end; }

    public static EventRange create(int startMonth, int startDate, int endMonth, int endDate) { return create(new DatePredicate(startMonth, startDate), new DatePredicate(endMonth,endDate)); }
    public static EventRange create(@Nonnull DatePredicate start, @Nonnull DatePredicate end) { return new EventRange(start,end); }

    public boolean isActive() { return DatePredicate.isInRange(this.start, this.end); }

}