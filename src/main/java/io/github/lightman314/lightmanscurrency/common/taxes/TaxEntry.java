package io.github.lightman314.lightmanscurrency.common.taxes;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.data.IRegistryAccess;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.builtin.MoneyStorage;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.FakeOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.PlayerOwner;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxCollector;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxable;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxableContext;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.common.data.types.TaxDataCache;
import io.github.lightman314.lightmanscurrency.common.menus.providers.TaxCollectorMenuProvider;
import io.github.lightman314.lightmanscurrency.common.menus.validation.EasyMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.api.taxes.notifications.categories.TaxEntryCategory;
import io.github.lightman314.lightmanscurrency.api.taxes.notifications.TaxesCollectedNotification;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerData;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.taxes.data.TaxStats;
import io.github.lightman314.lightmanscurrency.api.misc.world.WorldArea;
import io.github.lightman314.lightmanscurrency.api.misc.world.WorldPosition;
import io.github.lightman314.lightmanscurrency.api.taxes.reference.TaxableReference;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TaxEntry extends IRegistryAccess.Holder implements ITaxCollector {

    public static final long SERVER_TAX_ID = -9;

    public static int minRadius() { return 5; }
    public static int maxRadius() { return LCConfig.SERVER.taxCollectorMaxRadius.get(); }
    public static int minHeight() { return 2; }
    public static int maxHeight() { return LCConfig.SERVER.taxCollectorMaxHeight.get(); }
    public static int minVertOffset() { return -maxVertOffset(); }
    public static int maxVertOffset() { return LCConfig.SERVER.taxCollectorMaxVertOffset.get(); }

    public static int maxTaxRate() { return LCConfig.SERVER.taxCollectorMaxRate.get(); }

    public final TaxStats stats = new TaxStats(this);
    public void setStatsChanged() {
        this.setChanged(builder -> builder
                .setTag("Stats",this.stats.save(DataContext.createNBT(this.registryAccess()))));
    }

    private boolean locked = true;
    public final TaxEntry unlock() { this.locked = false; return this; }

    private boolean isClient = false;
    public final boolean isClient() { return this.isClient; }
    public final TaxEntry flagAsClient() { this.isClient = true; this.logger.flagAsClient(); return this.unlock(); }

    public final boolean isServerEntry() { return this.id == SERVER_TAX_ID; }

    private WorldPosition center = WorldPosition.VOID;
    public WorldPosition getCenter() { return this.center; }
    public void moveCenter(WorldPosition newPosition) {
        if(this.center.equals(newPosition))
            return;
        this.center = newPosition;
        this.setChanged(builder -> builder.setCustom("Center",this.center,ModLazyPackets.WORLD_POS));
    }
    @Override
    public WorldArea getArea() { return this.isInfiniteRange() ? WorldArea.ofInfiniteRange(this.center) : this.center.getArea(this.getRadius(), this.getHeight(), this.getVertOffset()); }

    private int radius = 10;
    public int getRadius() { return MathUtil.clamp(this.radius, minRadius(), maxRadius()); }
    public void setRadius(int newRadius) {
        if(this.isInfiniteRange())
            return;
        newRadius = MathUtil.clamp(newRadius, minRadius(), maxRadius());
        if(this.radius != newRadius)
        {
            this.radius = newRadius;
            this.setChanged(builder -> builder.setInt("Radius",this.radius));
        }
    }
    private int height = 10;
    public int getHeight() { return MathUtil.clamp(this.height, minHeight(), maxHeight()); }
    public void setHeight(int newHeight) {
        if(this.isInfiniteRange())
            return;
        newHeight = MathUtil.clamp(newHeight, minHeight(), maxHeight());
        if(this.height != newHeight)
        {
            this.height = newHeight;
            this.setChanged(builder -> builder.setInt("Height",this.height));
        }
    }
    private int vertOffset = 0;
    public int getVertOffset() { return MathUtil.clamp(this.vertOffset, minVertOffset(), maxVertOffset()); }
    public void setVertOffset(int newVertOffset) {
        if(this.isInfiniteRange())
            return;
        newVertOffset = MathUtil.clamp(newVertOffset, minVertOffset(), maxVertOffset());
        if(this.vertOffset != newVertOffset)
        {
            this.vertOffset = newVertOffset;
            this.setChanged(builder -> builder.setInt("VertOffset",this.vertOffset));
        }
    }

    /**
     * Render Modes:
     * 0- Don't draw anything
     * 1- Draw area to members only
     * 2- Draw area to all players
     */
    private int renderMode = 1;
    public int getRenderMode() { if(this.isInfiniteRange()) return 0; return this.renderMode; }
    public void setRenderMode(int newRenderMode) {
        newRenderMode = newRenderMode % 3;
        if(this.renderMode != newRenderMode)
        {
            this.renderMode = newRenderMode;
            this.setChanged(builder -> builder.setInt("RenderMode",this.renderMode));
        }
    }
    public boolean shouldRender(Player player)
    {
        //Don't renderBG the area if there is no area to draw (because infinite range)
        if(player == null || this.isInfiniteRange())
            return false;
        if(LCAdminMode.isAdminPlayer(player))
            return true;
        if(this.getRenderMode() == 1)
            return this.canAccess(player);
        //Don't renderBG for non-members if not active
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
    public int getTaxRate() { return MathUtil.clamp(this.taxRate, 0, maxTaxRate()); }
    public void setTaxRate(int newPercentage) {
        newPercentage = MathUtil.clamp(newPercentage, 1, maxTaxRate());
        if(this.taxRate != newPercentage)
        {
            this.taxRate = newPercentage;
            this.setChanged(builder -> builder.setInt("TaxRate",this.taxRate));
        }
    }

    private String name = "";
    public boolean hasCustomName() { return !this.name.isBlank(); }
    public String getCustomName() { return this.name; }
    @Override
    
    public MutableComponent getName() { if(this.name.isBlank()) return this.getDefaultName(); return EasyText.literal(this.name); }
    public void setName(String name) {
        if(this.name.equals(name))
            return;
        this.name = name;
        this.setChanged(builder -> builder.setString("Name",this.name));
    }
    protected MutableComponent getDefaultName() { return LCText.GUI_TAX_COLLECTOR_DEFAULT_NAME.get(this.isServerEntry() ? LCText.GUI_TAX_COLLECTOR_DEFAULT_NAME_SERVER.get() : this.owner.getName()); }

    private final OwnerData owner = new OwnerData(this,this::setOwnerChanged);
    @Override
    public OwnerData getOwner() { return this.owner; }
    public final boolean canAccess(Player player) { if(this.isServerEntry()) return player.hasPermissions(2); return this.owner.isMember(player); }

    private void setOwnerChanged() {
        this.setChanged(builder -> builder.setCustom("Owner",this.owner,ModLazyPackets.OWNER_DATA));
    }

    //Stored Money
    private final MoneyStorage storedMoney = new MoneyStorage().withListener(this::setStoredMoneyChanged);
    public MoneyStorage getStoredMoney() { return this.storedMoney; }
    public void depositMoney(MoneyValue amount) {
        IBankAccount account = this.getBankAccount();
        if(account != null)
        {
            account.depositMoney(amount);
            account.LogInteraction(this.getName(),amount,true);
            return;
        }
        this.storedMoney.addValue(amount);
    }
    public void clearStoredMoney() { this.storedMoney.clear(); }
    private void setStoredMoneyChanged() {
        this.setChanged(builder -> builder.setList("StoredMoney",this.storedMoney.allValues(),ModLazyPackets.MONEY_VALUE));
    }

    public final MoneyValue CalculateAndPayTaxes(ITaxable taxable, MoneyValue taxableAmount)
    {
        MoneyValue amountToPay = taxableAmount.percentageOfValue(this.getTaxRate());
        if(!amountToPay.isEmpty())
        {
            this.depositMoney(amountToPay);
            this.PushNotification(TaxesCollectedNotification.create(taxable.getName(), amountToPay, new TaxEntryCategory(this.getName(), this.id)));
            this.stats.OnTaxesCollected(taxable, amountToPay);
        }
        return amountToPay;
    }

    public final void PayTaxesDirectly(@Nullable ITaxable taxable, MoneyValue taxes)
    {
        if(!taxes.isEmpty())
        {
            this.depositMoney(taxes);
            if(taxable != null)
            {
                this.PushNotification(TaxesCollectedNotification.create(taxable.getName(),taxes,new TaxEntryCategory(this.getName(),this.id)));
                this.stats.OnTaxesCollected(taxable,taxes);
            }
        }
    }

    //Linked Bank Account?
    private boolean linkToBank = false;
    public void setLinkedToBank(boolean newState) {
        if(this.linkToBank != newState)
        {
            this.linkToBank = newState;
            this.setChanged(builder -> builder.setBoolean("BankLink",this.linkToBank));
        }
    }
    public boolean isLinkedToBank() { return this.linkToBank && !this.isServerEntry(); }
    @Nullable
    public final IBankAccount getBankAccount()
    {
        if(!this.isLinkedToBank())
            return null;
        BankReference reference = this.owner.getValidOwner().asBankReference();
        if(reference != null)
            return reference.get();
        return null;
    }

    private final NotificationData logger = new NotificationData();
    public final List<Notification> getNotifications() { return this.logger.getNotifications(); }

    public final void PushNotification(Supplier<Notification> notification) {
        if(this.isClient)
            return;

        this.logger.addNotification(notification.get());
        this.setChanged(builder -> builder.addToList("AddNotification",notification.get(),ModLazyPackets.NOTIFICATION));
    }

    //Accepted Entries
    //Traders will automatically accept when placed, but pre-existing traders will need to accept manually
    //Ignored if this is an Admin Tax
    private final List<TaxableReference> acceptedEntries = new ArrayList<>();
    public final List<TaxableReference> getAcceptedEntries() { return ImmutableList.copyOf(this.acceptedEntries); }
    public final void AcceptTaxable(ITaxable entry) {
        TaxableReference reference = entry.getReference();
        if(!this.acceptedEntries.contains(reference) && reference != null)
        {
            this.acceptedEntries.add(reference);
            this.setChanged(builder -> builder.setList("AcceptedEntries",this.acceptedEntries,ModLazyPackets.TAXABLE_REFERENCE));
        }
    }
    public final void TaxableWasRemoved(ITaxable entry)
    {
        TaxableReference reference = entry.getReference();
        if(this.acceptedEntries.contains(reference))
        {
            this.acceptedEntries.remove(reference);
            this.setChanged(builder -> builder.setList("AcceptedEntries",this.acceptedEntries,ModLazyPackets.TAXABLE_REFERENCE));
        }
    }

    //Whether this Tax Entry applies to a trader at the given position
    public boolean ShouldTax(ITaxableContext context) { return this.IsInArea(context.taxable()) && this.testNetworkTaxable(context) && (this.forcesAcceptance() || this.acceptedEntries.contains(context.taxable().getReference())); }

    public boolean IsInArea(ITaxable taxable) {
        return this.isActive() && this.getArea().isInArea(taxable.getWorldPosition());
    }

    private boolean testNetworkTaxable(ITaxableContext context)
    {
        if(this.isServerEntry() && this.onlyTargetNetwork)
            return context.networkAccess();
        return true;
    }

    private long id = -1;
    public long getID() { return this.id; }
    private boolean active = false;
    public boolean isActive() { return this.active; }
    public void setActive(boolean newState, @Nullable Player player) {
        if(this.active == newState)
            return;
        if(LCConfig.SERVER.taxCollectorAdminOnly.get() && !LCAdminMode.isAdminPlayer(player) && !this.active)
        {
            Permissions.PermissionWarning(player, "activate a tax entry", Permissions.ADMIN_MODE);
            return;
        }
        this.active = newState;
        this.setChanged(builder -> builder.setBoolean("Active",this.active));
    }
    private boolean forceAcceptance = false;
    public boolean forcesAcceptance() { return this.forceAcceptance || this.isServerEntry(); }
    public void setForceAcceptance(boolean forceAcceptance) {
        if(this.isServerEntry() || this.forceAcceptance == forceAcceptance)
            return;
        this.forceAcceptance = forceAcceptance;
        this.setChanged(builder -> builder.setBoolean("ForceAcceptance",this.forceAcceptance));
    }
    private boolean infiniteRange = false;
    public boolean isInfiniteRange() { return this.infiniteRange || this.isServerEntry(); }
    public void setInfiniteRange(boolean infiniteRange) {
        if(this.isServerEntry() || this.infiniteRange == infiniteRange)
            return;
        this.infiniteRange = infiniteRange;
        this.setChanged(builder -> builder.setBoolean("InfiniteRange",this.infiniteRange));
    }
    private boolean onlyTargetNetwork = false;
    public boolean isOnlyTargetingNetwork() { return this.isServerEntry() && this.onlyTargetNetwork; }
    public void setOnlyTargetingNetwork(boolean newValue) {
        if(this.isServerEntry() && this.onlyTargetNetwork != newValue)
        {
            this.onlyTargetNetwork = newValue;
            this.setChanged(builder -> builder.setBoolean("OnlyNetwork",this.onlyTargetNetwork));
        }
    }

    @Nullable
    private LazyPacketData.Builder changedData = null;

    protected final void setChanged(Consumer<LazyPacketData.Builder> dataWriter) {
        if(this.locked || this.isClient)
            return;
        TaxDataCache.TYPE.get(false).setEntryChanged(this.id);
        if(this.changedData != null)
            this.changedData = this.builder();
    }
    public LazyPacketData clean() {
        LazyPacketData result = this.changedData == null ? this.builder().build() : this.changedData.build();
        this.changedData = null;
        return result;
    }

    public TaxEntry() { }
    public TaxEntry(long id, @Nullable BlockEntity core, @Nullable Player owner)
    {
        this.id = id;
        if(core != null)
            this.center = WorldPosition.ofBE(core);
        this.owner.SetOwner(owner != null ? PlayerOwner.of(owner) : FakeOwner.of("NULL"));
    }

    public final void openMenu(Player player, MenuValidator validator)
    {
        if(this.canAccess(player))
            player.openMenu(new TaxCollectorMenuProvider(this.id, validator), EasyMenu.encoder(d -> d.writeLong(this.id), validator));
    }

    public CompoundTag save(DataContext<Tag> context)
    {
        CompoundTag tag = new CompoundTag();
        tag.putLong("ID", this.id);

        this.saveTaxRate(tag);
        this.saveStoredMoney(tag,context);
        this.saveActiveState(tag);
        this.saveName(tag);
        this.saveNotifications(tag,context);
        this.saveStats(tag,context);

        if(!this.isServerEntry())
        {
            //This data is not needed for a Server Entry.
            this.saveCenter(tag);
            this.saveArea(tag);
            this.saveRenderMode(tag);
            this.saveOwner(tag,context);
            this.saveAdminState(tag);
            this.saveAcceptedEntries(tag);
            this.saveBankState(tag);
        }
        else //This data is exclusive to the server entry
            this.saveServerOptions(tag);

        return tag;
    }

    protected final void saveArea(CompoundTag tag) {
        tag.putInt("HorizRadius", this.radius);
        tag.putInt("VertSize", this.height);
        tag.putInt("VertOffset", this.vertOffset);
    }

    protected final void saveRenderMode(CompoundTag tag) {
        tag.putInt("RenderMode", this.renderMode);
    }

    protected final void saveCenter(CompoundTag tag) {
        tag.put("Center", this.center.save());
    }

    protected final void saveTaxRate(CompoundTag tag) {
        tag.putInt("TaxRate", this.taxRate);
    }

    protected final void saveName(CompoundTag tag) {
        tag.putString("CustomName", this.name);
    }

    protected final void saveOwner(CompoundTag tag,DataContext<Tag> context) {
        tag.put("Owner",this.owner.save(context));
    }

    protected final void saveStoredMoney(CompoundTag tag,DataContext<Tag> context) {
        tag.put("StoredMoney", this.storedMoney.save());
    }

    protected final void saveAdminState(CompoundTag tag) {
        tag.putBoolean("ForceAcceptance",this.forceAcceptance);
        tag.putBoolean("IsInfiniteRange",this.infiniteRange);
    }

    protected final void saveActiveState(CompoundTag tag) {
        tag.putBoolean("IsActivated", this.active);
    }

    protected final void saveAcceptedEntries(CompoundTag tag)
    {
        ListTag acceptedEntriesList = new ListTag();
        for(TaxableReference entry : this.acceptedEntries)
            acceptedEntriesList.add(entry.save());
        tag.put("AcceptedEntries",acceptedEntriesList);
    }

    protected final void saveNotifications(CompoundTag tag,DataContext<Tag> context)
    {
        tag.put("Notifications",context.write(this.logger,NotificationData.CODEC));
    }

    protected final void saveStats(CompoundTag tag,DataContext<Tag> context)
    {
        tag.put("Statistics",this.stats.save(context));
    }

    protected final void saveBankState(CompoundTag tag)
    {
        tag.putBoolean("LinkedToBank", this.linkToBank);
    }

    protected final void saveServerOptions(CompoundTag tag)
    {
        tag.putBoolean("OnlyTargetNetwork",this.onlyTargetNetwork);
    }

    public void load(CompoundTag tag,DataContext<Tag> context)
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
            this.owner.copyFrom(context.read(tag.get("Owner"),OwnerData.CODEC));
        if(tag.contains("StoredMoney"))
            this.storedMoney.load(context.read(tag.get("StoredMoney"),MoneyStorage.CODEC).allValues());
        if(tag.contains("ForceAcceptance"))
            this.forceAcceptance = tag.getBoolean("ForceAcceptance");
        if(tag.contains("IsInfiniteRange"))
            this.infiniteRange = tag.getBoolean("IsInfiniteRange");
        if(tag.contains("OnlyTargetNetwork"))
            this.onlyTargetNetwork = tag.getBoolean("OnlyTargetNetwork");
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
            this.logger.copyFrom(context.read(tag.get("Notifications"),NotificationData.CODEC));
        if(tag.contains("Statistics"))
            this.stats.load(tag.getCompound("Statistics"),context);
        if(tag.contains("LinkedToBank"))
            this.linkToBank = tag.getBoolean("LinkedToBank");
    }

    public void handleSyncPacket(LazyPacketData data)
    {
        if(data.contains("Stats"))
            this.stats.load(data.getTag("Stats"),DataContext.createNBT(this.registryAccess()));
        if(data.contains("Center"))
            this.center = data.getCustom("Center",ModLazyPackets.WORLD_POS,this.center);
        if(data.contains("Radius"))
            this.radius = data.getInt("Radius");
        if(data.contains("Height"))
            this.height = data.getInt("Height");
        if(data.contains("VertOffset"))
            this.vertOffset = data.getInt("VertOffset");
        if(data.contains("RenderMode"))
            this.renderMode = data.getInt("RenderMode");
        if(data.contains("TaxRate"))
            this.taxRate = data.getInt("TaxRate");
        if(data.contains("Name"))
            this.name = data.getString("Name");
        if(data.contains("Owner"))
            this.owner.copyFrom(data.getCustom("Owner",ModLazyPackets.OWNER_DATA,this.owner));
        if(data.contains("StoredMoney"))
            this.storedMoney.load(data.getList("StoredMoney",ModLazyPackets.MONEY_VALUE));
        if(data.contains("BankLink"))
            this.linkToBank = data.getBoolean("BankLink");
        if(data.contains("AddNotification"))
        {
            for(Notification n : data.getList("AddNotification",ModLazyPackets.NOTIFICATION))
                this.logger.addNotification(n);
        }
        if(data.contains("AcceptedEntries"))
        {
            this.acceptedEntries.clear();
            this.acceptedEntries.addAll(data.getList("AcceptedEntries",ModLazyPackets.TAXABLE_REFERENCE));
        }
        if(data.contains("Active"))
            this.active = data.getBoolean("Active");
        if(data.contains("ForceAcceptance"))
            this.forceAcceptance = data.getBoolean("ForceAcceptance");
        if(data.contains("InfiniteRange"))
            this.infiniteRange = data.getBoolean("InfiniteRange");
        if(data.contains("OnlyNetwork"))
            this.onlyTargetNetwork = data.getBoolean("OnlyNetwork");
    }

}
