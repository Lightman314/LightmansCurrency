package io.github.lightman314.lightmanscurrency.api.stats;

import javax.annotation.Nonnull;

public class StatKey<A,B> {

    public final String key;
    public final StatType<A,B> type;

    private StatKey(@Nonnull String key, @Nonnull StatType<A,B> type)
    {
        this.key = key;
        this.type = type;
    }

    public static <A,B> StatKey<A,B> create(@Nonnull String key, @Nonnull StatType<A,B> type) { return new StatKey<>(key,type); }

}
