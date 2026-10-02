package io.github.lightman314.lightmanscurrency.api.ejection;

import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.interfaces.IOwnerHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public interface EjectionAPI {

    List<EjectionEntry> getDataForPlayer(Player player);
    @Nullable
    EjectionEntry getEntry(ISidedContext context,long id);

    default void ejectData(Owner owner,List<ItemStack> contents) { this.ejectData(new EjectionEntry(owner,contents)); }
    default void ejectData(IOwnerHolder owner,ItemStack contents) { this.ejectData(owner,List.of(contents)); }
    default void ejectData(IOwnerHolder owner,List<ItemStack> contents) { this.ejectData(new EjectionEntry(owner,contents)); }
    void ejectData(EjectionEntry entry);

}