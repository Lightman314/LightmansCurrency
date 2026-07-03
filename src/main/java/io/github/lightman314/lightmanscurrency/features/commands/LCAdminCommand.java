package io.github.lightman314.lightmanscurrency.features.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

public final class LCAdminCommand {

    private LCAdminCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,CommandBuildContext context) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("lcadmin")
                .requires(stack -> stack.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.ADMINS)));
    }



}