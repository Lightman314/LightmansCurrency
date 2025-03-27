package io.github.lightman314.lightmanscurrency.common.advancements.date;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.ResourceLocationException;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nonnull;
import java.time.LocalDate;

public final class DatePredicate {

    public final int month;
    public final int date;

    public DatePredicate(int month, int date) {
        this.month = month;
        if(this.month < 1 || this.month > 12)
            throw new IllegalArgumentException("Month must be between 1-12!");
        this.date = date;
        if(this.date < 1 || this.date > 31)
            throw new IllegalArgumentException("Day must be between 1-31!");
    }

    public JsonObject toJson()
    {
        JsonObject json = new JsonObject();
        json.addProperty("month", this.month);
        json.addProperty("day", this.date);
        return json;
    }

    public static DatePredicate fromJson(@Nonnull JsonElement element) throws JsonSyntaxException, ResourceLocationException {
        JsonObject json = GsonHelper.convertToJsonObject(element, "date data");
        return new DatePredicate(GsonHelper.getAsInt(json, "month"), GsonHelper.getAsInt(json, "day"));
    }

    private boolean isAfter(@Nonnull DatePredicate start) {
        if(this.month > start.month)
            return true;
        if(this.month == start.month)
            return this.date >= start.date;
        return false;
    }

    public boolean isAfter(@Nonnull LocalDate date) {
        if(this.month > date.getMonthValue())
            return true;
        if(this.month == date.getMonthValue())
            return this.date >= date.getDayOfMonth();
        return false;
    }

    public boolean isBefore(@Nonnull LocalDate date) {
        if(this.month < date.getMonthValue())
            return true;
        if(this.month == date.getMonthValue())
            return this.date <= date.getDayOfMonth();
        return false;
    }

    public static boolean isInRange(@Nonnull DatePredicate start, @Nonnull DatePredicate end) { return isInRange(LocalDate.now(), start, end); }
    public static boolean isInRange(@Nonnull LocalDate date, @Nonnull DatePredicate start, @Nonnull DatePredicate end)
    {
        if(end.isAfter(start))
        {
            //If no year overlap, require both to match
            return start.isBefore(date) && end.isAfter(date);
        }
        else
        {
            //If the end is the next year (i.e. start = 12/31 and end is 2/5, only require one match)
            return start.isBefore(date) || end.isAfter(date);
        }
    }

}
