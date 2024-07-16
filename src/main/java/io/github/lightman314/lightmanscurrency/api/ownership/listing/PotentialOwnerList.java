package io.github.lightman314.lightmanscurrency.api.ownership.listing;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnershipAPI;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class PotentialOwnerList {

    private final Player player;
    private final Supplier<OwnerData> currentOwner;
    private final Predicate<PotentialOwner> filter;
    private Owner oldOwner;
    private String lastSearch = "";
    private List<PotentialOwner> allOwners = null;
    private List<PotentialOwner> cache = new ArrayList<>();

    public PotentialOwnerList(@Nonnull Player player, @Nonnull Supplier<OwnerData> currentOwner, @Nonnull Predicate<PotentialOwner> filter)
    {
        this.player = player;
        this.currentOwner = currentOwner;
        this.filter = filter;
        this.updateCache("");
    }

    public void tick()
    {
        OwnerData data = this.currentOwner.get();
        if(data == null)
            return;
        if(this.oldOwner == null || !this.oldOwner.matches(data.getValidOwner()))
            this.updateCache(this.lastSearch);
    }

    public void updateCache(@Nonnull String searchFilter)
    {
        if(this.allOwners == null)
            this.allOwners = ImmutableList.copyOf(OwnershipAPI.API.getPotentialOwners(this.player).stream().filter(this.filter).toList());
        this.lastSearch = searchFilter;
        //Re-do the sorting whenever the search is updated
        List<PotentialOwner> temp = new ArrayList<>(this.allOwners);
        OwnerData data = this.currentOwner.get();
        if(data == null)
            return;
        this.oldOwner = data.getValidOwner();
        final Owner owner = this.oldOwner;
        //Flag the current owner
        temp.forEach(po -> po.setAsCurrentOwner(po.asOwner().matches(owner)));
        temp.sort(Comparator.comparingInt(PotentialOwner::sortingPriority));
        //Filter
        if(!searchFilter.isBlank())
            temp.removeIf(po -> po.failedFilter(searchFilter));

        this.cache = ImmutableList.copyOf(temp);
    }

    @Nonnull
    public List<PotentialOwner> getOwners() { return this.cache; }

}
