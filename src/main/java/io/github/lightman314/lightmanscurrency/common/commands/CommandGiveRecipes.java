package io.github.lightman314.lightmanscurrency.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.MessageArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.stream.Collectors;

public class CommandGiveRecipes {

    public static void register(CommandDispatcher<CommandSource> dispatcher)
    {
        LiteralArgumentBuilder<CommandSource> command
                = Commands.literal("award_recipes").requires(context -> context.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.players())
                        .then(Commands.argument("namespace", MessageArgument.message()).executes(CommandGiveRecipes::execute)));

        dispatcher.register(command);

    }

    static int execute(CommandContext<CommandSource> commandContext) throws CommandSyntaxException {

        MinecraftServer server = commandContext.getSource().getServer();

        String namespace = MessageArgument.getMessage(commandContext,"namespace").getString();
        List<ResourceLocation> recipes1 = server.getRecipeManager().getRecipeIds().filter(id -> id.getNamespace().equalsIgnoreCase(namespace)).collect(Collectors.toList());
        ResourceLocation[] recipes = new ResourceLocation[recipes1.size()];
        for(int i = 0; i < recipes1.size(); ++i)
            recipes[i] = recipes1.get(i);

        LightmansCurrency.LogDebug("Found " + recipes.length + " recipes with the '" + namespace + "' namespace!");

        int count = 0;
        for(ServerPlayerEntity player : EntityArgument.getPlayers(commandContext,"target"))
        {
            count++;
            player.awardRecipesByKey(recipes);
        }

        return count;

    }

}