package io.github.lightman314.lightmanscurrency.api.ownership.listing;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerHolder;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class PotentialOwnerList {

    private final Player player;
    private final Supplier<OwnerHolder> currentOwner;
    private final Predicate<PotentialOwner> filter;
    private Owner oldOwner;
    private String lastSearch = "";
    private List<PotentialOwner> allOwners = null;
    private List<PotentialOwner> cache = new ArrayList<>();

    public PotentialOwnerList(Player player, Supplier<OwnerHolder> currentOwner, Predicate<PotentialOwner> filter)
    {
        this.player = player;
        this.currentOwner = currentOwner;
        this.filter = filter;
        this.updateCache("");
    }

    public void tick()
    {
        OwnerHolder data = this.currentOwner.get();
        if(data == null)
            return;
        if(this.oldOwner == null || !this.oldOwner.matches(data.getValidOwner()))
            this.updateCache(this.lastSearch);
    }

    public void updateCache(String searchFilter)
    {
        if(this.allOwners == null)
            this.allOwners = PotentialOwnerProvider.getPotentialOwners(this.player).stream().filter(this.filter).toList();
        this.lastSearch = searchFilter;
        //Re-do the sorting whenever the search is updated
        List<PotentialOwner> temp = new ArrayList<>(this.allOwners);
        OwnerHolder data = this.currentOwner.get();
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

    public List<PotentialOwner> getOwners() { return this.cache; }

}