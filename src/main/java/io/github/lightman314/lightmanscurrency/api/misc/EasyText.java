package io.github.lightman314.lightmanscurrency.api.misc;

import com.google.gson.stream.MalformedJsonException;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public class EasyText {

    public static MutableComponent empty() { return Component.empty(); }
    public static MutableComponent literal(String text) { return Component.literal(text); }
    public static MutableComponent translatable(String translation, Object... children) { return Component.translatable(translation, children); }
    public static MutableComponent translatableWithFallback(String translation, String fallback) { return Component.translatableWithFallback(translation, fallback); }
    @Nullable
    public static MutableComponent translatableOrNull(String translationKey, Object... children) {
        Language lang = Language.getInstance();
        if(lang.has(translationKey))
            return EasyText.translatable(translationKey,children);
        return null;
    }

    public static MutableComponent makeMutable(Component text)
    {
        if(text instanceof MutableComponent mc)
            return mc;
        return text.copy();
    }

    public static void sendMessage(Player player, Component message) { player.sendSystemMessage(message); }

    public static void sendCommandSucess(CommandContext<CommandSourceStack> stack, Component message, boolean postToAdmins) { sendCommandSucess(stack.getSource(),message,postToAdmins); }
    public static void sendCommandSucess(CommandSourceStack stack, Component message, boolean postToAdmins) { stack.sendSuccess(() -> message, postToAdmins); }

    public static void sendCommandFail(CommandContext<CommandSourceStack> stack, Component message) { sendCommandFail(stack.getSource(),message); }
    public static void sendCommandFail(CommandSourceStack stack, Component message) { stack.sendFailure(message); }

    public static Component loadComponentOrString(String componentJson)
    {
        try { return Component.Serializer.fromJson(componentJson);
        } catch (Exception exception) { return EasyText.literal(componentJson); }
    }

}
