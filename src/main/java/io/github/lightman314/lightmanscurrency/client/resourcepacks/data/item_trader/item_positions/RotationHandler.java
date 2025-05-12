package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.ItemTraderBlockEntityRenderer;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public abstract class RotationHandler
{

    public static final String SPINNING = "SPINNING";
    public static final String FACING = "FACING";
    public static final String FACING_UP = "FACING_UP";

    private static final Map<String,RotationHandler> ROTATION_HANDLERS;
    @Nullable
    public static RotationHandler getRotationHandler(@Nonnull String type) { return ROTATION_HANDLERS.get(type); }
    public static String getRotationType(@Nonnull RotationHandler handler) {
        AtomicReference<String> result = new AtomicReference<>("SPINNING");
        ROTATION_HANDLERS.forEach((key,entry) -> {
            if(entry == handler)
                result.set(key);
        });
        return result.get();
    }

    @Nonnull
    protected abstract List<Quaternionf> rotate(@Nonnull BlockState state, float partialTicks);

    static {
        ROTATION_HANDLERS = new HashMap<>();
        ROTATION_HANDLERS.put(SPINNING, new RotationHandler() {
            @Nonnull
            @Override
            protected List<Quaternionf> rotate(@Nonnull BlockState state, float partialTicks) {
                return ImmutableList.of(ItemTraderBlockEntityRenderer.getRotation(partialTicks));
            }
        });
        ROTATION_HANDLERS.put(FACING, new RotationHandler() {
            @Nonnull
            @Override
            protected List<Quaternionf> rotate(@Nonnull BlockState state, float partialTicks) {
                if (state.getBlock() instanceof IRotatableBlock rb) {
                    int facing = rb.getFacing(state).get2DDataValue();
                    return ImmutableList.of(MathUtil.fromAxisAngleDegree(MathUtil.getYP(), facing * -90f));
                }
                return ImmutableList.of();
            }
        });
        ROTATION_HANDLERS.put(FACING_UP, new RotationHandler() {
            @Nonnull
            @Override
            protected List<Quaternionf> rotate(@Nonnull BlockState state, float partialTicks) {
                if(state.getBlock() instanceof IRotatableBlock rb)
                {
                    int facing = rb.getFacing(state).get2DDataValue();
                    return ImmutableList.of(
                            MathUtil.fromAxisAngleDegree(MathUtil.getYP(), facing * -90f),
                            MathUtil.fromAxisAngleDegree(MathUtil.getXP(), 90f));
                }
                else
                    return ImmutableList.of(MathUtil.fromAxisAngleDegree(MathUtil.getXP(), 90f));
            }
        });
    }

    public static void debugRegisteredHandlers()
    {
        StringBuilder values = new StringBuilder();
        ROTATION_HANDLERS.forEach((key,handler) -> {
                if(!values.isEmpty())
                    values.append(", ");
                values.append(key);
            });
        LightmansCurrency.LogDebug("Registered Rotation Handlers: " + values);
    }

    public static void registerRotationHandler(@Nonnull String typeName, @Nonnull RotationHandler handler)
    {
        if(ROTATION_HANDLERS.containsKey(typeName))
        {
            LightmansCurrency.LogWarning("Could not register duplicate rotation handler '" + typeName + "'!");
            return;
        }
        ROTATION_HANDLERS.put(typeName, handler);
    }


}
