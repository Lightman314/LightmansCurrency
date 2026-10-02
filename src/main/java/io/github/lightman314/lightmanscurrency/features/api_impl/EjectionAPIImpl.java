package io.github.lightman314.lightmanscurrency.features.api_impl;

import io.github.lightman314.lightmanscurrency.api.ejection.EjectionAPI;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionEntry;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.features.api_impl.data.EjectionDataCache;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EjectionAPIImpl implements EjectionAPI {
    public static final EjectionAPIImpl INSTANCE = new EjectionAPIImpl();
    private EjectionAPIImpl() {}

    @Override
    public List<EjectionEntry> getDataForPlayer(Player player) { return new ArrayList<>(EjectionDataCache.TYPE.get(ISidedContext.wrap(player)).getAllEntries().stream().filter(e -> e.isMember(player)).toList()); }

    @Nullable
    @Override
    public EjectionEntry getEntry(ISidedContext context,long id) { return EjectionDataCache.TYPE.get(context).getEntry(id); }

    @Override
    public void ejectData(EjectionEntry entry) {
        EjectionDataCache data = EjectionDataCache.TYPE.get(ISidedContext.LOGICAL_SERVER);
        if(data != null)
            data.addEntry(entry);
    }

}