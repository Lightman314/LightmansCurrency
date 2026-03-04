package io.github.lightman314.lightmanscurrency.api.stats;

public class StatKey<A,B> {

    public final String key;
    public final StatType<A,B> type;

    private StatKey(String key, StatType<A,B> type)
    {
        this.key = key;
        this.type = type;
    }

    public static <A,B> StatKey<A,B> create(String key, StatType<A,B> type) { return new StatKey<>(key,type); }

}
