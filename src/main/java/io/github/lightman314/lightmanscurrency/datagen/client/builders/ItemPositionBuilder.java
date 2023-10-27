package io.github.lightman314.lightmanscurrency.datagen.client.builders;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.math.Vector3f;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public final class ItemPositionBuilder {


    private boolean hasGlobalScale = false;
    private float globalScale = 0f;
    private String globalRotationType = null;
    private int globalExtraCount = 0;
    private Vector3f globalExtraOffset = null;
    private final List<PositionEntryBuilder> entries = new ArrayList<>();

    private ItemPositionBuilder() {}

    public static ItemPositionBuilder builder() { return new ItemPositionBuilder(); }

    public ItemPositionBuilder withGlobalRotationType(@Nonnull String rotationType) { this.globalRotationType = rotationType; return this; }
    public ItemPositionBuilder withGlobalScale(float globalScale) { this.hasGlobalScale = true; this.globalScale = globalScale; return this; }
    public ItemPositionBuilder withGlobalExtraCount(int extraCount) { this.globalExtraCount = extraCount; return this; }
    public ItemPositionBuilder withGlobalExtraOffset(@Nonnull Vector3f extraOffset) { this.globalExtraOffset = extraOffset; return this; }

    public ItemPositionBuilder withEntry(@Nonnull Vector3f position) { this.entries.add(new PositionEntryBuilder(position, 0, null, false, 0f, null)); return this; }
    public ItemPositionBuilder withEntry(@Nonnull Vector3f position, float scale) { this.entries.add(new PositionEntryBuilder(position, 0, null, true, scale, null)); return this; }
    public ItemPositionBuilder withEntry(@Nonnull Vector3f position, @Nonnull String rotationType) { this.entries.add(new PositionEntryBuilder(position, 0, null, false, 0f, rotationType)); return this; }
    public ItemPositionBuilder withEntry(@Nonnull Vector3f position, float scale, @Nonnull String rotationType) { this.entries.add(new PositionEntryBuilder(position, 0, null, true, scale, rotationType)); return this; }
    public ItemPositionBuilder withEntry(@Nonnull Vector3f position, int extraCount, @Nonnull Vector3f extraOffset) { this.entries.add(new PositionEntryBuilder(position, extraCount, extraOffset, false, 0f, null)); return this; }
    public ItemPositionBuilder withEntry(@Nonnull Vector3f position, int extraCount, @Nonnull Vector3f extraOffset, float scale) { this.entries.add(new PositionEntryBuilder(position, extraCount, extraOffset, true, scale, null)); return this; }
    public ItemPositionBuilder withEntry(@Nonnull Vector3f position, int extraCount, @Nonnull Vector3f extraOffset, @Nonnull String rotationType) { this.entries.add(new PositionEntryBuilder(position, extraCount, extraOffset, false, 0f, rotationType)); return this; }
    public ItemPositionBuilder withEntry(@Nonnull Vector3f position, int extraCount, @Nonnull Vector3f extraOffset, float scale, @Nonnull String rotationType) { this.entries.add(new PositionEntryBuilder(position, extraCount, extraOffset, true, scale, rotationType)); return this; }

    @Nonnull
    public JsonObject write()
    {
        JsonObject json = new JsonObject();
        if(this.hasGlobalScale)
            json.addProperty("Scale", this.globalScale);
        if(this.globalRotationType != null)
            json.addProperty("RotationType", this.globalRotationType);
        if(this.globalExtraCount > 0)
            json.addProperty("ExtraCount", this.globalExtraCount);
        if(this.globalExtraOffset != null)
        {
            json.addProperty("offsetX", this.globalExtraOffset.x());
            json.addProperty("offsetY", this.globalExtraOffset.y());
            json.addProperty("offsetZ", this.globalExtraOffset.z());
        }
        JsonArray entryList = new JsonArray();
        for(PositionEntryBuilder entry : this.entries)
        {
            JsonObject entryData = new JsonObject();
            JsonObject positionData = new JsonObject();
            positionData.addProperty("x", entry.position.x());
            positionData.addProperty("y", entry.position.y());
            positionData.addProperty("z", entry.position.z());
            if(entry.extraCount > 0)
            {
                positionData.addProperty("ExtraCount", entry.extraCount);
                positionData.addProperty("offsetX", entry.extraOffset.x());
                positionData.addProperty("offsetY", entry.extraOffset.y());
                positionData.addProperty("offsetZ", entry.extraOffset.z());
            }
            entryData.add("Position", positionData);
            if(entry.hasCustomScale)
                entryData.addProperty("Scale", entry.scale);
            if(entry.rotationType != null)
                entryData.addProperty("RotationType", entry.rotationType);
            entryList.add(entryData);
        }
        json.add("Entries", entryList);
        return json;
    }

    private record PositionEntryBuilder(Vector3f position, int extraCount, Vector3f extraOffset, boolean hasCustomScale, float scale, String rotationType) {}

}