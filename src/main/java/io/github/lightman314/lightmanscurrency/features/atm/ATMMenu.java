package io.github.lightman314.lightmanscurrency.features.atm;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.builtin.MoneyItemStorage;
import io.github.lightman314.lightmanscurrency.api.money.resource.builtin.UnsortedMoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.world.menu.provider.FancyMenuProvider;
import io.github.lightman314.lightmanscurrency.api.world.menu.slots.EasyResourceSlot;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.TabBuilder;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.TabbedMenu;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.builtin.BlockValidator;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.builtin.SimpleValidator;
import io.github.lightman314.lightmanscurrency.core.LCMenuTypes;
import io.github.lightman314.lightmanscurrency.features.api_impl.data.PlayerBankDataCache;
import io.github.lightman314.lightmanscurrency.features.atm.tabs.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ATMMenu extends TabbedMenu.Validated<ATMMenu,ATMTab> {

    private final List<Slot> moneySlots;
    public List<Slot> getMoneySlots() { return Objects.requireNonNullElseGet(this.moneySlots,List::of); }

    private final MoneyItemStorage moneyStorage = new MoneyItemStorage(9);
    public ResourceHandler<ItemResource> getMoneyStorage() { return this.moneyStorage; }
    public MoneyResourceHandler getMoneyResources() { return this.moneyStorage.getMoneyResourceHandler(this.getPlayer()); }

    private MoneyResourceHandler getPlayerMoney(boolean allowOverflow) { return LCApi.getMoneyAPI().getPlayersMoneyHandler(this.getPlayer(),allowOverflow); }

    public MoneyResourceHandler getPlayerAndMoneyResources() { return new UnsortedMoneyResourceHandler(List.of(this.getPlayerMoney(false),this.getMoneyResources(),this.getPlayerMoney(true))); }
    public MoneyResourceHandler getMoneyAndPlayerResources() { return new UnsortedMoneyResourceHandler(List.of(this.getMoneyResources(),this.getPlayerMoney(true))); }

    public ATMMenu(int containerId,Player player) { this(LCMenuTypes.ATM.get(),containerId,player,SimpleValidator.ALWAYS_TRUE); }
    protected ATMMenu(int containerId, Player player,MenuValidator validator) { this(LCMenuTypes.ATM.get(),containerId,player,validator); }
    protected ATMMenu(@Nullable MenuType<?> menuType, int containerId, Player player,MenuValidator validator) {
        super(menuType, containerId, player,validator);
        //Money Slots
        List<Slot> temp = new ArrayList<>();
        for(int x = 0; x < this.moneyStorage.size(); ++x) {
            Slot slot = this.addSlot(new EasyResourceSlot(this.moneyStorage,x,8 + x * 18,129));
            slot.setBackground(LCApi.id("container/slot/money"));
            temp.add(slot);
        }
        this.moneySlots = List.copyOf(temp);
    }

    public BankReference getSelectedAccount() { return LCApi.getBankAPI().getPlayersSelectedAccount(this.getPlayer()); }

    @Override
    protected void collectTabs(TabBuilder<ATMMenu,ATMTab> builder) {
        builder.addTab(new CoinExchangeTab(this));
        //Bank Account Tabs
        builder.addTab(new AccountSelectionTab(this));
        builder.addTab(new AccountInteractionTab(this));
        builder.addTab(new AccountSettingsTab(this));
        builder.addTab(new AccountLogsTab(this));
        builder.addTab(new MoneyTransferTab(this));
        //Salary Tabs
        //TODO add salary tabs
    }

    @Override
    protected void addInventorySlots(Inventory inventory) {
        //Player items
        for(int y = 0; y < 3; y++)
        {
            for(int x = 0; x < 9; x++)
            {
                this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 161 + y * 18));
            }
        }
        //Player hotbar
        for(int x = 0; x < 9; x++) {
            this.addSlot(new Slot(inventory, x, 8 + x * 18, 219));
        }
    }

    @Override
    public void serverTick() {
        super.serverTick();
        this.getSelectedAccount();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.clearContainer(this.moneyStorage);
        if(this.isServer()) {
            BankReference account = this.getSelectedAccount();
            if(!account.canPersist(this.getPlayer())) {
                PlayerBankDataCache.TYPE.get(this)
                        .setSelectedAccount(this.getPlayer(),PlayerBankReference.of(this.getPlayer()));
            }
        }
    }

    public static MenuProvider createProvider(Block block,BlockPos pos) { return createProvider(new BlockValidator(block,pos)); }
    public static MenuProvider createProvider(MenuValidator validator) {
        return new FancyMenuProvider((id,inv,p) -> new ATMMenu(id,p,validator));
    }

}
