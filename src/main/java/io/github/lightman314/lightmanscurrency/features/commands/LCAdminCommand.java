package io.github.lightman314.lightmanscurrency.features.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.command.arguments.TraderArgument;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.data_components.StoredTrader;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.WorldNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IDisplayNode;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import io.github.lightman314.lightmanscurrency.features.admin.AdminMode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.item.ItemStack;

public final class LCAdminCommand {

    private LCAdminCommand() {}

    public static final TextEntry TOGGLE_ADMIN = TextEntry.command(LCApi.MODID,"lcadmin.toggleadmin");
    public static final TextEntry TOGGLE_ADMIN_ENABLED = TextEntry.command(LCApi.MODID,"lcadmin.toggleadmin.enabled");
    public static final TextEntry TOGGLE_ADMIN_DISABLED = TextEntry.command(LCApi.MODID,"lcadmin.toggleadmin.disabled");
    public static final TextEntry TRADER_DELETE_SUCCESS = TextEntry.command(LCApi.MODID,"lcadmin.trader.delete");
    public static final TextEntry TRADER_RECOVER_SUCCESS = TextEntry.command(LCApi.MODID,"lcadmin.trader.recover");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,CommandBuildContext context) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("lcadmin")
                .requires(stack -> stack.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.literal("toggleAdmin")
                        .requires(CommandSourceStack::isPlayer)
                        .executes(LCAdminCommand::toggleAdminMode))
                .then(Commands.literal("trader")
                        .then(Commands.literal("delete")
                                .then(Commands.argument("trader",TraderArgument.trader())
                                        .executes(LCAdminCommand::deleteTrader)))
                        .then(Commands.literal("recover")
                                .then(Commands.argument("trader",TraderArgument.recoverableTrader())
                                        .then(Commands.argument("player",EntityArgument.player())
                                                .executes(LCAdminCommand::recoverTrader))
                                        .requires(CommandSourceStack::isPlayer)
                                        .executes(c -> recoverTraderInternal(c,c.getSource().getPlayerOrException())))));

        dispatcher.register(command);
    }

    private static int toggleAdminMode(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack stack = context.getSource();

        Component newMode = AdminMode.toggleAdmin(stack.getPlayerOrException()) ? TOGGLE_ADMIN_ENABLED.getWithStyle(ChatFormatting.GREEN) : TOGGLE_ADMIN_DISABLED.getWithStyle(ChatFormatting.RED);
        stack.sendSuccess(() -> TOGGLE_ADMIN.get(newMode),true);
        return 1;
    }

    private static int deleteTrader(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        TraderData trader = TraderArgument.getTrader(context,"trader");
        LCApi.getTraderAPI().deleteTrader(trader);
        context.getSource().sendSuccess(() -> TRADER_DELETE_SUCCESS.get(IDisplayNode.getTraderName(trader)),true);
        return 1;
    }

    private static int recoverTrader(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return recoverTraderInternal(context,EntityArgument.getPlayer(context,"player"));
    }
    private static int recoverTraderInternal(CommandContext<CommandSourceStack> context,ServerPlayer target) throws CommandSyntaxException {
        TraderData trader = TraderArgument.getTrader(context,"trader");
        WorldNode node = trader.getNode(WorldNode.TYPE);
        if(node != null) {
            ItemStack item = new ItemStack(node.getBlock());
            item.set(LCDataComponents.STORED_TRADER,new StoredTrader(trader,node.getState().allowAccess));
            target.getInventory().placeItemBackInInventory(item);
            context.getSource().sendSuccess(() -> TRADER_RECOVER_SUCCESS.get(target.getName(),IDisplayNode.getTraderName(trader)),true);
        }
        return 0;
    }

}