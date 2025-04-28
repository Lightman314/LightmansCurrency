package io.github.lightman314.lightmanscurrency.common.data;

import io.github.lightman314.lightmanscurrency.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.IEasyTickable;
import io.github.lightman314.lightmanscurrency.api.misc.IServerTicker;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomData;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomDataType;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@EventBusSubscriber
public class CustomSaveData extends SavedData {

    private static final Map<ResourceLocation,CustomData> serverDataCache = new HashMap<>();

    @Nullable
    public static <T extends CustomData> T getData(CustomDataType<T> type)
    {
        ResourceLocation dataID = LCRegistries.CUSTOM_DATA.getKey(type);
        if(dataID == null)
        {
            LightmansCurrency.LogError("Custom Data was not registered!");
            return null;
        }
        if(!serverDataCache.containsKey(dataID))
        {
            LightmansCurrency.LogWarning("Attempted to get custom data '" + dataID + "' before the server started!",new Throwable());
            return null;
        }
        return (T)serverDataCache.get(dataID);
    }

    public static boolean isLoaded(CustomDataType<?> type) {
        ResourceLocation dataID = LCRegistries.CUSTOM_DATA.getKey(type);
        if(dataID == null)
            return false;
        return serverDataCache.containsKey(dataID);
    }

    private static void initServerData(MinecraftServer server)
    {
        ServerLevel overworld = server.overworld();
        if(overworld == null)
            return;
        serverDataCache.clear();
        LCRegistries.CUSTOM_DATA.forEach(type -> {
            ResourceLocation id = LCRegistries.CUSTOM_DATA.getKey(type);
            CustomSaveData data = overworld.getDataStorage().computeIfAbsent(factory(type), type.fileName);
            serverDataCache.put(id,data.data);
        });
    }

    private final CustomData data;
    private CustomSaveData(@Nonnull CustomData data) {
        this.data = data;
        this.data.initServer(this::setDirty);
    }

    private static Factory<CustomSaveData> factory(CustomDataType<?> type) { return new Factory<>(() ->
                new CustomSaveData(type.create()),
            (t,l) -> {
                CustomData data = type.create();
                //LightmansCurrency.LogDebug("Loading '" + type.fileName + "' from file!\n" + t.getAsString());
                data.loadData(t,l);
                return new CustomSaveData(data);
            });
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider lookup) {
        this.data.save(tag,lookup);
        //LightmansCurrency.LogDebug("Saving '" + this.data.getType().fileName + "' to file!\n" + tag.getAsString());
        return tag;
    }

    @SubscribeEvent
    private static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if(event.getEntity() instanceof ServerPlayer player)
            forEach(data -> data.onPlayerJoin(player));
    }

    @SubscribeEvent
    private static void onServerTick(ServerTickEvent.Pre event)
    {
        for(CustomData data : serverDataCache.values())
        {
            if(data instanceof IEasyTickable ticker)
                ticker.tick();
            if(data instanceof IServerTicker ticker)
                ticker.serverTick();
        }
    }

    @SubscribeEvent
    private static void onServerStart(ServerStartedEvent event)
    {
        //Forcibly load all custom data from file on server boot
        initServerData(event.getServer());
    }

    @SubscribeEvent
    private static void onServerStop(ServerStoppedEvent event) {
        //Clear the data cache when the server stops
        serverDataCache.clear();
    }

    private static void forEach(Consumer<CustomData> consumer) { serverDataCache.values().forEach(consumer);}

}
