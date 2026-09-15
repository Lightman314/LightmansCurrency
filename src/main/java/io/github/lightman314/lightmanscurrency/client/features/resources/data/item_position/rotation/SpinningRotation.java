package io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.rotation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.RotationHandler;
import io.github.lightman314.lightmanscurrency.client.features.trader.item.block_entity.ItemTraderBlockEntityRenderer;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionfc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpinningRotation extends RotationHandler {

    private static final Map<Float,SpinningRotation> cache = new HashMap<>();

    public static final MapCodec<SpinningRotation> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.FLOAT.optionalFieldOf("speed",1f).forGetter(r -> r.speed)
    ).apply(builder,SpinningRotation::create));

    private final float speed;
    protected SpinningRotation(float speed) { this.speed = speed; }

    public static SpinningRotation createDefault() { return create(2f); }
    public static SpinningRotation create(float speed) {
        if(!cache.containsKey(speed))
            cache.put(speed,new SpinningRotation(speed));
        return cache.get(speed);
    }

    @Override
    public MapCodec<? extends RotationHandler> getType() { return MAP_CODEC; }

    @Override
    protected List<Quaternionfc> rotate(BlockState state, float partialTicks) {
        return List.of(ItemTraderBlockEntityRenderer.getRotation(partialTicks,this.speed));
    }

}