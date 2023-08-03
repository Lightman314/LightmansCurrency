package io.github.lightman314.lightmanscurrency.common.easy;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public class EasyText {

    public static MutableComponent empty() { return Component.empty(); }
    public static MutableComponent literal(String text) { return Component.literal(text); }
    public static MutableComponent translatable(String translation, Object... children) { return Component.translatable(translation, children); }

    public static MutableComponent makeMutable(Component text)
    {
        if(text instanceof MutableComponent mc)
            return mc;
        return EasyText.empty().append(text);
    }

    public static void sendMessage(ServerPlayer player, Component message) { player.sendSystemMessage(message); }

    public static class Serializer extends Component.Serializer {}

}
