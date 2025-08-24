package io.github.lightman314.lightmanscurrency.common.crafting.durability;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;

import java.util.Optional;

public class DurabilityData {

    public static DurabilityData NULL = new DurabilityData(false,0,0);

    public final boolean allowInfinite;
    private Optional<Boolean> aiOptional() {
        if(this.min == 0)
            return Optional.empty();
        return Optional.of(this.allowInfinite);
    }
    public final int min;
    public final int max;
    private DurabilityData(Optional<Boolean> allowInfinite,int min, int max) { this(allowInfinite.orElse(false),min,max); }
    public DurabilityData(boolean allowInfinite,int min, int max) { this.allowInfinite = allowInfinite || min <= 0; this.min = min; this.max = max; }

    public boolean isValid() { return this.min < this.max && this.max > 0 && this.min >= 0; }

    public boolean test(int durability) { return !this.isValid() || (this.allowInfinite && durability == 0) || (durability >= this.min && durability <= this.max); }

    public Optional<DurabilityData> asOptional() {
        if(!this.isValid())
            return Optional.empty();
        return Optional.of(this);
    }

    private String getFailMessage()
    {
        StringBuilder builder = new StringBuilder();
        if(this.min < 0)
            this.addLine(builder,"min(" + this.min + ") must be greater than or equal to 0!");
        if(this.max <= this.min)
            this.addLine(builder,"max(" + this.max + ") must be greater than " + this.min);
        return builder.toString();
    }

    private void addLine(StringBuilder builder,String line)
    {
        if(!builder.isEmpty())
            builder.append('\n');
        builder.append(line);
    }

    public static Optional<DurabilityData> parse(JsonObject json, String entry) throws JsonSyntaxException { return parse(json,entry,false); }
    public static Optional<DurabilityData> parseValid(JsonObject json, String entry) throws JsonSyntaxException { return parse(json,entry,true); }
    private static Optional<DurabilityData> parse(JsonObject json, String entry, boolean validate) throws JsonSyntaxException
    {
        if(json.has(entry))
        {
            JsonObject e = GsonHelper.getAsJsonObject(json,entry);
            boolean allowInfinite = GsonHelper.getAsBoolean(e,"allowInfinite",false);
            int min = GsonHelper.getAsInt(e,"min");
            if(validate && min < 0)
                throw new JsonSyntaxException("min(" + min + ") must be greater than or equal to 0!");
            int max = GsonHelper.getAsInt(e,"max");
            if(validate && max <= 0)
                throw new JsonSyntaxException("max(" + max + ") must be greater than " + min);
            return Optional.of(new DurabilityData(allowInfinite,min,max));
        }
        return Optional.empty();
    }

    public JsonObject write()
    {
        JsonObject json = new JsonObject();
        if(this.min != 0)
            json.addProperty("allowInfinite",true);
        json.addProperty("min",this.min);
        json.addProperty("max",this.max);
        return json;
    }

    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(this.allowInfinite);
        buffer.writeInt(this.min);
        buffer.writeInt(this.max);
    }

    public static DurabilityData decode(FriendlyByteBuf buffer) { return new DurabilityData(buffer.readBoolean(),buffer.readInt(),buffer.readInt()); }

}