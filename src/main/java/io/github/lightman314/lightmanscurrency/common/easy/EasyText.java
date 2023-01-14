package io.github.lightman314.lightmanscurrency.common.easy;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class EasyText {

    public static MutableComponent empty() { return literal(""); }
    public static MutableComponent literal(String text) { return new TextComponent(text); }
    public static MutableComponent translatable(String translation, Object... children) { return new TranslatableComponent(translation, children); }

    public static void sendMessage(ServerPlayer player, Component message) { player.sendMessage(message, new UUID(0,0)); }

}
