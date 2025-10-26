package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.rotation;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.RotationHandler;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.RotationHandlerType;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FacingUpRotation extends RotationHandler {

    public static final RotationHandlerType TYPE = FacingUpRotation::parse;

    private static final Map<Float,FacingUpRotation> cache = new HashMap<>();

    private final float extraRotation;
    protected FacingUpRotation(float extraRotation) {
        super(TYPE);
        this.extraRotation = extraRotation;
    }

    public static FacingUpRotation createDefault() { return create(0f); }
    public static FacingUpRotation create(float extraRotation) {
        if(!cache.containsKey(extraRotation))
            cache.put(extraRotation,new FacingUpRotation(extraRotation));
        return cache.get(extraRotation);
    }

    @Override
    protected List<Quaternionf> rotate(BlockState state, float partialTicks) {
        if(state.getBlock() instanceof IRotatableBlock rb)
        {
            int facing = rb.getFacing(state).get2DDataValue();
            return ImmutableList.of(
                    MathUtil.fromAxisAngleDegree(MathUtil.getYP(), (facing * -90f) + this.extraRotation),
                    MathUtil.fromAxisAngleDegree(MathUtil.getXP(), 90f));
        }
        else
            return ImmutableList.of(MathUtil.fromAxisAngleDegree(MathUtil.getXP(), 90f));
    }

    @Override
    protected void writeAdditional(JsonObject json) {
        if(this.extraRotation != 0f)
            json.addProperty("offset",this.extraRotation);
    }

    private static FacingUpRotation parse(JsonObject json) throws JsonSyntaxException { return create(GsonHelper.getAsFloat(json,"offset",0f)); }

}
