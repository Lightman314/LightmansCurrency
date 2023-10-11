package io.github.lightman314.lightmanscurrency.common.taxes;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.bank.BankSaveData;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.providers.TaxCollectorMenuProvider;
import io.github.lightman314.lightmanscurrency.common.menus.validation.EasyMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TaxEntryCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.types.taxes.TaxesCollectedNotification;
import io.github.lightman314.lightmanscurrency.common.ownership.OwnerData;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.taxes.data.TaxStats;
import io.github.lightman314.lightmanscurrency.common.taxes.data.WorldArea;
import io.github.lightman314.lightmanscurrency.common.taxes.data.WorldPosition;
import io.github.lightman314.lightmanscurrency.common.taxes.reference.TaxableReference;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TaxEntry implements IClientTracker {

    public static int minRadius() { return 5; }
    public static int maxRadius() { return Config.SERVER.taxMachineMaxRadius.get(); }
    public static int minHeight() { return 2; }
    public static int maxHeight() { return Config.SERVER.taxMachineMaxHeight.get(); }
    public static int minVertOffset() { return -maxVertOffset(); }
    public static int maxVertOffset() { return Config.SERVER.taxMachineMaxVertOffset.get(); }

    public static int maxTaxRate() { return Config.SERVER.taxMachineMaxRate.get(); }

    public final TaxStats stats = new TaxStats(this);

    private boolean locked = true;
    public final TaxEntry unlock() { this.locked = false; return this; }

    private boolean isClient = false;
    public final boolean isClient() { return this.isClient; }
    public final TaxEntry flagAsClient() { this.isClient = true; return this.unlock(); }

    public final boolean isServerEntry() { return this.id == TaxSaveData.SERVER_TAX_ID; }

    private WorldPosition center = WorldPosition.VOID;
    public WorldPosition getCenter() { return this.center; }
    public void moveCenter(@Nonnull WorldPosition newPosition) { if(this.center.equals(newPosition)) return; this.center = newPosition; this.markCenterDirty(); }
    public WorldArea getArea() { return this.isInfiniteRange() ? WorldArea.ofInfiniteRange(this.center) : this.center.getArea(this.getRadius(), this.getHeight(), this.getVertOffset()); }

    private int radius = 10;
    public int getRadius() { return MathUtil.clamp(this.radius, minRadius(), maxRadius()); }
    public void setRadius(int newRadius) { if(this.isInfiniteRange()) return; this.radius = MathUtil.clamp(newRadius, minRadius(), maxRadius()); this.markAreaDirty(); }
    private int height = 10;
    public int getHeight() { return MathUtil.clamp(this.height, minHeight(), maxHeight()); }
    public void setHeight(int newHeight) { if(this.isInfiniteRange()) return; this.height = MathUtil.clamp(newHeight, minHeight(), maxHeight()); this.markAreaDirty(); }
    private int vertOffset = 0;
    public int getVertOffset() { return MathUtil.clamp(this.vertOffset, minVertOffset(), maxVertOffset()); }
    public void setVertOffset(int newVertOffset) { if(this.isInfiniteRange()) return; this.vertOffset = MathUtil.clamp(newVertOffset, minVertOffset(), maxVertOffset()); this.markAreaDirty(); }

    /**
     * Render Modes:
     * 0- Don't draw anything
     * 1- Draw area to members only
     * 2- Draw area to all players
     */
    private int renderMode = 1;
    public int getRenderMode() { if(this.isInfiniteRange()) return 0; return this.renderMode; }
    public void setRenderMode(int newRenderMode) { this.renderMode = newRenderMode % 3; this.markRenderModeDirty(); }
    public boolean shouldRender(Player player)
    {
        //Don't render the area if there is no area to draw (because infinite range)
        if(player == null || this.isInfiniteRange())
            return false;
        if(LCAdminMode.isAdminPlayer(player))
            return true;
        if(this.getRenderMode() == 1)
            return this.canAccess(player);
        //Don't render for non-members if not active
        return this.getRenderMode() == 2 && this.isActive();
    }
    public int getRenderColor(Player player)
    {
        if(this.canAccess(player))
        {
            //Render green or red for members based on active state
            if(this.active)
                return 0x00C800;
            else
                return 0xC80000;
        }
        else if(this.renderMode != 2)
        {
            //Render off green/red for admins
            if(this.active)
                return 0x007700;
            else
                return 0x770000;
        }
        //Render yellow for non-members
        return 0xC8C800;
    }


    private int taxRate = 1;
    public int getTaxRate() { return MathUtil.clamp(this.taxRate, 0, Config.SERVER.taxMachineMaxRate.get()); }
    public void setTaxRate(int newPercentage) { this.taxRate = MathUtil.clamp(newPercentage, 1, Config.SERVER.taxMachineMaxRate.get()); this.markTaxPercentageDirty(); }

    private String name = "";
    public String getCustomName() { return this.name; }
    public MutableComponent getName() { if(this.name.isBlank()) return this.getDefaultName(); return EasyText.literal(this.name); }
    public void setName(String name) { this.name = name; this.markNameDirty(); }
    protected MutableComponent getDefaultName() { return EasyText.translatable("gui.lightmanscurrency.tax_entry.default_name", this.isServerEntry() ? EasyText.translatable("gui.lightmanscurrency.tax_entry.default_name.server") : this.owner.getOwnerName()); }

    private final OwnerData owner = new OwnerData(this, o -> this.markOwnerDirty());
    public OwnerData getOwner() { return this.owner; }
    public final boolean canAccess(@Nonnull Player player) { if(this.isServerEntry()) return true; return this.owner.isMember(player); }

    //Stored Money
    private CoinValue storedMoney = CoinValue.EMPTY;
    public CoinValue getStoredMoney() { return this.storedMoney; }
    public void depositMoney(CoinValue amount) {
        BankAccount account = this.getBankAccount();
        if(account != null)
        {
            account.depositCoins(amount);
            account.LogInteraction(this, amount);
            return;
        }
        this.storedMoney = this.storedMoney.plusValue(amount);
        this.markStoredMoneyDirty();
    }
    public void clearStoredMoney() { this.storedMoney = CoinValue.EMPTY; }

    @Nonnull
    public final CoinValue CalculateAndPayTaxes(@Nonnull ITaxable taxable, @Nonnull CoinValue taxableAmount)
    {
        CoinValue amountToPay = taxableAmount.percentageOfValue(this.getTaxRate());
        if(amountToPay.hasAny())
        {
            this.depositMoney(amountToPay);
            this.PushNotification(TaxesCollectedNotification.create(taxable.getName(), amountToPay, new TaxEntryCategory(this.getName(), this.id)));
            this.stats.OnTaxesCollected(taxable, amountToPay);
        }
        return amountToPay;
    }

    //Linked Bank Account?
    private boolean linkToBank = false;
    public void setLinkedToBank(boolean newState) { this.linkToBank = newState; this.markBankStateDirty(); }
    public boolean isLinkedToBank() { return this.linkToBank && !this.isServerEntry(); }
    public final BankAccount getBankAccount()
    {
        if(!this.linkToBank)
            return null;
        if(this.owner.hasTeam())
        {
            Team team = this.owner.getTeam();
            if(team.hasBankAccount())
                return team.getBankAccount();
        }
        else if(this.owner.hasPlayer())
            return BankSaveData.GetBankAccount(this.isClient, this.owner.getPlayer().id);
        return null;
    }

    private final NotificationData logger = new NotificationData();
    public final List<Notification> getNotifications() { return this.logger.getNotifications(); }

    public final void PushNotification(NonNullSupplier<Notification> notification) {
        if(this.isClient)
            return;

        this.logger.addNotification(notification.get());
        this.markNotificationsDirty();
    }

    //Accepted Entries
    //Traders will automatically accept when placed, but pre-existing traders will need to accept manually
    //Ignored if this is an Admin Tax
    private final List<TaxableReference> acceptedEntries = new ArrayList<>();
    public final List<TaxableReference> getAcceptedEntries() { return ImmutableList.copyOf(this.acceptedEntries); }
    public final void acceptTaxes(@Nonnull ITaxable entry) {
        TaxableReference reference = entry.getReference();
        if(!this.acceptedEntries.contains(reference) && reference != null)
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
    public boolean ShouldTax(@Nonnull ITaxable taxable) { return this.IsInArea(taxable) && (this.forcesAcceptance() || this.acceptedEntries.contains(taxable.getReference())); }

    public boolean IsInArea(@Nonnull ITaxable taxable) { return this.isActive() && this.getArea().isInArea(taxable.getWorldPosition()); }

    private long id = -1;
    public long getID() { return this.id; }
    private boolean active = false;
    public boolean isActive() { return this.active; }
    public void setActive(boolean newState, @Nullable Player player) {
        if(this.active == newState)
            return;
        if(Config.SERVER.taxMachinesAdminOnly.get() && !LCAdminMode.isAdminPlayer(player) && !this.active)
        {
            Permissions.PermissionWarning(player, "activate a tax entry", Permissions.ADMIN_MODE);
            return;
        }
        this.active = newState;
        this.markActiveStateDirty();
    }
    private boolean forceAcceptance = false;
    public boolean forcesAcceptance() { return this.forceAcceptance || this.isServerEntry(); }
    public void setForceAcceptance(boolean isAdmin) { if(this.isServerEntry()) return; this.forceAcceptance = isAdmin; this.markAdminStateDirty(); }
    private boolean infiniteRange = false;
    public boolean isInfiniteRange() { return this.infiniteRange || this.isServerEntry(); }
    public void setInfiniteRange(boolean infiniteRange) { if(this.isServerEntry()) return; this.infiniteRange = infiniteRange; this.markAdminStateDirty(); }

    protected final void markDirty(CompoundTag packet) { if(this.locked || this.isClient) return; TaxSaveData.MarkTaxEntryDirty(this.id, packet); }
    protected final void markDirty(Function<CompoundTag,CompoundTag> packet) { this.markDirty(packet.apply(new CompoundTag()));}

    public TaxEntry() { }
    public TaxEntry(long id, @Nullable BlockEntity core, @Nullable Player owner)
    {
        this.id = id;
        if(core != null)
            this.center = WorldPosition.ofBE(core);
        this.owner.SetOwner(owner);
    }

    public final void openMenu(@Nonnull Player player, @Nonnull MenuValidator validator)
    {
        if(player instanceof ServerPlayer sp && this.canAccess(player))
            NetworkHooks.openScreen(sp, new TaxCollectorMenuProvider(this.id, validator), EasyMenu.encoder(d -> d.writeLong(this.id), validator));
    }

    public CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        tag.putLong("ID", this.id);

        this.saveTaxRate(tag);
        this.saveStoredMoney(tag);
        this.saveActiveState(tag);
        this.saveName(tag);
        this.saveNotifications(tag);
        this.saveStats(tag);

        if(!this.isServerEntry())
        {
            //This data is not needed for a Server Entry.
            this.saveCenter(tag);
            this.saveArea(tag);
            this.saveRenderMode(tag);
            this.saveOwner(tag);
            this.saveAdminState(tag);
            this.saveAcceptedEntries(tag);
            this.saveBankState(tag);
        }

        return tag;
    }

    public final void markAreaDirty() { this.markDirty(this::saveArea);}

    protected final CompoundTag saveArea(CompoundTag tag) {
        tag.putInt("HorizRadius", this.radius);
        tag.putInt("VertSize", this.height);
        tag.putInt("VertOffset", this.vertOffset);
        return tag;
    }

    public final void markRenderModeDirty() { this.markDirty(this::saveRenderMode); }

    protected final CompoundTag saveRenderMode(CompoundTag tag) {
        tag.putInt("RenderMode", this.renderMode);
        return tag;
    }

    public final void markCenterDirty() { this.markDirty(this::saveCenter); }

    protected final CompoundTag saveCenter(CompoundTag tag) {
        tag.put("Center", this.center.save());
        return tag;
    }

    public final void markTaxPercentageDirty() { this.markDirty(this::saveTaxRate); }
    protected final CompoundTag saveTaxRate(CompoundTag tag) {
        tag.putInt("TaxRate", this.taxRate);
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

    public final void markStoredMoneyDirty() { this.markDirty(this::saveStoredMoney); }
    protected final CompoundTag saveStoredMoney(CompoundTag tag) {
        tag.put("StoredMoney", this.storedMoney.save());
        return tag;
    }

    public final void markAdminStateDirty() { this.markDirty(this::saveAdminState); }

    protected final CompoundTag saveAdminState(CompoundTag tag) {
        tag.putBoolean("ForceAcceptance", this.forceAcceptance);
        tag.putBoolean("IsInfiniteRange", this.infiniteRange);
        return tag;
    }

    public final void markActiveStateDirty() { this.markDirty(this::saveActiveState); }
    protected final CompoundTag saveActiveState(CompoundTag tag) {
        tag.putBoolean("IsActivated", this.active);
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

    public final void markNotificationsDirty() { this.markDirty(this::saveNotifications); }
    protected final CompoundTag saveNotifications(CompoundTag tag)
    {
        tag.put("Notifications", this.logger.save());
        return tag;
    }

    public final void markStatsDirty() { this.markDirty(this::saveStats); }
    protected final CompoundTag saveStats(CompoundTag tag)
    {
        tag.put("Statistics", this.stats.save());
        return tag;
    }

    public final void markBankStateDirty() { this.markDirty(this::saveBankState); }
    protected final CompoundTag saveBankState(CompoundTag tag)
    {
        tag.putBoolean("LinkedToBank", this.linkToBank);
        return tag;
    }

    public void load(CompoundTag tag)
    {
        if(tag.contains("ID"))
            this.id = tag.getLong("ID");
        if(tag.contains("Center"))
            this.center = WorldPosition.load(tag.getCompound("Center"));
        if(tag.contains("HorizRadius"))
            this.radius = tag.getInt("HorizRadius");
        if(tag.contains("VertSize"))
            this.height = tag.getInt("VertSize");
        if(tag.contains("VertOffset"))
            this.vertOffset = tag.getInt("VertOffset");
        if(tag.contains("RenderMode"))
            this.renderMode = tag.getInt("RenderMode");
        if(tag.contains("TaxRate"))
            this.taxRate = tag.getInt("TaxRate");
        if(tag.contains("CustomName"))
            this.name = tag.getString("CustomName");
        if(tag.contains("Owner"))
            this.owner.load(tag.getCompound("Owner"));
        if(tag.contains("StoredMoney"))
            this.storedMoney = CoinValue.load(tag.getCompound("StoredMoney"));
        if(tag.contains("ForceAcceptance"))
            this.forceAcceptance = tag.getBoolean("ForceAcceptance");
        if(tag.contains("IsInfiniteRange"))
            this.infiniteRange = tag.getBoolean("IsInfiniteRange");
        if(tag.contains("IsActivated"))
            this.active = tag.getBoolean("IsActivated");
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
        if(tag.contains("Notifications"))
            this.logger.load(tag.getCompound("Notifications"));
        if(tag.contains("Statistics"))
            this.stats.load(tag.getCompound("Statistics"));
    }

}
