package io.github.lightman314.lightmanscurrency.features.wallet.menu;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Function3;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.MathHelper;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.world.menu.MessageMenu;
import io.github.lightman314.lightmanscurrency.api.world.menu.slots.EasyResourceSlot;
import io.github.lightman314.lightmanscurrency.api.world.menu.slots.VanillaDisplaySlot;
import io.github.lightman314.lightmanscurrency.core.neoforge.LCDataAttachments;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletItem;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletStorage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractWalletMenu extends MessageMenu implements ItemAccess {

    public static final TextEntry TOOLTIP_WALLET_EXCHANGE = TextEntry.tooltip(LCApi.MODID,"wallet.exchange");
    public static final TextEntry TOOLTIP_WALLET_AUTO_EXCHANGE_ENABLE = TextEntry.tooltip(LCApi.MODID,"wallet.auto_exchange.enable");
    public static final TextEntry TOOLTIP_WALLET_AUTO_EXCHANGE_DISABLE = TextEntry.tooltip(LCApi.MODID,"wallet.auto_exchange.disable");
    public static final TextEntry TOOLTIP_WALLET_FORCE_DEFAULT_SOUND_ENABLE = TextEntry.tooltip(LCApi.MODID,"wallet.default_sounds.enable");
    public static final TextEntry TOOLTIP_WALLET_FORCE_DEFAULT_SOUND_DISABLE = TextEntry.tooltip(LCApi.MODID,"wallet.default_sounds.disable");
    public static final TextEntry TOOLTIP_WALLET_OPEN_BANK = TextEntry.tooltip(LCApi.MODID,"wallet.open_bank");
    public static final TextEntry TOOLTIP_WALLET_OPEN_WALLET = TextEntry.tooltip(LCApi.MODID,"wallet.open_wallet");

    protected final int walletSlot;
    protected final Item walletItem;
    public final boolean hasWallet() {
        ItemStack wallet = this.getWallet();
        return !wallet.isEmpty() && WalletItem.isWallet(wallet);
    }
    public final ItemStack getWallet() {
        if(this.walletSlot < 0)
            return this.getPlayer().getData(LCDataAttachments.WALLET).getWallet();
        return this.getPlayer().getInventory().getItem(this.walletSlot);
    }
    private void setWallet(ItemStack wallet) {
        if(this.walletSlot < 0)
            this.getPlayer().getData(LCDataAttachments.WALLET).setWallet(wallet);
        else
            this.getPlayer().getInventory().setItem(this.walletSlot,wallet);
    }
    public boolean isEquippedWallet() { return this.walletSlot < 0; }

    private final WalletStorage walletStorage;
    public final WalletStorage getWalletStorage() { return this.walletStorage; }

    public final int coinSlotHeight;
    public final int coinSlotWidth;
    public final int bonusWidth;
    public final int halfBonusWidth;

    private final List<Slot> coinSlots = new ArrayList<>();
    public final List<Slot> getCoinSlots() { return ImmutableList.copyOf(this.coinSlots); }

    public AbstractWalletMenu(MenuType<?> type,int menuID,Player player,int walletSlot) {
        super(type,menuID,player);
        this.walletSlot = walletSlot;

        ItemStack wallet = this.getWallet();
        this.walletItem = wallet.getItem();

        int walletSize = WalletItem.getWalletSlots(wallet);
        this.walletStorage = new WalletStorage(this);
        this.coinSlotHeight = Math.min(MathHelper.divideAndRoundUp(walletSize,9),6);
        if(walletSize > 9 * 6)
        {
            this.coinSlotWidth = MathHelper.divideAndRoundUp(walletSize,6);
            this.bonusWidth = 18 * (this.coinSlotWidth - 9);
            this.halfBonusWidth = this.bonusWidth / 2;
        }
        else
        {
            this.coinSlotWidth = 9;
            this.bonusWidth = this.halfBonusWidth = 0;
        }
    }


    protected final void addInventoryHotbarSlots(Container inventory, int left, int top) {
        for (int x = 0; x < 9; x++) {
            this.addInventorySlot(inventory, x, left + x * 18, top);
        }
    }

    protected void addInventoryExtendedSlots(Container inventory, int left, int top) {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                this.addInventorySlot(inventory, x + (y + 1) * 9, left + x * 18, top + y * 18);
            }
        }
    }

    protected void addStandardInventorySlots(Container container,int left,int top) {
        this.addInventoryExtendedSlots(container, left, top);
        this.addInventoryHotbarSlots(container, left, top + 58);
    }

    protected final void addInventorySlot(Container inventory,int index,int x, int y) {
        if(index == this.walletSlot)
            this.addSlot(new VanillaDisplaySlot(inventory,index,x,y));
        else
            this.addSlot(new Slot(inventory,index,x,y));
    }

    protected final void addCoinSlots(int yPos) {
        if(!this.coinSlots.isEmpty())
            return;
        Identifier background = LCApi.id("container/slot/money");
        int dummySlots = WalletItem.MAX_WALLET_SLOTS - this.walletStorage.size();
        int index = 0;
        for(int y = 0; y < this.coinSlotHeight; ++y)
        {
            int xOff;
            if(y == this.coinSlotHeight - 1)
            {
                int emptySlots = this.coinSlotWidth - (this.walletStorage.size() - index);
                xOff = Math.max(0,emptySlots * 9);
            }
            else
                xOff = 0;
            for(int x = 0; x < this.coinSlotWidth && index < this.walletStorage.size(); ++x)
            {
                Slot slot = new EasyResourceSlot(this.walletStorage,index++,xOff + 8 + (x * 18),yPos + (y * 18));
                slot.setBackground(background);
                this.addSlot(slot);
                this.coinSlots.add(slot);
            }
        }
        if(dummySlots < 0)
            LightmansCurrency.LogWarning("Coin Slot count is larger than expected limit!");
        //Add dummy slots so that server-client desync can't crash the game
        if(dummySlots > 0)
        {
            Container dummyContainer = new SimpleContainer(1);
            while(dummySlots-- > 0)
            {
                VanillaDisplaySlot slot = new VanillaDisplaySlot(dummyContainer,0,Integer.MAX_VALUE / 2,Integer.MAX_VALUE / 2);
                slot.setActive(false);
                this.addSlot(slot);
            }
        }
    }

    @Override
    public boolean stillValid(Player player) { return this.getWallet().getItem() == this.walletItem; }

    public final void exchangeCoins(@Nullable TransactionContext transaction) {
        try(Transaction tx = Transaction.open(transaction)) {
            LCApi.getCoinAPI().exchangeCoinsAllUp(this.walletStorage,tx);
            LCApi.getCoinAPI().sortCoinsByValue(this.walletStorage,tx);
            tx.commit();
        }
    }

    public final ItemStack pickupCoins(ItemStack coin,@Nullable TransactionContext transaction) {
        try(Transaction tx = Transaction.open(transaction)) {
            ItemStack copy = coin.copy();
            int inserted = this.walletStorage.insert(ItemResource.of(copy),copy.getCount(),transaction);
            if(WalletItem.shouldAutoExchange(this.getWallet()))
                this.exchangeCoins(tx);
            tx.commit();
            copy.shrink(inserted);
            return copy.isEmpty() ? ItemStack.EMPTY : copy;
        }
    }

    private final Snapshots snapshots = new Snapshots();
    @Override
    public ItemResource getResource() { return ItemResource.of(this.snapshots.getActiveWallet()); }
    @Override
    public int getAmount() { return this.snapshots.getActiveWallet().getCount(); }
    @Override
    public int insert(ItemResource resource,int amount,TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        if(this.snapshots.getActiveWallet().isEmpty() && WalletItem.isWallet(resource))
        {
            this.snapshots.updateSnapshots(transaction);
            this.snapshots.setActiveWallet(resource.toStack());
            return 1;
        }
        return 0;
    }
    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        ItemStack stack = this.snapshots.getActiveWallet().copy();
        if(!stack.isEmpty() && resource.matches(stack))
        {
            this.snapshots.updateSnapshots(transaction);
            int taken = Math.min(amount,stack.getCount());
            stack.shrink(taken);
            this.snapshots.setActiveWallet(stack.isEmpty() ? ItemStack.EMPTY : stack);
            return taken;
        }
        return 0;
    }

    private final class Snapshots extends SnapshotJournal<Optional<ItemStack>> {

        private Optional<ItemStack> activeWallet = Optional.empty();
        private ItemStack getActiveWallet() { return this.activeWallet.orElseGet(AbstractWalletMenu.this::getWallet); }
        private void setActiveWallet(ItemStack stack) { this.activeWallet = Optional.of(stack); }

        @Override
        protected Optional<ItemStack> createSnapshot() { return this.activeWallet.isPresent() ? Optional.of(this.activeWallet.get().copy()) : this.activeWallet; }
        @Override
        protected void revertToSnapshot(Optional<ItemStack> snapshot) { this.activeWallet = snapshot; }
        @Override
        protected void onRootCommit(Optional<ItemStack> originalState) {
            if(this.activeWallet.isPresent())
            {
                setWallet(this.activeWallet.get().copy());
                this.activeWallet = Optional.empty();
            }
        }
    }

    public static MenuProvider buildProvider(Function3<Integer,Player,Integer,? extends AbstractWalletMenu> factory, int walletSlot) { return buildProvider(factory,walletSlot,true); }
    public static MenuProvider buildProvider(Function3<Integer,Player,Integer,? extends AbstractWalletMenu> factory, int walletSlot,boolean triggerScreenReset) {
        return new WalletMenuProvider(factory,walletSlot,triggerScreenReset);
    }

    private static class WalletMenuProvider implements MenuProvider {

        Function3<Integer,Player,Integer,? extends AbstractWalletMenu> factory;
        private final int walletSlot;
        private final boolean triggerScreenReset;
        private Component walletName = Component.empty();
        private WalletMenuProvider(Function3<Integer,Player,Integer,? extends AbstractWalletMenu> factory,int walletSlot,boolean triggerScreenReset) {
            this.factory = factory;
            this.walletSlot = walletSlot;
            this.triggerScreenReset = triggerScreenReset;
        }
        @Override
        public Component getDisplayName() { return this.walletName; }
        private void calculateName(Player player) {
            if(this.walletSlot < 0)
                this.walletName = player.getData(LCDataAttachments.WALLET).getWallet().getHoverName().plainCopy();
            else
                this.walletName = player.getInventory().getItem(this.walletSlot).getHoverName().plainCopy();
        }
        @Override
        @Nullable
        public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
            this.calculateName(player);
            return this.factory.apply(containerId,player,this.walletSlot);
        }
        @Override
        public boolean shouldTriggerClientSideContainerClosingOnOpen() { return this.triggerScreenReset; }
        @Override
        public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) { buffer.writeInt(this.walletSlot); }

    }


}