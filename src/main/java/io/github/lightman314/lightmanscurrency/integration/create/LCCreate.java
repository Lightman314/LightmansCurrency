package io.github.lightman314.lightmanscurrency.integration.create;

import io.github.lightman314.lightmanscurrency.api.settings.pretty.PrettyTextWriter;
import io.github.lightman314.lightmanscurrency.integration.create.pretty_settings.ClipboardPrettyWriter;

public class LCCreate {

    public static void init()
    {
        PrettyTextWriter.register(ClipboardPrettyWriter.INSTANCE);
    }

}