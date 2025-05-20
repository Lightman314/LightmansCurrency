package io.github.lightman314.lightmanscurrency.client.resourcepacks;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.text.DualTextEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class LCResourcePacks {

    private LCResourcePacks() {}

    private static final List<CustomResourcePack> packList = new ArrayList<>();

    static {
        registerPack(LightmansCurrency.MODID, "RupeePack", LCText.RESOURCE_PACK_RUPEES);
        registerPack(LightmansCurrency.MODID, "CloserItemsPack",LCText.RESOURCE_PACK_CLOSER_ITEMS);
        registerPack(LightmansCurrency.MODID, "LegacyCoins",LCText.RESOURCE_PACK_LEGACY_COINS);
        registerPack(LightmansCurrency.MODID, "FancyIcons",LCText.RESOURCE_PACK_FANCY_ICONS);
    }

    public static void registerPack(@Nonnull String modid, @Nonnull String path, @Nonnull DualTextEntry text) { registerPack(new CustomResourcePack(modid,path,text.first.get())); }

    public static void registerPack(@Nonnull String modid, @Nonnull String path, @Nonnull Component name) { registerPack(new CustomResourcePack(modid,path,name)); }

    public static void registerPack(@Nonnull CustomResourcePack pack) {
        if(packList.contains(pack))
            return;
        packList.add(pack);
    }


    @SubscribeEvent
    public static void registerPackSource(AddPackFindersEvent event)
    {
        if(event.getPackType() != PackType.CLIENT_RESOURCES)
            return;
        for(CustomResourcePack pack : packList)
            pack.addToRepository((p) -> event.addRepositorySource((consumer) -> consumer.accept(p)));
    }

    public static class CustomResourcePack
    {
        private final String modid;
        private final String path;
        private final Component name;
        public CustomResourcePack(@Nonnull String modid, @Nonnull String path, @Nonnull Component name)
        {
            this.modid = modid;
            this.path = path;
            this.name = name;
        }

        public void addToRepository(@Nonnull Consumer<Pack> consumer)
        {
            Path resourcePath = ModList.get().getModFileById(this.modid).getFile().findResource(this.path);
            Pack pack = Pack.readMetaAndCreate("builtin/" + this.path, this.name, false, (path) -> new PathPackResources(path, resourcePath, false), PackType.CLIENT_RESOURCES, Pack.Position.TOP, PackSource.BUILT_IN);
            if(pack == null)
                LightmansCurrency.LogWarning("Custom Resource Pack of '" + this.modid + "/" + this.path + " failed to load properly!");
            else
                consumer.accept(pack);
        }
    }

}
