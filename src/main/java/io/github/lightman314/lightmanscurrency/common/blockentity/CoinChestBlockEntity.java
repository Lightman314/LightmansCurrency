package io.github.lightman314.lightmanscurrency.common.blockentity;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTicker;
import io.github.lightman314.lightmanscurrency.api.misc.IServerTicker;
import io.github.lightman314.lightmanscurrency.api.misc.blockentity.EasyBlockEntity;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyViewer;
import io.github.lightman314.lightmanscurrency.api.upgrades.IUpgradeable;
import io.github.lightman314.lightmanscurrency.api.upgrades.IUpgradeableBlockEntity;
import io.github.lightman314.lightmanscurrency.common.capability.CurrencyCapabilities;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.menus.CoinChestMenu;
import io.github.lightman314.lightmanscurrency.common.menus.containers.CoinContainer;
import io.github.lightman314.lightmanscurrency.common.menus.containers.UpgradeContainer;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestUpgradeData;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CoinChestBlockEntity extends EasyBlockEntity implements IUpgradeable, IClientTicker, IServerTicker, LidBlockEntity, IUpgradeableBlockEntity {

    private final ChestLidController chestLidController = new ChestLidController();
    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        protected void onOpen(Level level, BlockPos pos, @Nonnull BlockState state) {
            level.playSound(null, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }
        protected void onClose(Level level, BlockPos pos, @Nonnull BlockState state) {
            level.playSound(null, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }
        protected void openerCountChanged(Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, int oldCount, int newCount) {
            level.blockEvent(CoinChestBlockEntity.this.worldPosition, ModBlocks.COIN_CHEST.get(), 1, newCount);
        }
        protected boolean isOwnContainer(Player player) { return player.containerMenu instanceof CoinChestMenu ccm && ccm.be == CoinChestBlockEntity.this; }
    };

    public int getOpenerCount() { return this.openersCounter.getOpenerCount(); }

    public static final int STORAGE_ROWS = 3;
    public static final int STORAGE_SIZE = 9 * STORAGE_ROWS;
    public static final int UPGRADE_SIZE = 3;

    private final ItemHandler handler = new ItemHandler(this);

    private Component customName = null;
    public void setCustomName(Component name) { this.customName = name; this.markCustomNameDirty(); }
    public Component getDisplayName() { return this.customName != null ? this.customName : LCText.BLOCK_MONEY_CHEST.get(); }

    private CoinContainer storage;
    public final CoinContainer getStorage() { return this.storage; }
    private final UpgradeContainer upgrades;
    @Nonnull
    public final UpgradeContainer getUpgrades() { return this.upgrades; }

    private final IMoneyViewer moneyViewer = MoneyAPI.getApi().GetContainersMoneyHandler(this.storage,s -> {},this);

    private List<CoinChestUpgradeData> unfilteredUpgradeDataCache = new ArrayList<>();
    private List<CoinChestUpgradeData> upgradeDataCache = new ArrayList<>();

    private boolean allowEvents = true;

    public final ImmutableList<CoinChestUpgradeData> getChestUpgrades()
    {
        this.checkUpgradeCache();
        return ImmutableList.copyOf(this.upgradeDataCache);
    }
    public final ImmutableList<CoinChestUpgradeData> getActiveUpgrades() {
        return ImmutableList.copyOf(this.getChestUpgrades().stream().filter(CoinChestUpgradeData::isActive).collect(Collectors.toList()));
    }

    private void checkUpgradeCache()
    {
        if(this.unfilteredUpgradeDataCache.size() != UPGRADE_SIZE)
            this.refreshUpgradeCache();
    }
    private void refreshUpgradeCache()
    {
        List<CoinChestUpgradeData> oldList = this.unfilteredUpgradeDataCache;
        this.unfilteredUpgradeDataCache = new ArrayList<>();
        for(int i = 0; i < UPGRADE_SIZE; ++i)
        {
            this.unfilteredUpgradeDataCache.add(CoinChestUpgradeData.forItem(this.upgrades.getItem(i), i, this::markUpgradesChanged));
            //Copy over old tick timers etc. if the upgrade type is the same as the one previously in that slot
            if(i < oldList.size())
                this.unfilteredUpgradeDataCache.get(i).copyRelevantData(oldList.get(i));
        }
        this.upgradeDataCache = this.unfilteredUpgradeDataCache.stream().filter(CoinChestUpgradeData::notNull).toList();
    }

    @Nonnull
    public final CoinChestUpgradeData getChestUpgradeForSlot(int slot)
    {
        this.checkUpgradeCache();
        List<CoinChestUpgradeData> list = this.unfilteredUpgradeDataCache;
        if(slot >= 0 && slot < list.size())
            return list.get(slot);
        return CoinChestUpgradeData.NULL;
    }

    @Nullable
    public final CoinChestUpgradeData getChestUpgradeOfType(CoinChestUpgrade type)
    {
        for(CoinChestUpgradeData data : this.getChestUpgrades())
        {
            if(data.upgrade == type)
                return data;
        }
        return null;
    }

    public CoinChestBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.COIN_CHEST.get(), pos, state);
        this.storage = new CoinContainer(STORAGE_SIZE);
        this.storage.addListener(i -> this.markStorageDirty());
        this.upgrades = new UpgradeContainer(UPGRADE_SIZE,this);
        this.upgrades.addListener(i -> this.markUpgradesDirty());
    }

    @Override
    public void load(@Nonnull CompoundTag compound) {
        super.load(compound);
        if(compound.contains("Name"))
            this.customName = Component.Serializer.fromJson(compound.getString("Name"));
        if(compound.contains("Storage"))
        {
            this.storage = new CoinContainer(InventoryUtil.loadAllItems("Storage", compound, STORAGE_SIZE));
            this.storage.addListener(i -> this.markStorageDirty());
        }
        if(compound.contains("Upgrades"))
        {
            this.upgrades.load("Upgrades",compound);
            this.refreshUpgradeCache();
        }
    }

    public static MenuProvider getMenuProvider(CoinChestBlockEntity be) { return new CoinChestMenuProvider(be); }

    private record CoinChestMenuProvider(CoinChestBlockEntity be) implements MenuProvider {
        @Nonnull
        @Override
        public Component getDisplayName() { return this.be.getDisplayName(); }
        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int id, @Nonnull Inventory inventory, @Nonnull Player player) { return new CoinChestMenu(id, inventory, this.be); }
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag compound) {
        super.saveAdditional(compound);
        this.saveCustomName(compound);
        this.saveStorage(compound);
        this.saveUpgrades(compound);
    }

    protected CompoundTag saveCustomName(CompoundTag compound) {
        if(this.customName != null)
            compound.putString("Name", Component.Serializer.toJson(this.customName));
        return compound;
    }

    protected CompoundTag saveStorage(CompoundTag compound) {
        InventoryUtil.saveAllItems("Storage", compound, this.storage);
        return compound;
    }

    protected CompoundTag saveUpgrades(CompoundTag compound) {
        this.upgrades.save("Upgrades",compound);
        return compound;
    }

    public final void markCustomNameDirty()
    {
        this.setChanged();
        if(this.isServer())
            BlockEntityUtil.sendUpdatePacket(this, this.saveCustomName(new CompoundTag()));
    }

    public final void markStorageDirty()
    {
        this.setChanged();
        if(this.isServer())
        {
            if(this.allowEvents)
            {
                this.allowEvents = false;
                for(CoinChestUpgradeData data : this.getActiveUpgrades())
                {
                    try{ data.upgrade.OnStorageChanged(this, data);
                    } catch (Throwable t) { LightmansCurrency.LogError("Error on CoinChestUpgrade Storage Change listener!", t); }
                }
                this.allowEvents = true;
                //Don't send update packet until any additional event processing or
                BlockEntityUtil.sendUpdatePacket(this, this.saveStorage(new CompoundTag()));
            }
        }
    }

    public final void markUpgradesDirty()
    {
        this.refreshUpgradeCache();
        this.markUpgradesChanged();

    }

    /**
     * Simpler version of the upgrade dirty flag, for use when the upgrade changes itself,
     * meaning that the upgrade item/type is the same, it's nbt has simply been changed.
     * Main difference is that it does not flag the upgrade cache for re-assessment
     */
    private void markUpgradesChanged()
    {
        this.setChanged();
        if(this.isServer())
            BlockEntityUtil.sendUpdatePacket(this, this.saveUpgrades(new CompoundTag()));
    }

    public final void checkUpgradeEquipped(int slot)
    {
        if(this.isClient())
            return;
        CoinChestUpgradeData data = this.getChestUpgradeForSlot(slot);
        if(data != null)
        {
            try{
                data.upgrade.OnEquip(this, data);
            } catch(Throwable t) { LightmansCurrency.LogError("Error during CoinChestUpgrade's Equip listener!", t); }
        }

        //Send Refresh Message to all players with this opened
        for(int i = 0; i < this.relevantPlayers.size(); ++i)
        {
            Player player = this.relevantPlayers.get(i);
            if(player.containerMenu instanceof CoinChestMenu menu && menu.be == this)
                menu.SendMessageToClient(LazyPacketData.builder().setBoolean("RefreshTabs", true));
            else
            {
                this.relevantPlayers.remove(i);
                i--;
            }
        }
    }

    @Override
    public void clientTick() { this.chestLidController.tickLid(); }

    @Override
    public void serverTick() {
        for(CoinChestUpgradeData data : this.getActiveUpgrades())
            data.tick(this);
    }

    @Override
    public boolean allowUpgrade(@Nonnull UpgradeType type) { return type instanceof CoinChestUpgrade upgrade; }

    public boolean allowAccess(@Nullable Player player)
    {
        if(LCAdminMode.isAdminPlayer(player))
            return true;
        for(CoinChestUpgradeData data : this.getActiveUpgrades())
        {
            if(data.upgrade.BlockAccess(this, data, player))
                return false;
        }
        return true;
    }

    public void onValidBlockRemoval()
    {
        for(CoinChestUpgradeData data : this.getActiveUpgrades())
            data.upgrade.OnValidBlockRemoval(this, data);
    }

    public void onBlockRemoval()
    {
        for(CoinChestUpgradeData data : this.getActiveUpgrades())
            data.upgrade.OnBlockRemoval(this, data);
    }

    @Override
    public @Nonnull <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER)
            return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, LazyOptional.of(() -> this.handler));
        if(cap == CurrencyCapabilities.MONEY_VIEWER)
            return CurrencyCapabilities.MONEY_VIEWER.orEmpty(cap,LazyOptional.of(() -> this.moneyViewer));
        return super.getCapability(cap, side);
    }

    @Override
    public float getOpenNess(float partial) { return this.chestLidController.getOpenness(partial); }

    private static class ItemHandler extends InvWrapper
    {
        private final CoinChestBlockEntity blockEntity;
        public ItemHandler(CoinChestBlockEntity blockEntity) { super(blockEntity.storage); this.blockEntity = blockEntity; }
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) { return CoinAPI.getApi().IsCoin(stack, false); }
        @Override
        public Container getInv() { return this.blockEntity.storage; }
    }

    @Override
    public boolean triggerEvent(int event, int value) {
        if (event == 1) {
            this.chestLidController.shouldBeOpen(value > 0);
            return true;
        } else {
            return super.triggerEvent(event, value);
        }
    }

    private final List<Player> relevantPlayers = new ArrayList<>();

    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
        if(!this.relevantPlayers.contains(player))
            this.relevantPlayers.add(player);
    }

    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
        this.relevantPlayers.remove(player);
    }

    public void recheckOpen() {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }



}
