package io.github.lightman314.lightmanscurrency.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lightman314.lightmanscurrency.common.commands.arguments.ColorArgument;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.tickets.TicketSaveData;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public class CommandTicket {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> command =
                Commands.literal("tickets")
                        .requires(commandSource -> commandSource.getEntity() instanceof PlayerEntity && commandSource.hasPermission(2))
                        .then(Commands.literal("changeColor")
                                .then(Commands.argument("color", ColorArgument.argument())
                                        .executes(CommandTicket::changeColor)))
                        .then(Commands.literal("create")
                                .executes(CommandTicket::createTicketNonColored)
                                .then(Commands.argument("color", ColorArgument.argument())
                                        .executes(CommandTicket::createTicketColored)));

        dispatcher.register(command);
    }

    static int changeColor(CommandContext<CommandSource> commandContext) throws CommandSyntaxException {
        ServerPlayerEntity player = commandContext.getSource().getPlayerOrException();
        ItemStack heldItem = player.getMainHandItem();
        if(heldItem.getItem() instanceof TicketItem)
        {
            int color = ColorArgument.getColor(commandContext, "color");
            TicketItem.SetTicketColor(heldItem, color);
        }
        else
            commandContext.getSource().sendFailure(EasyText.translatable("command.lightmanscurrency.ticket.color.not_held"));
        return 0;
    }

    static int createTicketNonColored(CommandContext<CommandSource> commandContext) throws CommandSyntaxException {
        ServerPlayerEntity player = commandContext.getSource().getPlayerOrException();
        ItemStack ticket = TicketItem.CreateMasterTicket(TicketSaveData.createNextID());
        giveItemToPlayer(player, ticket);
        return 1;
    }

    static int createTicketColored(CommandContext<CommandSource> commandContext) throws CommandSyntaxException {
        ServerPlayerEntity player = commandContext.getSource().getPlayerOrException();
        int color = ColorArgument.getColor(commandContext,"color");
        ItemStack ticket = TicketItem.CreateMasterTicket(TicketSaveData.createNextID(), color);
        giveItemToPlayer(player, ticket);
        return 1;
    }

    private static void giveItemToPlayer(ServerPlayerEntity player, ItemStack item) {
        PlayerInventory inv = player.inventory;
        if(inv.getItem(inv.selected).isEmpty())
            player.inventory.setItem(inv.selected, item);
        else
            ItemHandlerHelper.giveItemToPlayer(player, item);
    }

}