package io.github.lightman314.lightmanscurrency.datagen.client.builders;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.joml.Vector3f;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ItemPositionBuilder {

    private boolean hasGlobalScale = false;
    private float globalScale = 0f;
    private String globalRotationType = null;
    private int globalExtraCount = 0;
    private Vector3f globalExtraOffset = null;
    private int globalMinLight = 0;
    private final List<PositionEntryBuilder> entries = new ArrayList<>();

    private ItemPositionBuilder() {}

    public static ItemPositionBuilder builder() { return new ItemPositionBuilder(); }

    public ItemPositionBuilder withGlobalRotationType(String rotationType) { this.globalRotationType = rotationType; return this; }
    public ItemPositionBuilder withGlobalScale(float globalScale) { this.hasGlobalScale = true; this.globalScale = globalScale; return this; }
    public ItemPositionBuilder withGlobalExtraCount(int extraCount) { this.globalExtraCount = extraCount; return this; }
    public ItemPositionBuilder withGlobalExtraOffset(Vector3f extraOffset) { this.globalExtraOffset = extraOffset; return this; }
    public ItemPositionBuilder withGlobalMinLight(int minLight) { this.globalMinLight = minLight; return this; }

    public PositionEntryBuilder withEntry(Vector3f position) {
        PositionEntryBuilder b = new PositionEntryBuilder(this,position);
        this.entries.add(b);
        return b;
    }

    public ItemPositionBuilder withSimpleEntry(Vector3f position) { return this.withEntry(position).back(); }

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
            json.addProperty("offsetX", this.globalExtraOffset.x);
            json.addProperty("offsetY", this.globalExtraOffset.y);
            json.addProperty("offsetZ", this.globalExtraOffset.z);
        }
        if(this.globalMinLight > 0)
            json.addProperty("MinLight",this.globalMinLight);
        JsonArray entryList = new JsonArray();
        for(PositionEntryBuilder entry : this.entries)
        {
            JsonObject entryData = new JsonObject();
            JsonObject positionData = new JsonObject();
            positionData.addProperty("x", entry.position.x);
            positionData.addProperty("y", entry.position.y);
            positionData.addProperty("z", entry.position.z);
            if(entry.extraCount > 0)
            {
                positionData.addProperty("ExtraCount", entry.extraCount);
                positionData.addProperty("offsetX", entry.extraOffset.x);
                positionData.addProperty("offsetY", entry.extraOffset.y);
                positionData.addProperty("offsetZ", entry.extraOffset.z);
            }
            entryData.add("Position", positionData);
            if(entry.scale > 0f)
                entryData.addProperty("Scale", entry.scale);
            if(entry.minLight >= 0)
                entryData.addProperty("MinLight",entry.minLight);
            if(entry.rotationType != null)
                entryData.addProperty("RotationType", entry.rotationType);
            entryList.add(entryData);
        }
        json.add("Entries", entryList);
        return json;
    }

    //private record PositionEntryBuilder(Vector3f position, int extraCount, Vector3f extraOffset, boolean hasCustomScale, float scale, String rotationType) {}

    public static class PositionEntryBuilder
    {

        private final ItemPositionBuilder parent;

        private final Vector3f position;
        private int extraCount = -1;
        private Vector3f extraOffset = null;
        private float scale = -1f;
        private int minLight = -1;
        String rotationType = null;

        private PositionEntryBuilder(ItemPositionBuilder parent,Vector3f position) { this.parent = parent; this.position = position; }

        public PositionEntryBuilder withExtraCount(int extraCount) { this.extraCount = extraCount; return this; }
        public PositionEntryBuilder withExtraOffset(Vector3f extraOffset) { this.extraOffset = extraOffset; return this; }
        public PositionEntryBuilder withScale(float scale) { this.scale = scale; return this; }
        public PositionEntryBuilder withMinLight(int minLight) { this.minLight = minLight; return this; }
        public PositionEntryBuilder withRotationType(String rotationType) { this.rotationType = rotationType; return this; }

        public ItemPositionBuilder back() { return this.parent; }

    }

}