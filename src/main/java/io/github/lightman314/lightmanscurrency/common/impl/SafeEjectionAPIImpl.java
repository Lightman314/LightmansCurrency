package io.github.lightman314.lightmanscurrency.common.impl;

import io.github.lightman314.lightmanscurrency.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionDataType;
import io.github.lightman314.lightmanscurrency.api.ejection.SafeEjectionAPI;
import io.github.lightman314.lightmanscurrency.common.data.types.EjectionDataCache;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.OldEjectionDataHelper;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SafeEjectionAPIImpl extends SafeEjectionAPI {

    public SafeEjectionAPIImpl() {}

    @Override
    public List<EjectionData> getAllData(boolean isClient) { return EjectionDataCache.TYPE.get(isClient).getData(); }

    
    @Override
    public List<EjectionData> getDataForPlayer(Player player) { return this.getAllData(player.level().isClientSide).stream().filter(d -> d.canAccess(player) && !d.isEmpty()).toList(); }

    @Nullable
    @Override
    public EjectionData parseData(CompoundTag tag) {
        if(!tag.contains("type"))
            return OldEjectionDataHelper.parseOldData(tag);
        EjectionDataType type = LCRegistries.EJECTION_DATA.getValue(VersionUtil.parseResource(tag.getString("type")));
        if(type == null)
        {
            LightmansCurrency.LogWarning("Could not parse Ejection Data as no EjectionDataType was registered for '" + tag.getString("type") + "'!");
            return null;
        }
        EjectionData data = type.load(tag);
        if(tag.contains("ID"))
            data.setID(tag.getLong("ID"));
        return type.load(tag);
    }

    @Override
    public void handleEjection(Level level, BlockPos pos, EjectionData data) {
        if(level.isClientSide)
            return;
        EjectionDataCache d = EjectionDataCache.TYPE.get(false);
        if(d != null)
            d.handleEjection(level,pos,data);
    }

}