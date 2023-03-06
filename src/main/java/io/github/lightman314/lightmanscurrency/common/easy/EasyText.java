package io.github.lightman314.lightmanscurrency.common.easy;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.UUID;

public class EasyText {

    public static IFormattableTextComponent empty() { return literal(""); }
    public static IFormattableTextComponent literal(String text) { return new StringTextComponent(text); }
    public static IFormattableTextComponent translatable(String translation, Object... children) { return new TranslationTextComponent(translation, children); }

    public static void sendMessage(ServerPlayerEntity player, ITextComponent message) { player.sendMessage(message, new UUID(0,0)); }

}
