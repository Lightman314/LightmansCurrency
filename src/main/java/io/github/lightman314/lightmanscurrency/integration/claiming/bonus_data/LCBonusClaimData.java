package io.github.lightman314.lightmanscurrency.integration.claiming.bonus_data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//Local storage of purchased claim and chunk loads for generic use by any claiming mod that doesn't have their own values I can intercept and/or add to.
public class LCBonusClaimData extends SavedData {

    private final List<PlayerData> bonusClaimData = new ArrayList<>();

    private LCBonusClaimData() {}
    private LCBonusClaimData(@Nonnull CompoundTag tag)
    {
        ListTag dataList = tag.getList("BonusClaims", Tag.TAG_COMPOUND);
        for(int i = 0; i < dataList.size(); ++i)
        {
            CompoundTag data = dataList.getCompound(i);
            PlayerData playerData = new PlayerData(data.getUUID("ID"));
            playerData.bonusClaims = data.getInt("Claims");
            playerData.bonusLoads = data.getInt("ChunkLoads");
            this.bonusClaimData.add(playerData);
        }
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag tag) {
        ListTag dataList = new ListTag();
        for(var pd : bonusClaimData)
        {
            if(pd.bonusClaims != 0)
            {
                CompoundTag data = new CompoundTag();
                data.putUUID("ID",pd.playerID);
                data.putInt("Claims",pd.bonusClaims);
                data.putInt("ChunkLoads",pd.bonusLoads);
                dataList.add(data);
            }
        }
        tag.put("BonusClaims", dataList);
        return tag;
    }

    public static int getBonusClaimsFor(Player player)
    {
        if(player == null)
            return 0;
        LCBonusClaimData data = get();
        if(data != null)
        {
            for(var pd : data.bonusClaimData)
            {
                if(pd.matches(player))
                    return pd.bonusClaims;
            }
        }
        return 0;
    }

    public static int getBonusChunkLoadsFor(Player player)
    {
        if(player == null)
            return 0;
        LCBonusClaimData data = get();
        if(data != null)
        {
            for(var pd : data.bonusClaimData)
            {
                if(pd.matches(player))
                    return pd.bonusLoads;
            }
        }
        return 0;
    }

    public static void addBonusClaimsFor(Player player, int amount)
    {
        if(player == null)
            return;
        LCBonusClaimData data = get();
        if(data != null)
        {
            for(var pd : data.bonusClaimData)
            {
                if(pd.matches(player))
                {
                    pd.bonusClaims += amount;
                    data.setDirty();
                    return;
                }
            }
            PlayerData newData = new PlayerData(player.getUUID());
            data.bonusClaimData.add(newData);
            newData.bonusClaims = amount;
            data.setDirty();
        }
    }

    public static void addBonusChunkLoadsFor(Player player, int amount)
    {
        if(player == null)
            return;
        LCBonusClaimData data = get();
        if(data != null)
        {
            for(var pd : data.bonusClaimData)
            {
                if(pd.matches(player))
                {
                    pd.bonusLoads += amount;
                    data.setDirty();
                    return;
                }
            }
            PlayerData newData = new PlayerData(player.getUUID());
            data.bonusClaimData.add(newData);
            newData.bonusLoads = amount;
            data.setDirty();
        }
    }

    private static LCBonusClaimData get() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server != null)
        {
            ServerLevel level = server.getLevel(Level.OVERWORLD);
            if(level != null)
                return level.getDataStorage().computeIfAbsent(LCBonusClaimData::new, LCBonusClaimData::new, "lightmanscurrency_cadmus_data");
        }
        return null;
    }

    private static class PlayerData
    {
        final UUID playerID;
        int bonusClaims = 0;
        int bonusLoads = 0;
        PlayerData(@Nonnull UUID playerID) { this.playerID = playerID; }
        boolean matches(@Nonnull Player player) { return this.playerID.equals(player.getUUID()); }
    }

}
