package io.github.lightman314.lightmanscurrency.integration.create;

import com.simibubi.create.AllItems;
import io.github.lightman314.lightmanscurrency.api.filter.FilterAPI;
import io.github.lightman314.lightmanscurrency.api.settings.pretty.PrettyTextWriter;
import io.github.lightman314.lightmanscurrency.integration.create.attributes.LCItemAttributes;
import io.github.lightman314.lightmanscurrency.integration.create.filter.CreateItemTradeFilter;
import io.github.lightman314.lightmanscurrency.integration.create.pretty_settings.ClipboardPrettyWriter;
import net.neoforged.bus.api.IEventBus;

public class LCCreate {

    public static void init(IEventBus modBus)
    {
        //Registering the mod bus should force the class to load and initialize the other static variables
        LCItemAttributes.REGISTRY.register(modBus);

        PrettyTextWriter.register(ClipboardPrettyWriter.INSTANCE);

        FilterAPI.registerFilterProvider(stack -> {
            if(stack.getItem() == AllItems.FILTER.get() || stack.getItem() == AllItems.ATTRIBUTE_FILTER.get())
                return CreateItemTradeFilter.INSTANCE;
            return null;
        });

    }

}
