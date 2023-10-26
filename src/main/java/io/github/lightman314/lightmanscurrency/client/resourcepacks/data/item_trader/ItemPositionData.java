package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ItemPositionData {

    public static final ItemPositionData EMPTY = new ItemPositionData(ImmutableList.of());

    private final List<PositionEntry> entries;

    public ItemPositionData(@Nonnull PositionEntry... entries) { this.entries = ImmutableList.copyOf(entries); }
    public ItemPositionData(@Nonnull List<PositionEntry> entries) { this.entries = ImmutableList.copyOf(entries); }

    @Nonnull
    public static ItemPositionData parse(@Nonnull JsonObject json) throws JsonSyntaxException, IllegalArgumentException
    {
        String globalRotation = null;
        float globalScale = GsonHelper.getAsFloat(json, "Scale", Float.NEGATIVE_INFINITY);
        int globalExtraCount = GsonHelper.getAsInt(json, "ExtraCount", 0);
        if(globalExtraCount < 0)
            throw new IllegalArgumentException("ExtraCount cannot be less than 0!");
        Vector3f globalExtraOffset = new Vector3f(
                GsonHelper.getAsFloat(json, "offsetX", 0f),
                GsonHelper.getAsFloat(json, "offsetY", 0f),
                GsonHelper.getAsFloat(json, "offsetZ", 0f));
        if(json.has("RotationType"))
        {
            globalRotation = GsonHelper.getAsString(json, "RotationType");
            if(RotationHandler.getRotationHandler(globalRotation) == null)
                throw new IllegalArgumentException("'" + globalRotation + "' is not a valid RotationType!");
        }


        JsonArray entryList = GsonHelper.getAsJsonArray(json, "Entries");

        List<PositionEntry> entries = new ArrayList<>();
        for(int i = 0; i < entryList.size(); ++i)
        {
            JsonObject entryData = GsonHelper.convertToJsonObject(entryList.get(i), "Entry #" + (i + 1));
            JsonObject positionData = GsonHelper.getAsJsonObject(entryData, "Position");
            Vector3f startPosition = new Vector3f(
                    GsonHelper.getAsFloat(positionData, "x"),
                    GsonHelper.getAsFloat(positionData, "y"),
                    GsonHelper.getAsFloat(positionData, "z")
            );
            int extraCount = GsonHelper.getAsInt(positionData, "ExtraCount", globalExtraCount);
            Vector3f extraOffset = new Vector3f();
            if(extraCount != 0)
            {
                if(extraCount < 0)
                    throw new IllegalArgumentException("ExtraCount cannot be less than 0!");
                extraOffset = new Vector3f(
                        GsonHelper.getAsFloat(positionData, "offsetX", globalExtraOffset.x()),
                        GsonHelper.getAsFloat(positionData, "offsetY", globalExtraOffset.y()),
                        GsonHelper.getAsFloat(positionData, "offsetZ", globalExtraOffset.z())
                );
                if(extraOffset.x() == 0f && extraOffset.y() == 0f && extraOffset.z() == 0f)
                    throw new IllegalArgumentException("offsetX/Y/Z is not defined or has all values equal zero!");
            }
            float scale;
            if(globalScale != Float.NEGATIVE_INFINITY)
                scale = GsonHelper.getAsFloat(entryData,"Scale", globalScale);
            else
                scale = GsonHelper.getAsFloat(entryData, "Scale", 1f);
            String rotationType;
            if(globalRotation != null)
                rotationType = GsonHelper.getAsString(entryData, "RotationType", globalRotation);
            else
                rotationType = GsonHelper.getAsString(entryData, "RotationType");
            RotationHandler rotationHandler = RotationHandler.getRotationHandler(rotationType);
            if(rotationHandler == null)
                throw new IllegalArgumentException("'" + rotationType + "' is not a valid RotationType!");
            entries.add(new PositionEntry(startPosition, extraCount, extraOffset, scale, rotationHandler));
        }
        return new ItemPositionData(entries);
    }

    @Nullable
    private PositionEntry safeGetEntry(int index) { if(index < 0 || index >= this.entries.size()) return null; return this.entries.get(index); }

    @Nonnull
    public List<Vector3f> getPositions(BlockState state, int index)
    {
        PositionEntry entry = this.safeGetEntry(index);
        if(entry == null)
            return new ArrayList<>();

        FacingData facing;
        if(state.getBlock() instanceof IRotatableBlock rb)
            facing = new FacingData(rb.getFacing(state));
        else
            facing = new FacingData();

        List<Vector3f> results = new ArrayList<>();
        Vector3f currentPos = entry.position.copy();

        results.add(facing.handle(currentPos));
        for(int i = 0; i < entry.extraCount; ++i)
        {
            currentPos.add(entry.extraOffset);
            results.add(facing.handle(currentPos));
        }
        return results;
    }

    @Nonnull
    public List<Quaternion> getRotation(BlockState state, int index, float partialTicks)
    {
        PositionEntry entry = this.safeGetEntry(index);
        if(entry == null)
            return ImmutableList.of();
        return entry.rotationHandler.rotate(state, partialTicks);
    }

    public float getScale(int index)
    {
        PositionEntry entry = this.safeGetEntry(index);
        if(entry == null)
            return 1f;
        return entry.scale;
    }

    public int getEntryCount() { return this.entries.size(); }
    public boolean isEmpty() { return this.getEntryCount() == 0; }

    public record PositionEntry(Vector3f position, int extraCount, Vector3f extraOffset, float scale, RotationHandler rotationHandler) {}

    private static final class FacingData
    {
        final Vector3f forward;
        final Vector3f right;
        final Vector3f up;
        final Vector3f offset;
        FacingData() {
            this.forward = MathUtil.getZP();
            this.right = MathUtil.getXP();
            this.up = MathUtil.getYP();
            this.offset = new Vector3f();
        }
        FacingData(Direction facing) {
            this.forward = IRotatableBlock.getForwardVect(facing);
            this.right = IRotatableBlock.getRightVect(facing);
            this.up = MathUtil.getYP();
            this.offset = IRotatableBlock.getOffsetVect(facing);
        }
        Vector3f handle(Vector3f position)
        {
            Vector3f x = MathUtil.VectorMult(this.right, position.x());
            Vector3f y = MathUtil.VectorMult(this.up, position.y());
            Vector3f z = MathUtil.VectorMult(this.forward, position.z());
            return MathUtil.VectorAdd(x,y,z,this.offset);
        }
    }

}