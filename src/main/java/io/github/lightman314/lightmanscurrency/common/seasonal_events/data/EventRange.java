package io.github.lightman314.lightmanscurrency.common.seasonal_events.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.common.advancements.date.DatePredicate;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.util.GsonHelper;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EventRange
{

    private final DatePredicate start;
    private final DatePredicate end;
    private EventRange(DatePredicate start, DatePredicate end) { this.start = start; this.end = end; }

    public static EventRange create(int startMonth, int startDate, int endMonth, int endDate) { return create(new DatePredicate(startMonth, startDate), new DatePredicate(endMonth,endDate)); }
    public static EventRange create(DatePredicate start, DatePredicate end) { return new EventRange(start,end); }

    public boolean isActive() { return DatePredicate.isInRange(this.start, this.end); }

    public JsonObject toJson()
    {
        JsonObject json = new JsonObject();
        json.add("start",this.start.toJson());
        json.add("end",this.end.toJson());
        return json;
    }

    public static EventRange fromJson(JsonObject json) throws JsonSyntaxException, ResourceLocationException
    {
        DatePredicate start = DatePredicate.fromJson(GsonHelper.getAsJsonObject(json,"start"));
        DatePredicate end = DatePredicate.fromJson(GsonHelper.getAsJsonObject(json,"end"));
        return create(start,end);
    }

}
