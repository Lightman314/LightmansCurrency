package io.github.lightman314.lightmanscurrency.common.core.custom;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.*;
import io.github.lightman314.lightmanscurrency.common.traders.auction.nodes.*;
import io.github.lightman314.lightmanscurrency.common.traders.commands.nodes.*;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.nodes.*;
import io.github.lightman314.lightmanscurrency.common.traders.item.nodes.*;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.nodes.*;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.nodes.*;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModTraderNodes {

    public static final DeferredRegister<TraderNodeType<?>> REGISTER = DeferredRegister.create(LCRegistries.TRADER_NODE,LightmansCurrency.MODID);

    static {
        //Register the built-in nodes
        REGISTER.register("base", () -> NormalTraderNode.TYPE);
        REGISTER.register("owner", () -> OwnerNode.TYPE);
        REGISTER.register("allies", () -> AlliesNode.TYPE);
        REGISTER.register("display",() -> DisplayNode.TYPE);
        REGISTER.register("world_state",() -> WorldStateNode.TYPE);
        REGISTER.register("money_storage", () -> MoneyStorageNode.TYPE);
        REGISTER.register("bank", () -> BankNode.TYPE);
        REGISTER.register("upgrades", () -> UpgradesNode.TYPE);
        REGISTER.register("input", () -> InputNode.TYPE);
        REGISTER.register("interaction", () -> InteractionNode.TYPE);
        REGISTER.register("logger", () -> LoggerNode.TYPE);
        REGISTER.register("rules", () -> TraderRulesNode.TYPE);
        REGISTER.register("taxes", () -> TaxesNode.TYPE);
        REGISTER.register("network", () -> NetworkNode.TYPE);
        REGISTER.register("interface_support",() -> InterfaceSupportNode.TYPE);
        //Admin-Only Trader Nodes
        REGISTER.register("persistent_data",() -> PersistentDataNode.TYPE);
        REGISTER.register("admin",() -> AdminNode.TYPE);

        //Optional Node for addons/integration
        REGISTER.register("fake_owner",() -> FakeOwnerNode.TYPE);
        REGISTER.register("color",() -> TraderColorNode.TYPE);
        REGISTER.register("machine_access", () -> MachineAccessNode.TYPE);

        //Auction House Nodes
        REGISTER.register("auction_trades",() -> AuctionTradesNode.TYPE);
        REGISTER.register("auction_storage",() -> AuctionStorageNode.TYPE);

        //Item Trader Nodes
        REGISTER.register("item_storage",() -> ItemStorageNode.TYPE);
        REGISTER.register("item_trades",() -> ItemTradeNode.TYPE);
        REGISTER.register("item_restriction_armor",() -> ArmorRestrictionNode.TYPE);
        REGISTER.register("item_restriction_book",() -> ArmorRestrictionNode.TYPE);
        REGISTER.register("item_restriction_ticket",() -> TicketRestrictionNode.TYPE);

        //Paygate Nodes
        REGISTER.register("ticket_stub_storage",() -> TicketStubNode.TYPE);
        REGISTER.register("paygate_trades",() -> PaygateTradeNode.TYPE);

        //Command Nodes
        REGISTER.register("command_trades",() -> CommandTradeNode.TYPE);

        //Slot Machine Nodes
        REGISTER.register("slot_machine",() -> SlotMachineNode.TYPE);

        //Gacha Machine Nodes
        REGISTER.register("gacha_storage",() -> GachaStorageNode.TYPE);
        REGISTER.register("gacha",() -> GachaNode.TYPE);

    }

}
