package io.github.lightman314.lightmanscurrency.integration.create;

import io.github.lightman314.lightmanscurrency.api.settings.pretty.PrettyTextWriter;
import io.github.lightman314.lightmanscurrency.integration.create.pretty_settings.ClipboardPrettyWriter;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class LCCreate {

    public static void init(IEventBus modBus)
    {
        modBus.addListener(LCCreate::registerCapabilities);
        PrettyTextWriter.register(ClipboardPrettyWriter.INSTANCE);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        //event.registerItem();
    }

}
