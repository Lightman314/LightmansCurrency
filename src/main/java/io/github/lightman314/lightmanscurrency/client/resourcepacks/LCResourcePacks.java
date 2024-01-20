package io.github.lightman314.lightmanscurrency.client.resourcepacks;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.ItemPositionBlockManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.ItemPositionManager;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
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

    public static final CustomResourcePack RUPEE_PACK = registerPack(LightmansCurrency.MODID, "RupeePack",EasyText.translatable("resourcepack.lightmanscurrency.rupees"));
    public static final CustomResourcePack FANCY_ITEMS_PACK = registerPack(LightmansCurrency.MODID, "CloserItemsPack",EasyText.translatable("resourcepack.lightmanscurrency.closer_items"));

    @Nonnull
    public static CustomResourcePack registerPack(@Nonnull String modid, @Nonnull String path, @Nonnull Component name) { return registerPack(new CustomResourcePack(modid,path,name)); }

    @Nonnull
    public static CustomResourcePack registerPack(@Nonnull CustomResourcePack pack) {
        if(packList.contains(pack))
            return pack;
        packList.add(pack);
        return pack;
    }


    @SubscribeEvent
    public static void registerPackSource(AddPackFindersEvent event)
    {
        if(event.getPackType() != PackType.CLIENT_RESOURCES)
            return;
        for(CustomResourcePack pack : packList)
            pack.addToRepository((p) -> event.addRepositorySource((consumer) -> consumer.accept(p)));
    }

    @SubscribeEvent
    public static void registerResourceListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(ItemPositionManager.INSTANCE);
        event.registerReloadListener(ItemPositionBlockManager.INSTANCE);
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
