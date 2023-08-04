package io.github.lightman314.lightmanscurrency.common.taxes;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TaxEntryCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.types.taxes.TaxesCollectedNotification;
import io.github.lightman314.lightmanscurrency.common.ownership.OwnerData;
import io.github.lightman314.lightmanscurrency.common.taxes.data.WorldArea;
import io.github.lightman314.lightmanscurrency.common.taxes.data.WorldPosition;
import io.github.lightman314.lightmanscurrency.common.taxes.reference.TaxableReference;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class TaxEntry implements IClientTracker {

    public static final int MIN_RADIUS = 5;
    public static final int MIN_VERT_SIZE = 2;
    public static final int MAX_VERT_OFFSET = 64;

    private boolean locked = true;
    public final TaxEntry unlock() { this.locked = false; return this; }

    private boolean isClient = false;
    public final boolean isClient() { return this.isClient; }
    public final TaxEntry flagAsClient() { this.isClient = true; return this.unlock(); }

    private WorldPosition center = WorldPosition.VOID;
    public WorldPosition getCenter() { return this.center; }
    public void moveCenter(@Nonnull WorldPosition newPosition) { if(this.center.equals(newPosition)) return; this.center = newPosition; this.markCenterDirty(); }
    public WorldArea getArea() { return this.infiniteRange ? WorldArea.ofInfiniteRange(this.center) : this.center.getArea(this.horizRadius, this.vertSize, this.vertOffset); }

    private int horizRadius = 10;
    public int getHorizRadius() { return this.horizRadius; }
    public void setHorizRadius(int newHorizRadius) { this.horizRadius = Math.max(MIN_RADIUS, newHorizRadius); this.markAreaDirty(); }
    private int vertSize = 10;
    public int getVertSize() { return this.vertSize; }
    public void setVertSize(int newVertSize) { this.vertSize = Math.max(MIN_VERT_SIZE, newVertSize); this.markAreaDirty(); }
    private int vertOffset = 0;
    public int getVertOffset() { return this.vertOffset; }
    public void setVertOffset(int newVertOffset) { this.vertOffset = MathUtil.clamp(newVertOffset, -MAX_VERT_OFFSET, MAX_VERT_OFFSET); this.markAreaDirty(); }

    private int taxPercentage = 0;
    public int getTaxPercentage() { return this.taxPercentage; }
    public void setTaxPercentage(int newPercentage) { this.taxPercentage = MathUtil.clamp(newPercentage, 1, 50); this.markTaxPercentageDirty(); }

    private String name = "";
    public MutableComponent getName() { if(this.name.isBlank()) return this.getDefaultName(); return EasyText.literal(this.name); }
    public void setName(String name) { this.name = name; this.markNameDirty(); }
    protected MutableComponent getDefaultName() { return EasyText.translatable("gui.lightmanscurrency.tax_entry.default_name", this.owner.getOwnerName()); }

    private final OwnerData owner = new OwnerData(this, o -> this.markOwnerDirty());
    public OwnerData getOwner() { return this.owner; }

    //Stored Money
    private CoinValue storedMoney = CoinValue.EMPTY;
    public CoinValue getStoredMoney() { return this.storedMoney; }
    public void depositMoney(CoinValue amount) { this.storedMoney = this.storedMoney.plusValue(amount); }
    public void clearStoredMoney() { this.storedMoney = CoinValue.EMPTY; }

    public final void PayTaxes(ITaxable taxable, CoinValue amount)
    {
        this.depositMoney(amount);
        this.PushNotification(TaxesCollectedNotification.create(taxable.getName(), amount, new TaxEntryCategory(this.getName(), this.id)));
    }

    //Linked Bank Account?


    //Collection Logs?
    private final NotificationData logger = new NotificationData();
    public final List<Notification> getNotifications() { return this.logger.getNotifications(); }
    public final void PushNotification(NonNullSupplier<Notification> notification) {
        this.logger.addNotification(notification.get());
        //TODO push to members/admins/owner
    }

    //Accepted Entries
    //Traders will automatically accept when placed, but pre-existing traders will need to accept manually
    //Ignored if this is an Admin Tax
    private final List<TaxableReference> acceptedEntries = new ArrayList<>();
    public final List<TaxableReference> getAcceptedEntries() { return ImmutableList.copyOf(this.acceptedEntries); }
    public final void acceptTaxes(@Nonnull ITaxable entry) {
        TaxableReference reference = entry.getReference();
        if(!this.acceptedEntries.contains(reference))
        {
            this.acceptedEntries.add(reference);
            this.markAcceptedEntriesDirty();
        }
    }
    public final void taxableBlockRemoved(@Nonnull ITaxable entry)
    {
        TaxableReference reference = entry.getReference();
        if(this.acceptedEntries.contains(reference))
        {
            this.acceptedEntries.remove(reference);
            this.markAcceptedEntriesDirty();
        }
    }

    //Whether this Tax Entry applies to a trader at the given position
    public boolean ShouldTax(@Nonnull ITaxable taxable) { return this.IsInArea(taxable) && this.admin || this.acceptedEntries.contains(taxable.getReference()); }

    public boolean IsInArea(@Nonnull ITaxable taxable) { return this.getArea().isInArea(taxable.getWorldPosition()); }

    private long id = -1;
    public long getID() { return this.id; }
    private boolean admin = false;
    public boolean isAdmin() { return this.admin; }
    public TaxEntry setAdmin(boolean isAdmin) { this.admin = isAdmin; this.markAdminStateDirty(); return this; }
    private boolean infiniteRange = false;
    public boolean isInfiniteRange() { return true; }
    public TaxEntry setInfiniteRange(boolean infiniteRange) { this.infiniteRange = infiniteRange; this.markAdminStateDirty(); return this; }

    protected final void markDirty(CompoundTag packet) { if(this.locked || this.isClient) return; TaxSaveData.MarkTaxEntryDirty(this.id, packet); }
    protected final void markDirty(Function<CompoundTag,CompoundTag> packet) { this.markDirty(packet.apply(new CompoundTag()));}

    public TaxEntry() { }
    public TaxEntry(long id, @Nullable BlockEntity core, @Nonnull Player owner)
    {
        this.id = id;
        if(core != null)
            this.center = WorldPosition.ofBE(core);
        else
        {
            this.admin = true;
            this.infiniteRange = true;
        }
        this.owner.SetOwner(owner);
    }

    public CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        tag.putLong("ID", this.id);
        this.saveCenter(tag);
        this.saveArea(tag);
        this.saveTaxRate(tag);
        this.saveOwner(tag);
        this.saveAdminState(tag);
        this.saveAcceptedEntries(tag);
        return tag;
    }

    public final void markAreaDirty() { this.markDirty(this::saveArea);}

    protected final CompoundTag saveArea(CompoundTag tag) {
        tag.putInt("HorizRadius", this.horizRadius);
        tag.putInt("VertSize", this.vertSize);
        tag.putInt("VertOffset", this.vertOffset);
        return tag;
    }

    public final void markCenterDirty() { this.markDirty(this::saveCenter); }

    protected final CompoundTag saveCenter(CompoundTag tag) {
        tag.put("Center", this.center.save());
        return tag;
    }

    public final void markTaxPercentageDirty() { this.markDirty(this::saveTaxRate); }
    protected final CompoundTag saveTaxRate(CompoundTag tag) {
        tag.putInt("TaxRate", this.taxPercentage);
        return tag;
    }

    public final void markNameDirty() { this.markDirty(this::saveName); }
    protected final CompoundTag saveName(CompoundTag tag) {
        tag.putString("CustomName", this.name);
        return tag;
    }

    public final void markOwnerDirty() { this.markDirty(this::saveOwner); }

    protected final CompoundTag saveOwner(CompoundTag tag) {
        tag.put("Owner", this.owner.save());
        return tag;
    }

    public final void markAdminStateDirty() { this.markDirty(this::saveAdminState); }

    protected final CompoundTag saveAdminState(CompoundTag tag) {
        tag.putBoolean("IsAdmin", this.admin);
        tag.putBoolean("IsInfiniteRange", this.infiniteRange);
        return tag;
    }

    public final void markAcceptedEntriesDirty() { this.markDirty(this::saveAcceptedEntries); }

    protected final CompoundTag saveAcceptedEntries(CompoundTag tag)
    {
        ListTag acceptedEntriesList = new ListTag();
        for(TaxableReference entry : this.acceptedEntries)
            acceptedEntriesList.add(entry.save());
        tag.put("AcceptedEntries", acceptedEntriesList);
        return tag;
    }

    public void load(CompoundTag tag)
    {
        if(tag.contains("ID"))
            this.id = tag.getLong("ID");
        if(tag.contains("Center"))
            this.center = WorldPosition.load(tag.getCompound("Center"));
        if(tag.contains("HorizRadius"))
            this.horizRadius = tag.getInt("HorizRadius");
        if(tag.contains("VertSize"))
            this.vertSize = tag.getInt("VertSize");
        if(tag.contains("VertOffset"))
            this.vertOffset = tag.getInt("VertOffset");
        if(tag.contains("TaxRate"))
            this.taxPercentage = tag.getInt("TaxRate");
        if(tag.contains("CustomName"))
            this.name = tag.getString("CustomName");
        if(tag.contains("Owner"))
            this.owner.load(tag.getCompound("Owner"));
        if(tag.contains("IsAdmin"))
            this.admin = tag.getBoolean("IsAdmin");
        if(tag.contains("IsInfiniteRange"))
            this.infiniteRange = tag.getBoolean("IsInfiniteRange");
        if(tag.contains("AcceptedEntries"))
        {
            ListTag acceptedEntriesList = tag.getList("AcceptedEntries", Tag.TAG_COMPOUND);
            this.acceptedEntries.clear();
            for(int i = 0; i < acceptedEntriesList.size(); ++i)
            {
                TaxableReference r = TaxableReference.load(acceptedEntriesList.getCompound(i));
                if(r != null)
                    this.acceptedEntries.add(r);
            }
        }
    }


}