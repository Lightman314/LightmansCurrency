package io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.github.lightman314.lightmanscurrency.api.helpers.MathHelper;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IRotatableBlock;
import net.minecraft.IdentifierException;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ItemPositionData {

    public static final ItemPositionData EMPTY = new ItemPositionData();

    public static final Codec<ItemPositionData> CODEC = ExtraCodecs.JSON.flatXmap(ItemPositionData::parse,d -> DataResult.error(() -> "Cannot encode ItemPositionData to json!"));

    private final List<PositionEntry> entries;

    public ItemPositionData(PositionEntry... entries) { this.entries = ImmutableList.copyOf(entries); }
    public ItemPositionData(List<PositionEntry> entries) { this.entries = ImmutableList.copyOf(entries); }

    private static DataResult<ItemPositionData> parse(JsonElement element) {
        try {
            JsonObject json = GsonHelper.convertToJsonObject(element,"root");
            RotationHandler globalRotation = null;
            float globalScale = GsonHelper.getAsFloat(json,"scale",1f);
            int globalExtraCount = GsonHelper.getAsInt(json,"extra_count",0);
            if(globalExtraCount < 0)
                throw new JsonSyntaxException("extra_count cannot be less than 0!");
            Vector3f globalExtraOffset = new Vector3f(
                    GsonHelper.getAsFloat(json,"offsetX",0f),
                    GsonHelper.getAsFloat(json,"offsetY",0f),
                    GsonHelper.getAsFloat(json,"offsetZ",0f));
            if(json.has("rotation_type"))
                globalRotation = RotationHandler.CODEC.decode(JsonOps.INSTANCE,json.get("rotation_type")).getOrThrow(JsonSyntaxException::new).getFirst();
            int globalMinLight = GsonHelper.getAsInt(json,"min_light",0);

            JsonArray entryList = GsonHelper.getAsJsonArray(json,"entries");
            List<PositionEntry> entries = new ArrayList<>();
            for(int i = 0; i < entryList.size(); ++i) {
                JsonObject entryData = GsonHelper.convertToJsonObject(entryList.get(i),"entries[" + i + "]");
                JsonObject positionData = GsonHelper.getAsJsonObject(entryData,"position");
                Vector3f startPosition = new Vector3f(
                        GsonHelper.getAsFloat(positionData,"x"),
                        GsonHelper.getAsFloat(positionData,"y"),
                        GsonHelper.getAsFloat(positionData,"z"));
                int extraCount = GsonHelper.getAsInt(positionData,"extra_count",globalExtraCount);
                Vector3f extraOffset = new Vector3f();
                if(extraCount != 0)
                {
                    if(extraCount < 0)
                        throw new JsonSyntaxException("extra_count cannot be less than 0!");
                    extraOffset = new Vector3f(
                            GsonHelper.getAsFloat(positionData,"offsetX",globalExtraOffset.x),
                            GsonHelper.getAsFloat(positionData,"offsetY",globalExtraOffset.y),
                            GsonHelper.getAsFloat(positionData,"offsetZ",globalExtraOffset.z));
                    if(extraOffset.x == 0f && extraOffset.y == 0f && extraOffset.z == 0f)
                        throw new JsonSyntaxException("offsetX/Y/Z is not defined or has all values equal to zero!");
                }
                float scale = GsonHelper.getAsFloat(entryData,"scale",globalScale);
                RotationHandler rotationHandler;
                if(entryData.has("rotation_type"))
                    rotationHandler = RotationHandler.CODEC.decode(JsonOps.INSTANCE,entryData.get("rotation_type")).getOrThrow(JsonSyntaxException::new).getFirst();
                else if(globalRotation == null)
                    throw new JsonSyntaxException("Missing rotation_type, expected to find a JsonObject");
                else
                    rotationHandler = globalRotation;
                int minLight = GsonHelper.getAsInt(entryData,"min_light",globalMinLight);
                entries.add(new PositionEntry(startPosition,extraCount,extraOffset,scale,rotationHandler,minLight));
            }
            return DataResult.success(new ItemPositionData(entries));
        } catch (JsonParseException | IdentifierException exception) {
            return DataResult.error(() -> "Error parsing Item Position Data: " + exception.getMessage());
        }
    }

    @Nullable
    PositionEntry getEntry(int index) {
        if(index < 0 || index >= this.entries.size())
            return null;
        return this.entries.get(index);
    }

    public List<Vector3fc> getPositions(BlockState state,int index) {
        PositionEntry entry = this.getEntry(index);
        if(entry == null)
            return new ArrayList<>();
        FacingData facing;
        if(state.getBlock() instanceof IRotatableBlock rb)
            facing = new FacingData(rb.getFacing(state));
        else
            facing = new FacingData();

        List<Vector3fc> results = new ArrayList<>();
        Vector3f currentPos = new Vector3f(entry.position);
        results.add(facing.handle(currentPos));
        for(int i = 0; i < entry.extraCount; ++i) {
            currentPos.add(entry.extraOffset);
            results.add(facing.handle(currentPos));
        }
        return results;
    }

    public List<Quaternionfc> getRotation(BlockState state,int index,float partialTicks) {
        PositionEntry entry = this.getEntry(index);
        if(entry == null)
            return new ArrayList<>();
        return entry.rotationHandler.rotate(state,partialTicks);
    }

    public float getScale(int index) {
        PositionEntry entry = this.getEntry(index);
        return entry == null ? 1f : entry.scale;
    }

    public int getMinLight(int index) {
        PositionEntry entry = this.getEntry(index);
        return entry == null ? 0 : entry.minLight;
    }

    public int getEntryCount() { return this.entries.size(); }
    public boolean isEmpty() { return this.entries.isEmpty(); }

    public record PositionEntry(Vector3f position,int extraCount,Vector3f extraOffset,float scale,RotationHandler rotationHandler,int minLight) {  }

    private static final class FacingData {
        final Vector3fc forward;
        final Vector3fc right;
        final Vector3fc up;
        final Vector3fc offset;
        FacingData() {
            this.forward = new Vector3f(0,0,1);
            this.right = new Vector3f(1,0,0);
            this.up = new Vector3f(0,1,0);
            this.offset = new Vector3f();
        }
        FacingData(Direction facing) {
            this.forward = IRotatableBlock.getFrontVect(facing);
            this.right = IRotatableBlock.getRightVect(facing);
            this.up = new Vector3f(0,1,0);
            this.offset = IRotatableBlock.getOffsetVect(facing);
        }
        Vector3fc handle(Vector3fc position) {
            Vector3fc x = MathHelper.vectorMult(this.right,position.x());
            Vector3fc y = MathHelper.vectorMult(this.up,position.y());
            Vector3fc z = MathHelper.vectorMult(this.forward,position.z());
            return MathHelper.vectorAdd(x,y,z,this.offset);
        }
    }

}