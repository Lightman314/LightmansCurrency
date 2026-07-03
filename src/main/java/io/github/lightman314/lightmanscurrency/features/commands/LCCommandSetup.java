package io.github.lightman314.lightmanscurrency.features.commands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.function.Predicate;

@EventBusSubscriber
public final class LCCommandSetup {
    private LCCommandSetup() {}

    @SubscribeEvent
    private static void registerCommands(RegisterCommandsEvent event) {
        LCAdminCommand.register(event.getDispatcher(),event.getBuildContext());
        LCDebugCommand.register(event.getDispatcher(),event.getBuildContext());
    }

    public static Predicate<CommandSourceStack> requiresLevel(PermissionLevel level) { return stack -> stack.permissions().hasPermission(new Permission.HasCommandLevel(level)); }

}