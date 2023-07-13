package io.github.lightman314.lightmanscurrency.common.easy;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class EasyText {

    public static final UUID DUMMY_ID = new UUID(0,0);

    public static MutableComponent empty() { return literal(""); }
    public static MutableComponent literal(String text) { return new TextComponent(text); }
    public static MutableComponent translatable(String translation, Object... children) { return new TranslatableComponent(translation, children); }

    public static MutableComponent makeMutable(Component text)
    {
        if(text instanceof MutableComponent mc)
            return mc;
        return EasyText.empty().append(text);
    }

    public static void sendMessage(Player player, Component message) { player.sendMessage(message, DUMMY_ID); }


    public static class Serializer extends Component.Serializer {}

}
