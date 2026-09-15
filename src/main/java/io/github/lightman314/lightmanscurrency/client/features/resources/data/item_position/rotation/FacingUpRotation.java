package io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.rotation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.MathHelper;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.RotationHandler;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FacingUpRotation extends RotationHandler {

    public static final MapCodec<FacingUpRotation> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.FLOAT.optionalFieldOf("offset",0f).forGetter(r -> r.offset)
    ).apply(builder,FacingUpRotation::create));

    private static final Map<Float,FacingUpRotation> cache = new HashMap<>();

    private final float offset;
    private FacingUpRotation(float offset) {
        this.offset = offset;
    }

    public static FacingUpRotation createDefault() { return create(0f); }
    public static FacingUpRotation create(float offset) {
        if(!cache.containsKey(offset))
            cache.put(offset,new FacingUpRotation(offset));
        return cache.get(offset);
    }

    @Override
    public MapCodec<? extends RotationHandler> getType() { return MAP_CODEC; }

    @Override
    protected List<Quaternionfc> rotate(BlockState state, float partialTicks) {
        if(state.getBlock() instanceof IRotatableBlock rb) {
            int facing = rb.getFacing(state).getOpposite().get2DDataValue();
            return List.of(
                    new Quaternionf().fromAxisAngleDeg(MathHelper.YP,(facing * -90f) + this.offset),
                    new Quaternionf().fromAxisAngleDeg(MathHelper.XP,90)
            );
        }
        return List.of(new Quaternionf().fromAxisAngleDeg(MathHelper.XP,90));
    }

}