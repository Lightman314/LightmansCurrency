package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.rotation;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.ItemTraderBlockEntityRenderer;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.RotationHandler;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.RotationHandlerType;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpinningRotation extends RotationHandler {

    private static final Map<Float,SpinningRotation> cache = new HashMap<>();

    public static final RotationHandlerType TYPE = SpinningRotation::parse;

    private final float speed;
    protected SpinningRotation(float speed) { super(TYPE); this.speed = speed; }

    public static SpinningRotation createDefault() { return create(2f); }
    public static SpinningRotation create(float speed) {
        if(!cache.containsKey(speed))
            cache.put(speed,new SpinningRotation(speed));
        return cache.get(speed);
    }

    @Override
    protected List<Quaternionf> rotate(BlockState state, float partialTicks) {
        return ImmutableList.of(ItemTraderBlockEntityRenderer.getRotation(partialTicks,this.speed));
    }

    @Override
    protected void writeAdditional(JsonObject json) {
        json.addProperty("speed",this.speed);
    }

    private static SpinningRotation parse(JsonObject json) throws JsonSyntaxException { return create(GsonHelper.getAsFloat(json,"speed",1f)); }

}
