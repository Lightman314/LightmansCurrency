package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.rotation;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.RotationHandler;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.RotationHandlerType;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

import java.util.List;

public class FacingRotation extends RotationHandler {

    public static final RotationHandlerType TYPE = json -> getInstance();
    private static final FacingRotation INSTANCE = new FacingRotation();

    public static FacingRotation getInstance() { return INSTANCE; }

    private FacingRotation() { super(TYPE); }

    @Override
    protected List<Quaternionf> rotate(BlockState state, float partialTicks) {
        if (state.getBlock() instanceof IRotatableBlock rb) {
            int facing = rb.getFacing(state).get2DDataValue();
            return ImmutableList.of(MathUtil.fromAxisAngleDegree(MathUtil.getYP(), facing * -90f));
        }
        return ImmutableList.of();
    }

    @Override
    protected void writeAdditional(JsonObject json) { }

}