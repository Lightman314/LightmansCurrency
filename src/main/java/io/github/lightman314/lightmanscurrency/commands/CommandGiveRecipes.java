package io.github.lightman314.lightmanscurrency.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class CommandGiveRecipes {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> command
                = Commands.literal("award_recipes").requires(context -> context.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.players())
                        .then(Commands.argument("namespace", MessageArgument.message()).executes(CommandGiveRecipes::execute)));

        dispatcher.register(command);

    }

    static int execute(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {

        MinecraftServer server = commandContext.getSource().getServer();

        String namespace = MessageArgument.getMessage(commandContext,"namespace").getString();
        ResourceLocation[] recipes = server.getRecipeManager().getRecipeIds().filter(id -> id.getNamespace().equalsIgnoreCase(namespace)).toList().toArray(ResourceLocation[]::new);
        LightmansCurrency.LogDebug("Found " + recipes.length + " recipes with the '" + namespace + "' namespace!");

        int count = 0;
        for(ServerPlayer player : EntityArgument.getPlayers(commandContext,"target"))
        {
            count++;
            player.awardRecipesByKey(recipes);
        }

        return count;

    }

}