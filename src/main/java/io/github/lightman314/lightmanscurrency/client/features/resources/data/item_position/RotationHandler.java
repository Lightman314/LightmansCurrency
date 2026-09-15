package io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.rotation.RegisterRotationHandlerEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModLoader;
import org.joml.Quaternionfc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class RotationHandler {

    private static final Map<Identifier,MapCodec<? extends RotationHandler>> REGISTRY = new HashMap<>();
    private static boolean initialized = false;

    public static final Codec<RotationHandler> CODEC = CodecHelper.byNameCodec(CodecHelper.IDENTIFIER_LC_DEFAULT,REGISTRY,"Rotation Handler").dispatch(RotationHandler::getType, k -> k);

    public static void initialize() {
        if(initialized)
            return;
        initialized = true;
        ModLoader.postEvent(new RegisterRotationHandlerEvent(REGISTRY));
    }

    public static Identifier getKey(MapCodec<? extends RotationHandler> type) {
        for(var entry : REGISTRY.entrySet()) {
            if(entry.getValue() == type)
                return entry.getKey();
        }
        return Identifier.withDefaultNamespace("null");
    }

    public abstract MapCodec<? extends RotationHandler> getType();

    protected abstract List<Quaternionfc> rotate(BlockState state, float partialTicks);


}