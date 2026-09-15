package io.github.lightman314.lightmanscurrency.client.features.resources;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.text.DualTextEntry;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@EventBusSubscriber(Dist.CLIENT)
public final class BuiltInResourcePacks {

    public static final DualTextEntry CLOSER_ITEMS_PACK = DualTextEntry.resourcePack(LCApi.MODID,"closer_items");
    public static final DualTextEntry FANCY_ICONS_PACK = DualTextEntry.resourcePack(LCApi.MODID,"fancy_icons");
    public static final DualTextEntry LEGACY_COINS_PACK = DualTextEntry.resourcePack(LCApi.MODID,"legacy_coins");
    public static final DualTextEntry RUPEES_PACK = DualTextEntry.resourcePack(LCApi.MODID,"rupees");

    @SubscribeEvent
    private static void registerBuiltInPacks(AddPackFindersEvent event) {
        addBuiltInPack(event,"closer_items",CLOSER_ITEMS_PACK);
        addBuiltInPack(event,"fancy_icons",FANCY_ICONS_PACK);
        addBuiltInPack(event,"legacy_coins",LEGACY_COINS_PACK);
        addBuiltInPack(event,"rupees",RUPEES_PACK);
    }

    private static void addBuiltInPack(AddPackFindersEvent event,String path,DualTextEntry title) {
        event.addPackFinders(LCApi.id(path),PackType.CLIENT_RESOURCES,title.first.get(),PackSource.BUILT_IN,false,Pack.Position.TOP);
    }

}
