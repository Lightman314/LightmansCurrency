package io.github.lightman314.lightmanscurrency.api.traders.menu.customer;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.ITraderSource;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.MoneyStorageNode;
import io.github.lightman314.lightmanscurrency.api.traders.discount_codes.TypedInputSource;
import io.github.lightman314.lightmanscurrency.common.menus.LazyMessageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.providers.TerminalMenuProvider;
import io.github.lightman314.lightmanscurrency.common.menus.validation.IValidatedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public abstract class AbstractTraderMenu extends LazyMessageMenu implements ITraderMenu, IValidatedMenu {

    protected final MenuValidator validator;
    protected AbstractTraderMenu(MenuType<?> type, int id, Inventory inventory, MenuValidator validator) {
        super(type, id, inventory, validator);
        this.validator = validator;
    }

    public final boolean isSingleTrader() {
        ITraderSource tradeSource = this.getTraderSource();
        if(tradeSource == null)
        {
            this.player.closeContainer();
            return false;
        }
        return tradeSource.isSingleTrader() && tradeSource.getTraders().size() == 1;
    }

    @Nullable
    public final TraderData getSingleTrader() {
        if(this.isSingleTrader())
            return this.getTraderSource().getSingleTrader();
        return null;
    }

    @Override
    public final Player getPlayer() { return this.player; }

    @Override
    public final List<Slot> getSlots() { return ImmutableList.copyOf(this.slots); }

    @Override
    public final ItemStack getHeldItem() { return this.getCarried(); }
    @Override
    public final void setHeldItem(ItemStack stack) { this.setCarried(stack); }

    protected final TypedInputSource discountCodes = new TypedInputSource();
    @Override
    public final Set<String> getTypedDiscountCodes() { return this.discountCodes.getCodes(); }

    @Override
    public final void submitDiscountCode(String code)
    {
        if(code.isEmpty())
            return;
        this.discountCodes.addCode(code);
        if(this.isClient())
            this.SendMessage(this.builder().setString("AddCode",code));
    }

    @Override
    public final void removeDiscountCode(String code)
    {
        if(code.isEmpty())
            return;
        this.discountCodes.removeCode(code);
        if(this.isClient())
            this.SendMessage(this.builder().setString("RemoveCode",code));
    }

    @Override
    public final void clearDiscountCodes()
    {
        this.discountCodes.clearCodes();
        if(this.isClient())
            this.SendMessage(this.builder().setFlag("ClearCodes"));
    }

    public final void openStorage()
    {
        if(this.isClient())
        {
            this.SendMessage(this.builder().setFlag("OpenStorage"));
            return;
        }
        ITraderSource source = this.getTraderSource();
        if(source != null && source.isSingleTrader())
        {
            TraderData trader = source.getSingleTrader();
            if(trader != null)
                trader.openStorageMenu(this.player,this.validator);
            else
                LightmansCurrency.LogWarning("Error opening storage menu!\nTrader source is present, but the trader could not be found!");
        }
        else
        {
            LightmansCurrency.LogWarning("Error opening storage menu!\nEither the source is null, or not a single trader!");
        }
    }

    public final void collectMoney()
    {
        if(this.isClient())
        {
            this.SendMessage(this.builder().setFlag("CollectMoney"));
            return;
        }
        ITraderSource source = this.getTraderSource();
        if(source != null && source.isSingleTrader())
        {
            TraderData trader = source.getSingleTrader();
            if(trader != null)
            {
                MoneyStorageNode node = trader.getNode(MoneyStorageNode.TYPE);
                if(node != null)
                    node.collectStoredMoney(this.player);
            }
        }
    }

    public final void openTerminal()
    {
        if(this.isClient())
        {
            this.SendMessage(this.builder().setFlag("OpenTerminal"));
            return;
        }
        //Only allow terminal access if we got here *through* a terminal
        if(this.validator.isThroughNetwork)
            TerminalMenuProvider.OpenMenu(this.player,this.validator);
    }

    @Override
    public final void ExecuteTrade(int traderIndex, int tradeIndex) {
        if(this.isClient())
        {
            this.SendMessage(this.builder().setInt("ExecuteTrade",traderIndex)
                    .setInt("TradeIndex",tradeIndex));
            return;
        }
        this.executeTrade(traderIndex,tradeIndex);
    }

    protected abstract void executeTrade(int traderIndex,int tradeIndex);

    @Override
    public boolean isClient() { return this.player.level().isClientSide; }

    @Override
    public final MenuValidator getValidator() { return this.validator; }

    @Override
    protected void processMessage(LazyPacketData message) {
        if(message.contains("AddCode"))
            this.submitDiscountCode(message.getString("AddCode"));
        if(message.contains("RemoveCode"))
            this.removeDiscountCode(message.getString("RemoveCode"));
        if(message.contains("ClearCodes"))
            this.clearDiscountCodes();
        if(message.contains("OpenStorage"))
            this.openStorage();
        if(message.contains("CollectMoney"))
            this.collectMoney();
        if(message.contains("OpenTerminal"))
            this.openTerminal();
        if(message.contains("ExecuteTrade") && message.contains("TradeIndex"))
            this.ExecuteTrade(message.getInt("ExecuteTrade"),message.getInt("TradeIndex"));
    }

}
