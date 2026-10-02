package io.github.lightman314.lightmanscurrency.api.ownership.listing;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class PotentialOwnerList {

    private final Player player;
    private final Supplier<Owner> currentOwner;
    private final Predicate<PotentialOwner> filter;
    private Owner oldOwner = null;
    private String lastSearch = "";
    private List<PotentialOwner> allOwners = null;
    private List<PotentialOwner> cache = new ArrayList<>();

    public PotentialOwnerList(Player player, Supplier<Owner> currentOwner, Predicate<PotentialOwner> filter)
    {
        this.player = player;
        this.currentOwner = currentOwner;
        this.filter = filter;
        this.updateCache("");
    }

    public void tick()
    {
        Owner data = this.currentOwner.get();
        if(data == null)
            return;
        if(this.oldOwner == null || !this.oldOwner.equals(data))
            this.updateCache(this.lastSearch);
    }

    public void updateCache(String searchFilter)
    {
        if(this.allOwners == null)
            this.allOwners = PotentialOwnerProvider.getPotentialOwners(this.player).stream().filter(this.filter).toList();
        LightmansCurrency.LogDebug("There are " + this.allOwners.size() + " total owners to filter from!");
        this.lastSearch = searchFilter;
        //Re-do the sorting whenever the search is updated
        List<PotentialOwner> temp = new ArrayList<>(this.allOwners);
        Owner data = this.currentOwner.get();
        if(data == null)
            return;
        this.oldOwner = data;
        final Owner owner = this.oldOwner;
        //Flag the current owner
        temp.forEach(po -> po.setAsCurrentOwner(po.asOwner().equals(owner)));
        temp.sort(Comparator.comparingInt(PotentialOwner::sortingPriority));
        //Filter
        if(!searchFilter.isBlank())
            temp.removeIf(po -> po.failedFilter(searchFilter));

        this.cache = List.copyOf(temp);
        LightmansCurrency.LogDebug("Found " + this.cache.size() + " potential owners.");
    }

    public List<PotentialOwner> getOwners() { return this.cache; }

}