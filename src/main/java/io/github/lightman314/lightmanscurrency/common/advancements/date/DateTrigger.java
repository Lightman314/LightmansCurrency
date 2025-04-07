package io.github.lightman314.lightmanscurrency.common.advancements.date;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;
import java.util.Optional;

public class DateTrigger extends SimpleCriterionTrigger<DateTrigger.Instance> {

    public static final ResourceLocation ID = VersionUtil.lcResource("date_range");
    public static final DateTrigger INSTANCE = new DateTrigger();
    private static final Codec<DateTrigger.Instance> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
                    DatePredicate.CODEC.fieldOf("start").forGetter(i -> i.startDate),
                    DatePredicate.CODEC.fieldOf("end").forGetter(i -> i.endDate)
            ).apply(builder,Instance::new));

    private DateTrigger() {}

    public static Criterion<DateTrigger.Instance> ofRange(int startMonth, int startDate, int endMonth, int endDate) { return INSTANCE.createCriterion(new Instance(Optional.empty(), new DatePredicate(startMonth, startDate), new DatePredicate(endMonth, endDate))); }
    public static Criterion<DateTrigger.Instance> ofRange(@Nonnull DatePredicate startDate, @Nonnull DatePredicate endDate) { return INSTANCE.createCriterion(new Instance(Optional.empty(), startDate, endDate)); }

    @Nonnull
    @Override
    public Codec<Instance> codec() { return CODEC; }
    public void trigger(@Nonnull ServerPlayer player) { this.trigger(player, Instance::test); }

    public static class Instance implements SimpleCriterionTrigger.SimpleInstance {

        private final Optional<ContextAwarePredicate> player;
        private final DatePredicate startDate;
        private final DatePredicate endDate;

        public Instance(Optional<ContextAwarePredicate> player, DatePredicate startDate, DatePredicate endDate) {
            this.player = player;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public boolean test() { return DatePredicate.isInRange(this.startDate, this.endDate); }

        @Override
        public void validate(@Nonnull CriterionValidator validator) { }

        @Nonnull
        @Override
        public Optional<ContextAwarePredicate> player() { return this.player; }

    }

}
