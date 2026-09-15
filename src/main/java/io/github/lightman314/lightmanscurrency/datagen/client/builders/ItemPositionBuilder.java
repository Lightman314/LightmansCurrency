package io.github.lightman314.lightmanscurrency.datagen.client.builders;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.RotationHandler;
import org.joml.Vector3f;
import org.joml.Vector3fc;


import java.util.ArrayList;
import java.util.List;

public final class ItemPositionBuilder {

    private boolean hasGlobalScale = false;
    private float globalScale = 0f;
    private RotationHandler globalRotationType = null;
    private int globalExtraCount = 0;
    private Vector3fc globalExtraOffset = null;
    private int globalMinLight = 0;
    private final List<PositionEntryBuilder> entries = new ArrayList<>();

    private ItemPositionBuilder() {}

    public static ItemPositionBuilder builder() { return new ItemPositionBuilder(); }

    public ItemPositionBuilder withGlobalRotationType(RotationHandler rotationType) { this.globalRotationType = rotationType; return this; }
    public ItemPositionBuilder withGlobalScale(float globalScale) { this.hasGlobalScale = true; this.globalScale = globalScale; return this; }
    public ItemPositionBuilder withGlobalExtraCount(int extraCount) { this.globalExtraCount = extraCount; return this; }
    public ItemPositionBuilder withGlobalExtraOffset(float x,float y,float z) { return this.withGlobalExtraOffset(new Vector3f(x,y,z)); }
    public ItemPositionBuilder withGlobalExtraOffset(Vector3fc extraOffset) { this.globalExtraOffset = extraOffset; return this; }
    public ItemPositionBuilder withGlobalMinLight(int minLight) { this.globalMinLight = minLight; return this; }

    public PositionEntryBuilder withEntry(float x,float y, float z) { return this.withEntry(new Vector3f(x,y,z)); }
    public PositionEntryBuilder withEntry(Vector3fc position) {
        PositionEntryBuilder b = new PositionEntryBuilder(this,position);
        this.entries.add(b);
        return b;
    }

    public ItemPositionBuilder withSimpleEntry(float x,float y,float z) { return this.withSimpleEntry(new Vector3f(x,y,z)); }
    public ItemPositionBuilder withSimpleEntry(Vector3fc position) { return this.withEntry(position).back(); }

    public JsonObject write()
    {
        JsonObject json = new JsonObject();
        if(this.hasGlobalScale)
            json.addProperty("scale", this.globalScale);
        if(this.globalRotationType != null)
            json.add("rotation_type",RotationHandler.CODEC.encodeStart(JsonOps.INSTANCE,this.globalRotationType).getOrThrow());
        if(this.globalExtraCount > 0)
            json.addProperty("extra_count", this.globalExtraCount);
        if(this.globalExtraOffset != null)
        {
            json.addProperty("offsetX", this.globalExtraOffset.x());
            json.addProperty("offsetY", this.globalExtraOffset.y());
            json.addProperty("offsetZ", this.globalExtraOffset.z());
        }
        if(this.globalMinLight > 0)
            json.addProperty("min_light",this.globalMinLight);
        JsonArray entryList = new JsonArray();
        for(PositionEntryBuilder entry : this.entries)
        {
            JsonObject entryData = new JsonObject();
            JsonObject positionData = new JsonObject();
            positionData.addProperty("x", entry.position.x());
            positionData.addProperty("y", entry.position.y());
            positionData.addProperty("z", entry.position.z());
            if(entry.extraCount > 0)
                positionData.addProperty("extra_count", entry.extraCount);
            if(entry.extraOffset != null) {
                positionData.addProperty("offsetX", entry.extraOffset.x());
                positionData.addProperty("offsetY", entry.extraOffset.y());
                positionData.addProperty("offsetZ", entry.extraOffset.z());
            }
            entryData.add("position", positionData);
            if(entry.scale > 0f)
                entryData.addProperty("scale", entry.scale);
            if(entry.minLight >= 0)
                entryData.addProperty("min_light",entry.minLight);
            if(entry.rotationType != null)
                entryData.add("rotation_type",RotationHandler.CODEC.encodeStart(JsonOps.INSTANCE,entry.rotationType).getOrThrow());
            entryList.add(entryData);
        }
        json.add("entries", entryList);
        return json;
    }

    //private record PositionEntryBuilder(Vector3f position, int extraCount, Vector3f extraOffset, boolean hasCustomScale, float scale, String rotationType) {}

    public static class PositionEntryBuilder
    {

        private final ItemPositionBuilder parent;

        private final Vector3fc position;
        private int extraCount = -1;
        private Vector3fc extraOffset = null;
        private float scale = -1f;
        private int minLight = -1;
        RotationHandler rotationType = null;

        private PositionEntryBuilder(ItemPositionBuilder parent,Vector3fc position) { this.parent = parent; this.position = position; }

        public PositionEntryBuilder withExtraCount(int extraCount) { this.extraCount = extraCount; return this; }
        public PositionEntryBuilder withExtraOffset(Vector3fc extraOffset) { this.extraOffset = extraOffset; return this; }
        public PositionEntryBuilder withScale(float scale) { this.scale = scale; return this; }
        public PositionEntryBuilder withMinLight(int minLight) { this.minLight = minLight; return this; }
        public PositionEntryBuilder withRotationType(RotationHandler rotationType) { this.rotationType = rotationType; return this; }

        public ItemPositionBuilder back() { return this.parent; }

    }

}