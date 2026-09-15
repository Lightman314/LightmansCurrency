package io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.builtin;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.money.resource.SortableMoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.builtin.ItemCapabilityResourceWrapper;
import io.github.lightman314.lightmanscurrency.api.money.resource.builtin.MoneyItemStorage;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IInteractionSlotProvider;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.BuiltInResourceTypes;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.AbstractTabbedCustomerMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.TraderCustomerTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.slot.InteractionSlot;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.slot.InteractionSlotData;
import io.github.lightman314.lightmanscurrency.api.world.menu.slots.EasyResourceSlot;
import io.github.lightman314.lightmanscurrency.api.world.menu.slots.IEasySlot;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class NormalCustomerTab extends TraderCustomerTab {

    public static final TextEntry TOOLTIP_CUSTOMER_TAB = TextEntry.tooltip(LCApi.MODID,"trader.customer_interactions");
    public static final TextEntry TOOLTIP_MONEY_SOURCE_SLOTS = SortableMoneyResourceHandler.createEntry(LCApi.MODID,"slots");
    public static final TextEntry TOOLTIP_MONEY_SOURCE_SLOTS_CAPABILITY = SortableMoneyResourceHandler.createEntry(LCApi.MODID,"slots.capability");

    public static final Identifier CLIENT_KEY = LCApi.id("customer");

    private InteractionSlot interactionSlot = new InteractionSlot(new HashMap<>(),0,0,this);
    public InteractionSlot getInteractionSlot() { return this.interactionSlot; }

    private final MoneyItemStorage storage = new MoneyItemStorage(5);
    private final List<Slot> moneySlots = new ArrayList<>();
    public List<Slot> getMoneySlots() { return this.moneySlots; }
    public NormalCustomerTab(AbstractTabbedCustomerMenu menu) { super(menu); }

    @Override
    public Identifier getClientTabKey() { return CLIENT_KEY; }

    @Override
    public void addMenuSlots(Consumer<Slot> builder) {
        //Money Slots
        for(int x = 0; x < this.storage.size(); ++x) {
            EasyResourceSlot slot = new EasyResourceSlot(this.storage,x,SLOT_OFFSET + 8 + (x + 4) * 18,122);
            this.moneySlots.add(slot);
            slot.setActive(false);
            slot.setBackground(LCApi.id("container/slot/money"));
            builder.accept(slot);
        }
        //Interaction slots
        Map<Identifier,InteractionSlotData> slotData = new HashMap<>();
        Consumer<InteractionSlotData> b = data -> slotData.put(data.getType(),data);
        for(TraderData trader : this.getMenu().getTraders())
        {
            for(IInteractionSlotProvider node : trader.getNodes(IInteractionSlotProvider.class))
                node.addInteractionSlot(b);
        }
        this.interactionSlot = new InteractionSlot(slotData,SLOT_OFFSET + 8,122,this);
        this.interactionSlot.setActive(false);
        builder.accept(this.interactionSlot);
    }

    @Override
    public void buildContext(TradeContext.Builder builder) {
        //Item capability wrapper only at high priority for both inserting and extracting money
        builder.withResource(BuiltInResourceTypes.MONEY,SortableMoneyResourceHandler.wrapHandler(new ItemCapabilityResourceWrapper(this.storage,this),TOOLTIP_MONEY_SOURCE_SLOTS_CAPABILITY.get(),-200,-200))
                //All other money handling (i.e. coins) for the money slots at high extract priority and low insert priority
                .withResource(BuiltInResourceTypes.MONEY,SortableMoneyResourceHandler.wrapHandler(this.storage.getMoneyResourceHandler(this.getPlayer(),false),TOOLTIP_MONEY_SOURCE_SLOTS.get(),100));
        //Add interaction slot resources directly
        this.interactionSlot.wrapResource(builder);
    }

    @Override
    public void onTabOpened(FancyPacketMap additional) {
        IEasySlot.setActive(this.moneySlots,true);
        this.interactionSlot.setActive(true);
    }

    @Override
    public void onTabClosed() {
        IEasySlot.setActive(this.moneySlots,false);
        this.interactionSlot.setActive(false);
    }

    @Override
    public void onMenuClosed() {
        //Clear the money slots
        this.getMenu().clearContainer(this.storage);
        this.getMenu().clearContainer(this.interactionSlot.container);
    }

}
