package io.github.lightman314.lightmanscurrency.common.playertrading;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.PlayerTradeMenu;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.playertrading.SMessageUpdatePlayerTrade;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerTrade implements IPlayerTrade, INamedContainerProvider {


    private boolean stillPending = true;
    public final long creationTime;
    public final int tradeID;
    private boolean completed = false;
    public boolean isCompleted() { return this.completed; }

    @Override
    public boolean isHost(@Nonnull PlayerEntity player) { return player.getUUID() == this.hostPlayerID; }
    public boolean isGuest(@Nonnull PlayerEntity player) { return player.getUUID() == this.guestPlayerID; }
    private final UUID hostPlayerID;
    @Override
    public ITextComponent getHostName() {
        ServerPlayerEntity hostPlayer = this.getPlayer(this.hostPlayerID);
        return hostPlayer == null ? EasyText.literal( "NULL") : hostPlayer.getName();
    }
    private final UUID guestPlayerID;
    @Override
    public ITextComponent getGuestName() {
        ServerPlayerEntity guestPlayer = this.getPlayer(this.guestPlayerID);
        return guestPlayer == null ? EasyText.literal("NULL") : guestPlayer.getName();
    }

    private boolean playerMissing(@Nonnull UUID playerID) { return getPlayer(playerID) == null; }

    private ServerPlayerEntity getPlayer(UUID playerID) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server != null)
            return server.getPlayerList().getPlayer(playerID);
        return null;
    }

    private final CoinValue hostMoney = new CoinValue();
    @Override
    public CoinValue getHostMoney() { return this.hostMoney.copy(); }
    private final CoinValue guestMoney = new CoinValue();
    @Override
    public CoinValue getGuestMoney() { return this.guestMoney.copy(); }

    private final Inventory hostItems = new Inventory(IPlayerTrade.ITEM_COUNT);
    @Override
    public IInventory getHostItems() { return this.hostItems; }
    private final Inventory guestItems = new Inventory(IPlayerTrade.ITEM_COUNT);
    @Override
    public IInventory getGuestItems() { return this.guestItems; }

    private int hostState = 0;
    @Override
    public int getHostState() { return this.hostState; }
    private int guestState = 0;
    @Override
    public int getGuestState() { return this.guestState; }

    public PlayerTrade(ServerPlayerEntity host, ServerPlayerEntity guest, int tradeID) {
        this.hostPlayerID = host.getUUID();
        this.guestPlayerID = guest.getUUID();
        this.tradeID = tradeID;
        this.creationTime = TimeUtil.getCurrentTime();
        this.hostItems.addListener(this::onContainerChange);
        this.guestItems.addListener(this::onContainerChange);
    }

    private void onContainerChange(IInventory container) {
        if(this.hostItems == container)
            this.onTradeEdit(true);
        if(this.guestItems == container)
            this.onTradeEdit(false);
        this.markDirty();
    }

    private void onTradeEdit(boolean wasHost)
    {
        if(wasHost)
        {
            this.guestState = 0;
            this.hostState = Math.min(this.hostState, 1);
        }
        else
        {
            this.hostState = 0;
            this.guestState = Math.min(this.guestState, 1);
        }
    }

    private boolean playerAbandonedTrade(boolean host) {
        ServerPlayerEntity player = this.getPlayer(host ? this.hostPlayerID : this.guestPlayerID);
        if(player == null)
        {
            LightmansCurrency.LogWarning("The " + (host ? "host" : "guest") + " is no longer online. Flagging trade as abandoned.");
            return true;
        }
        if(!this.isInMenu(player))
        {
            LightmansCurrency.LogWarning("The " + (host ? "host" : "guest") + " is no longer in the trade menu. Flagging trade as abandoned.");
            return true;
        }
        return false;
    }

    private boolean isInMenu(PlayerEntity player) {
        if(player != null && player.containerMenu instanceof PlayerTradeMenu)
        {
            PlayerTradeMenu menu = (PlayerTradeMenu)player.containerMenu;
            return menu.tradeID == tradeID;
        }

        return false;
    }

    private void takeMenuAction(Consumer<PlayerTradeMenu> action) {
        this.ifInMenu(this.hostPlayerID, action);
        this.ifInMenu(this.guestPlayerID, action);
    }

    private void ifInMenu(UUID playerID, Consumer<PlayerTradeMenu> action) {
        PlayerEntity player = this.getPlayer(playerID);
        if(player != null)
            this.ifInMenu(player, action);
    }

    private void ifInMenu(PlayerEntity player, Consumer<PlayerTradeMenu> action) {
        if(player.containerMenu instanceof PlayerTradeMenu) {
            PlayerTradeMenu menu = (PlayerTradeMenu)player.containerMenu;
            if(menu.tradeID == this.tradeID)
                action.accept(menu);
        }
    }

    public final void tryCloseMenu(UUID playerID) {
        ServerPlayerEntity player = this.getPlayer(playerID);
        if(player != null)
            this.tryCloseMenu(player);
    }

    public final void tryCloseMenu(PlayerEntity player) { if(this.isInMenu(player)) player.closeContainer(); }

    public final boolean requestAccepted(ServerPlayerEntity player) {
        if(this.stillPending && this.isGuest(player))
        {
            this.stillPending = false;
            ServerPlayerEntity host = this.getPlayer(this.hostPlayerID);
            ServerPlayerEntity guest = this.getPlayer(this.guestPlayerID);
            if(host == null || guest == null)
            {
                LightmansCurrency.LogWarning("Trade Request accepted, but either the Host or Guest is no longer online.");
                return false;
            }
            //Open the Player Trading menu for both involved parties
            NetworkHooks.openGui(host, this, this::writeAdditionalMenuData);
            NetworkHooks.openGui(guest, this, this::writeAdditionalMenuData);
            LightmansCurrency.LogInfo("Trade Request accepted, and Player Trading menu should be open for both players.");
            return true;
        }
        if(!this.stillPending)
            LightmansCurrency.LogWarning("Trade Request accepted, but the trade is not in a pending state!");
        if(!this.isGuest(player))
            LightmansCurrency.LogWarning("Trade Request accepted by the wrong player.");
        return false;
    }

    public boolean shouldCancel() {
        if(this.completed)
            return true;
        if(this.stillPending)
        {
            if(this.playerMissing(this.hostPlayerID))
            {
                LightmansCurrency.LogInfo("Cancelling pending trade as the host is missing.");
                return true;
            }
            if(this.playerMissing(this.guestPlayerID))
            {
                LightmansCurrency.LogInfo("Cancelling pending trade as the guest is missing.");
                return true;
            }
            if(!TimeUtil.compareTime(IPlayerTrade.PENDING_DURATION, this.creationTime))
            {
                LightmansCurrency.LogInfo("Cancelling pending trade as the trade has expired.");
                return true;
            }
            return false;
        }
        else
            return this.playerAbandonedTrade(true) || this.playerAbandonedTrade(false);
    }

    public void onCancel() {
        if(this.stillPending || this.completed)
            return;
        //Close menu for both the host and the guest
        this.tryCloseMenu(this.hostPlayerID);
        this.tryCloseMenu(this.guestPlayerID);
    }

    private void tryExecuteTrade() {
        if(this.hostState < 2 || this.guestState < 2 || this.stillPending)
            return;

        ServerPlayerEntity host = this.getPlayer(this.hostPlayerID);
        ServerPlayerEntity guest = this.getPlayer(this.guestPlayerID);
        if(host == null || guest == null)
            return;

        //Confirm that payment can be taken successfully
        if(MoneyUtil.ProcessPayment(null, host, this.hostMoney.copy()))
        {
            if(MoneyUtil.ProcessPayment(null, guest, this.guestMoney.copy()))
            {
                //Flag trade as completed
                completed = true;

                //Give money/items to host
                MoneyUtil.ProcessChange(null, host, this.guestMoney.copy());
                for(int i = 0; i < this.guestItems.getContainerSize(); ++i)
                {
                    ItemStack stack = this.guestItems.getItem(i);
                    if(!stack.isEmpty())
                        ItemHandlerHelper.giveItemToPlayer(host, stack);
                }

                //Give money/items to guest
                MoneyUtil.ProcessChange(null, guest, this.hostMoney.copy());
                for(int i = 0; i < this.hostItems.getContainerSize(); ++i)
                {
                    ItemStack stack = this.hostItems.getItem(i);
                    if(!stack.isEmpty())
                        ItemHandlerHelper.giveItemToPlayer(guest, stack);
                }

                this.tryCloseMenu(host);
                this.tryCloseMenu(guest);

            }
            else //Refund host money if guest doesn't have enough money
                MoneyUtil.ProcessChange(null, host, this.hostMoney.copy());
        }

    }

    private ClientPlayerTrade getData() { return new ClientPlayerTrade(this.hostPlayerID, this.getHostName(), this.getGuestName(), this.hostMoney.copy(), this.guestMoney.copy(), InventoryUtil.copy(this.hostItems), InventoryUtil.copy(this.guestItems), this.hostState, this.guestState); }

    public void handleInteraction(PlayerEntity player, CompoundNBT message) {
        if(!this.isHost(player) && !this.isGuest(player))
            return;
        if(this.isHost(player))
        {
            if(message.contains("TogglePropose"))
            {
                if(this.hostState > 0)
                {
                    this.hostState = 0;
                    this.guestState = Math.min(this.guestState, 1);
                }
                else
                    this.hostState = 1;
            }
            else if(message.contains("ToggleActive"))
            {
                if(this.hostState == 2)
                    this.hostState = 1;
                else if(this.hostState == 1 && this.guestState > 0)
                {
                    this.hostState = 2;
                    this.tryExecuteTrade();
                }
            }
            else if(message.contains("ChangeMoney"))
            {
                this.hostMoney.load(message,"ChangeMoney");
                this.onTradeEdit(true);
            }
        }
        else if(this.isGuest(player))
        {
            if(message.contains("TogglePropose"))
            {
                if(this.guestState > 0)
                {
                    this.guestState = 0;
                    this.hostState = Math.min(this.hostState, 1);
                }
                else
                    this.guestState = 1;
            }
            else if(message.contains("ToggleActive"))
            {
                if(this.guestState == 2)
                    this.guestState = 1;
                else if(this.guestState == 1 && this.hostState > 0)
                {
                    this.guestState = 2;
                    this.tryExecuteTrade();
                }
            }
            else if(message.contains("ChangeMoney"))
            {
                this.guestMoney.load(message,"ChangeMoney");
                this.onTradeEdit(false);
            }
        }
        else
            return;
        this.markDirty();
    }

    public final void markDirty() {
        ClientPlayerTrade data = this.getData();
        final ServerPlayerEntity hostPlayer = this.getPlayer(this.hostPlayerID);
        final ServerPlayerEntity guestPlayer = this.getPlayer(this.guestPlayerID);
        if(hostPlayer != null)
            LightmansCurrencyPacketHandler.instance.send(PacketDistributor.PLAYER.with(() -> hostPlayer), new SMessageUpdatePlayerTrade(data));
        if(guestPlayer != null)
            LightmansCurrencyPacketHandler.instance.send(PacketDistributor.PLAYER.with(() -> guestPlayer), new SMessageUpdatePlayerTrade(data));

        this.takeMenuAction(PlayerTradeMenu::onTradeChange);
    }


    //Menu Handling/Opening
    @Override
    public @Nonnull ITextComponent getDisplayName() { return EasyText.empty(); }

    @Nullable
    @Override
    public Container createMenu(int windowID, @Nonnull PlayerInventory inventory, @Nonnull PlayerEntity player) { return new PlayerTradeMenu(windowID, inventory, this.tradeID, this); }

    private void writeAdditionalMenuData(PacketBuffer buffer) {
        buffer.writeInt(this.tradeID);
        this.getData().encode(buffer);
    }

}