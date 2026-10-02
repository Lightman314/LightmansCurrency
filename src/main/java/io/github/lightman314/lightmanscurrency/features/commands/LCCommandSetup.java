package io.github.lightman314.lightmanscurrency.features.commands;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber
public final class LCCommandSetup {
    private LCCommandSetup() {}

    @SubscribeEvent
    private static void registerCommands(RegisterCommandsEvent event) {
        LCAdminCommand.register(event.getDispatcher(),event.getBuildContext());
        LCDebugCommand.register(event.getDispatcher(),event.getBuildContext());
        LCConfigCommand.register(event.getDispatcher(),event.getBuildContext());
    }

}