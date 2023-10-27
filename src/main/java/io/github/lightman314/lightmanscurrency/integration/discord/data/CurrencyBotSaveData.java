package io.github.lightman314.lightmanscurrency.integration.discord.data;

import io.github.lightman314.lightmansdiscord.discord.links.LinkedAccount;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class CurrencyBotSaveData extends SavedData {

    private final List<CurrencyBotData> data = new ArrayList<>();

    private CurrencyBotSaveData() {}

    private CurrencyBotSaveData(CompoundTag tag)
    {

    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag tag) {
        ListTag dataList = new ListTag();
        for(CurrencyBotData d : data)
            dataList.add(d.save());
        tag.put("Data", dataList);
        return tag;
    }

    protected static CurrencyBotSaveData get()
    {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server != null)
            return server.overworld().getDataStorage().computeIfAbsent(CurrencyBotSaveData::new, CurrencyBotSaveData::new, "lightmanscurrency_discord_data");
        return null;
    }

    public static void markDirty() { CurrencyBotSaveData d = get(); if(d != null) d.setDirty(); }

    @Nonnull
    protected CurrencyBotData getDataFor(@Nonnull String discordID)
    {
        for(CurrencyBotData d : this.data)
        {
            if(d.discordAccount.equals(discordID))
                return d;
        }
        CurrencyBotData newData = new CurrencyBotData(discordID);
        this.data.add(newData);
        this.setDirty();
        return newData;
    }

    @Nonnull
    public static CurrencyBotData getDataFor(@Nonnull LinkedAccount account)
    {
        CurrencyBotSaveData data = get();
        if(data == null)
            return new CurrencyBotData(account.discordID);
        return data.getDataFor(account.discordID);
    }

}