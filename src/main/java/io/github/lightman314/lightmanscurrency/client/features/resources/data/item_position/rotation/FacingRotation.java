package io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.rotation;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.helpers.MathHelper;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.RotationHandler;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

import java.util.List;

public class FacingRotation extends RotationHandler {

    public static final FacingRotation INSTANCE = new FacingRotation();
    public static final MapCodec<FacingRotation> MAP_CODEC = MapCodec.unit(INSTANCE);

    private FacingRotation() {}

    @Override
    public MapCodec<? extends RotationHandler> getType() { return MAP_CODEC; }

    @Override
    protected List<Quaternionfc> rotate(BlockState state, float partialTicks) {
        if(state.getBlock() instanceof IRotatableBlock rb) {
            int facing = rb.getFacing(state).get2DDataValue();
            return List.of(new Quaternionf().fromAxisAngleDeg(MathHelper.YP,facing * 90f));
        }
        return List.of();
    }
}
