package io.github.lightman314.lightmanscurrency.features;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCCapabilities;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerClient;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerCommon;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerServer;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyKey;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.trader.world.block.TraderBlock;
import io.github.lightman314.lightmanscurrency.core.LCBlockEntities;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import io.github.lightman314.lightmanscurrency.core.LCRecipeTypes;
import io.github.lightman314.lightmanscurrency.core.neoforge.LCDataAttachments;
import io.github.lightman314.lightmanscurrency.features.api_impl.data.PlayerBankDataCache;
import io.github.lightman314.lightmanscurrency.features.enchantments.CoinMagnetEnchantmentHelper;
import io.github.lightman314.lightmanscurrency.features.enchantments.MoneyMendingEnchantmentHelper;
import io.github.lightman314.lightmanscurrency.features.trader.item_common.ItemStorageNode;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.TooltipProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.tooltip.TooltipAppender;
import net.neoforged.neoforge.common.tooltip.TooltipLocation;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterTooltipAppendersEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@EventBusSubscriber
public final class CommonEventListeners {

    private CommonEventListeners() {}

    private static int ticker = 0;

    @SubscribeEvent
    private static void addRegistries(NewRegistryEvent event)
    {
        event.register(LCRegistries.Money.VALUE_TYPE);
        event.register(LCRegistries.Money.VALUE_SOURCE);
        event.register(LCRegistries.Money.VALUE_HELPER);
        event.register(LCRegistries.Coins.VALUE_DISPLAY_SERIALIZER);
        event.register(LCRegistries.Coins.ATM_ICON_TYPE);
        event.register(LCRegistries.Coins.ATM_COMMAND_TYPE);
        event.register(LCRegistries.Bank.REFERENCE_TYPE);
        event.register(LCRegistries.Ownership.OWNER_TYPE);
        event.register(LCRegistries.Ownership.POTENTIAL_OWNER);
        event.register(LCRegistries.Trader.TRADER_TYPES);
        event.register(LCRegistries.Trader.TRADER_NODE_TYPE);
        event.register(LCRegistries.Trader.TRADE_DATA_TYPE);
        event.register(LCRegistries.Trader.TRADE_PRICE_TYPE);
        event.register(LCRegistries.Trader.TRADE_PRICE_RECEIPT_TYPE);
        event.register(LCRegistries.Trader.TRADE_RULE_TYPE);
        event.register(LCRegistries.Trader.PERMISSION_TYPE);
        event.register(LCRegistries.Trader.PERMISSION);
        event.register(LCRegistries.Notifications.NOTIFICATION_TYPE);
        event.register(LCRegistries.Notifications.NOTIFICATION_CATEGORY_TYPE);
        event.register(LCRegistries.Upgrades.UPGRADES);
        event.register(LCRegistries.Upgrades.NUMBER_SOURCE);
        event.register(LCRegistries.Misc.MENU_VALIDATOR);
        event.register(LCRegistries.Misc.ICON_TYPE);
        event.register(LCRegistries.Data.FANCY_DATA);
        event.register(LCRegistries.Network.PACKET_TYPE);
    }

    @SubscribeEvent
    private static void serverTick(ServerTickEvent.Post event) {
        ProfilerFiller filler = Profiler.get();
        //TODO Check the Date Trigger once every minute
        //Enchantments
        ticker++;
        if(ticker >= LCConfig.SERVER.enchantmentTickDelay.get()) {
            filler.push("Lightman's Currency Enchantment Ticks");
            ticker = 0;
            for(ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                if(!player.isSpectator()) {
                    //Wallet Enchantment Ticks (Coin Magnet for now)
                    player.getExistingData(LCDataAttachments.WALLET).ifPresent(attachment -> {
                        //Tick Coin Magnet
                        CoinMagnetEnchantmentHelper.runEntityTick(attachment,player);
                    });
                    //Money Mending Tick
                    MoneyMendingEnchantmentHelper.runEntityTick(player);
                }
            }
            filler.pop();
        }
        //Bank Account Salary Tick
        filler.push("Bank Account Salary Tick");
        List<BankAccount> allAccounts = LCApi.getBankAPI().getAllBankAccounts(ISidedContext.LOGICAL_SERVER);
        for(BankAccount account : allAccounts) {
            account.serverTick();
        }
        filler.pop();
        //Bank Interest Tick
        PlayerBankDataCache bankData = PlayerBankDataCache.TYPE.get(ISidedContext.LOGICAL_SERVER);
        if(LCConfig.SERVER.bankAccountInterestRate.get() > 0 &&
                TimeHelper.timerExpired(bankData.getLastInterestTime(),LCConfig.SERVER.bankAccountInterestTime.get() * TimeHelper.DURATION_MINUTE)) {
            filler.push("Bank Account Interest");
            bankData.flagInterestAsComplete();
            //Collect config values to optimize loop as it's very possible that there could be a lot of bank accounts to loop through
            double rate = LCConfig.SERVER.bankAccountInterestRate.get();
            Map<MoneyKey,MoneyValue> limits = LCConfig.SERVER.bankAccountInterestLimits.get();
            List<String> blacklist = LCConfig.SERVER.bankAccountInterestBlacklist.get();
            boolean forceInterest = LCConfig.SERVER.bankAccountForceInterest.get();
            boolean pushNotification = LCConfig.SERVER.bankAccountInterestNotification.get();
            for(BankAccount account : allAccounts) {
                try {
                    account.applyInterest(rate,limits,blacklist,forceInterest,pushNotification);
                } catch (Throwable e) {
                    LightmansCurrency.LogError("Error applying bank account interest!",e);
                }
            }
            filler.pop();
        }
    }

    //Tick the players open menu
    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Pre event)
    {
        Player player = event.getEntity();
        ISidedContext context = ISidedContext.wrap(player);
        if(player.containerMenu instanceof ITickerClient t && context.isClient())
            t.clientTick();
        if(player.containerMenu instanceof ITickerCommon t)
            t.tick();
        if(player.containerMenu instanceof ITickerServer t && context.isServer())
            t.serverTick();
    }

    @SubscribeEvent
    private static void registerTooltipAppenders(RegisterTooltipAppendersEvent event) {
        //Register coin tooltip appender
        event.registerAppender(TooltipLocation.HEAD,ChainData::addCoinTooltips);
        event.registerAppender(TooltipLocation.TAIL,MoneyMendingEnchantmentHelper::addEnchantmentTooltips);
        //Register data component types
        beforeAll(event,LCDataComponents.COLOR_DISPLAY);
        beforeAll(event,LCDataComponents.COPIED_TRADER);
        beforeAll(event,LCDataComponents.STORED_TRADER);
        beforeAll(event,LCDataComponents.UPGRADE_TYPE);
    }

    private static <T extends TooltipProvider> void beforeAll(RegisterTooltipAppendersEvent event, Supplier<DataComponentType<T>> type) {
        event.registerComponentAppenderBeforeAll(type,TooltipAppender.createComponentAppender(type.get()));
    }

    @SubscribeEvent
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerEntity(LCCapabilities.Money.ENTITY,EntityType.PLAYER,(p,d) -> LCApi.getMoneyAPI().getPlayersMoneyHandler(p));
        event.registerBlockEntity(Capabilities.Item.BLOCK, LCBlockEntities.COIN_MINT.get(),(be,side) -> be.getItemCapability());

        //Item Storage Capabilities
        TraderBlock.registerCapability(event,Capabilities.Item.BLOCK,LCBlockEntities.ITEM_TRADER,(t,s) -> t.getNodeArgValue(ItemStorageNode.TYPE,s,ItemStorageNode::getCapabilityForSide));
        //TODO same for gacha machine

    }

    private static void syncRelevantRecipes(OnDatapackSyncEvent event) {
        //Sync for recipe-viewer purposes
        event.sendRecipes(LCRecipeTypes.COIN_MINT.get());
    }

}
