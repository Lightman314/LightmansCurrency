package io.github.lightman314.lightmanscurrency.api;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class LCTags {
    private LCTags() {}

    public static final class Items {
        private Items() {}

        //Wallet Tags
        public static final TagKey<Item> WALLET = key("wallet");
        public static final TagKey<Item> WALLET_UPGRADE_MATERIAL = key("wallet_upgrade_material");

        public static TagKey<Item> key(String name) { return TagKey.create(Registries.ITEM,LCApi.id(name)); }

    }


}