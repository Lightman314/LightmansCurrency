package io.github.lightman314.lightmanscurrency.common.advancements.date;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;

public class DateTrigger extends SimpleCriterionTrigger<DateTrigger.Instance> {

    public static final ResourceLocation ID = new ResourceLocation(LightmansCurrency.MODID, "date_range");
    public static final DateTrigger INSTANCE = new DateTrigger();

    private DateTrigger() {}

    public static AbstractCriterionTriggerInstance ofRange(int startMonth, int startDate, int endMonth, int endDate) { return new Instance(EntityPredicate.Composite.ANY, new DatePredicate(startMonth, startDate), new DatePredicate(endMonth, endDate)); }
    public static AbstractCriterionTriggerInstance ofRange(@Nonnull DatePredicate startDate, @Nonnull DatePredicate endDate) { return new Instance(EntityPredicate.Composite.ANY, startDate, endDate); }

    @Nonnull
    @Override
    protected Instance createInstance(@Nonnull JsonObject json, @Nonnull EntityPredicate.Composite predicate, @Nonnull DeserializationContext context) {
        return new Instance(predicate, DatePredicate.fromJson(json.get("start")), DatePredicate.fromJson(json.get("end")));
    }

    @Nonnull
    @Override
    public ResourceLocation getId() { return ID; }

    public void trigger(@Nonnull ServerPlayer player) { this.trigger(player, Instance::test); }

    protected static class Instance extends AbstractCriterionTriggerInstance
    {

        private final DatePredicate startDate;
        private final DatePredicate endDate;

        public Instance(EntityPredicate.Composite playerPred, DatePredicate startDate, DatePredicate endDate) {
            super(ID, playerPred);
            this.startDate = startDate;
            this.endDate = endDate;
        }

        @Nonnull
        @Override
        public JsonObject serializeToJson(@Nonnull SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.add("start", this.startDate.toJson());
            json.add("end", this.endDate.toJson());
            return json;
        }

        public boolean test() { return DatePredicate.isInRange(this.startDate, this.endDate); }

    }

}
