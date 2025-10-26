package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.api.config.holder_sets.ItemListOptionSet;

public class ModHolderSets {

    public static void init() {}

    static {
        ModRegistries.HOLDER_SET.register("item_list_config", () -> ItemListOptionSet.TYPE);
    }

}
