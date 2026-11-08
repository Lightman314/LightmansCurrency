package io.github.lightman314.lightmanscurrency.api;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

public final class LCDataMapTypes {
    private LCDataMapTypes() {}

    public static final DataMapType<Item,Item> MASTER_TICKET_SET = DataMapType
            .builder(LCApi.id("master_ticket_display"),Registries.ITEM,BuiltInRegistries.ITEM.byNameCodec())
            .synced(BuiltInRegistries.ITEM.byNameCodec(),true).build();

    public static final DataMapType<Item,Item> TICKET_STUB = DataMapType
            .builder(LCApi.id("ticket_stub"),Registries.ITEM,BuiltInRegistries.ITEM.byNameCodec())
            .synced(BuiltInRegistries.ITEM.byNameCodec(),true).build();

}