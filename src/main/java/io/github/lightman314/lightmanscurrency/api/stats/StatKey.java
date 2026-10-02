package io.github.lightman314.lightmanscurrency.api.stats;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.TooltipHelper;
import io.github.lightman314.lightmanscurrency.api.money.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.stats.builtin.*;
import io.github.lightman314.lightmanscurrency.api.stats.interfaces.StatViewer;
import net.minecraft.locale.Language;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record StatKey<V,A>(Identifier key,StatType<V,A> type) {

    public static MapCodec<StatKey<?,?>> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Identifier.CODEC.fieldOf("key").forGetter(StatKey::key),
            LCRegistries.Data.STAT_TYPE.byNameCodec().fieldOf("type").forGetter(StatKey::type)
    ).apply(builder,StatKey::new));
    public static final StreamCodec<RegistryFriendlyByteBuf,StatKey<?,?>> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,StatKey::key,
            ByteBufCodecs.registry(LCRegistries.Data.STAT_TYPE_KEY),StatKey::type,
            StatKey::new);

    public static final Comparator<StatKey<?,?>> SORTER = Comparator.comparingInt(StatKey::getSortingOrder);

    private static final Map<Identifier,Integer> sortingPriorities = new HashMap<>();
    public static <V,A> StatKey<V,A> registerSortingOrder(StatKey<V,A> key,int sortingOrder) { sortingPriorities.put(key.key,sortingOrder); return key; }
    public int getSortingOrder() { return sortingPriorities.getOrDefault(this.key,0); }

    public static String getDescriptionID(Identifier key) { return Util.makeDescriptionId("lightmanscurrency.stat",key); }
    public static String getTooltipID(Identifier key) { return Util.makeDescriptionId("lightmanscurrency.stat",key) + ".tooltip"; }
    public Component getName() { return Component.translatable(getDescriptionID(this.key)); }
    public Component getLabel() { return StatViewer.GUI_STAT_LABEL.get(this.getName()); }
    @Nullable
    public List<Component> getTooltip() {
        String tooltipID = getTooltipID(this.key);
        if(Language.getInstance().has(tooltipID))
            return TooltipHelper.splitTooltips(Component.translatable(tooltipID));
        return null;
    }

    public boolean isValidValue(Object value) { return this.type.isValidValue(value); }

    public static StatKey<Integer,Integer> createInt(Identifier key) { return new StatKey<>(key,IntegerStatType.TYPE); }
    public static StatKey<Integer,Integer> createInt(Identifier key,int order) { return registerSortingOrder(createInt(key),order); }
    public static StatKey<MoneyView,MoneyValue> createMoney(Identifier key) { return new StatKey<>(key,MoneyStatType.TYPE); }
    public static StatKey<MoneyView,MoneyValue> createMoney(Identifier key,int order) { return registerSortingOrder(createMoney(key),order); }
    public static StatKey<Long,Long> createTimestamp(Identifier key) { return new StatKey<>(key,TimestampStatType.TYPE); }
    public static StatKey<Long,Long> createTimestamp(Identifier key,int order) { return registerSortingOrder(createTimestamp(key),order); }

}