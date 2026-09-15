package io.github.lightman314.lightmanscurrency.features.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.features.admin.AdminMode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

public final class LCAdminCommand {

    private LCAdminCommand() {}

    public static final TextEntry TOGGLE_ADMIN = TextEntry.command(LCApi.MODID,"lcadmin.toggleadmin");
    public static final TextEntry TOGGLE_ADMIN_ENABLED = TextEntry.command(LCApi.MODID,"lcadmin.toggleadmin.enabled");
    public static final TextEntry TOGGLE_ADMIN_DISABLED = TextEntry.command(LCApi.MODID,"lcadmin.toggleadmin.disabled");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,CommandBuildContext context) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("lcadmin")
                .requires(stack -> stack.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.ADMINS)))
                .then(Commands.literal("toggleAdmin")
                        .requires(CommandSourceStack::isPlayer)
                        .executes(LCAdminCommand::toggleAdminMode));

        dispatcher.register(command);
    }

    private static int toggleAdminMode(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack stack = context.getSource();

        Component newMode = AdminMode.toggleAdmin(stack.getPlayerOrException()) ? TOGGLE_ADMIN_ENABLED.getWithStyle(ChatFormatting.GREEN) : TOGGLE_ADMIN_DISABLED.getWithStyle(ChatFormatting.RED);
        stack.sendSuccess(() -> TOGGLE_ADMIN.get(newMode),true);
        return 1;
    }

}