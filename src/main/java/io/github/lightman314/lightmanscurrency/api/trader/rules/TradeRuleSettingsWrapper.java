package io.github.lightman314.lightmanscurrency.api.trader.rules;

import io.github.lightman314.lightmanscurrency.api.helpers.keys.DualKey;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.TradeRulesNode;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.ISettingsStorageIO;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.SettingsDisplayOutput;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.SettingsLoadContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;
import java.util.function.Predicate;

public class TradeRuleSettingsWrapper implements ISettingsStorageIO {

    private final TradeRuleHolder holder;
    private final DualKey key;
    private final Component name;
    private final Predicate<SettingsLoadContext> loadPermissions;
    public TradeRuleSettingsWrapper(TradeRuleHolder holder,DualKey parentKey,Component name,Predicate<SettingsLoadContext> loadPermissions) { this(holder,parentKey,"",name,loadPermissions); }
    public TradeRuleSettingsWrapper(TradeRuleHolder holder,DualKey parentKey,String extraKey,Component name,Predicate<SettingsLoadContext> loadPermissions) {
        this.holder = holder;
        this.key = parentKey.addToKey("trade_rules" + extraKey);
        this.name = name;
        this.loadPermissions = loadPermissions;
    }

    @Override
    public DualKey getSettingsKey() { return this.key; }
    @Override
    public Component getSettingsName() { return this.name; }

    @Override
    public void encodeSettings(ValueOutput output) {
        this.holder.getRuleData().forEach((type,rule) -> {
            if(rule.shouldWriteToFile()) {
                ValueOutput entry = output.child(type.getKey().toString());
                rule.encodeSettings(entry);
            }
        });
    }

    @Override
    public void decodeSettings(ValueInput data, SettingsLoadContext context) {
        if(this.loadPermissions.test(context)) {
            this.holder.getRuleData().forEach((type,rule) -> {
                Optional<ValueInput> entry = data.child(type.getKey().toString());
            });
        }
    }

    @Override
    public void appendDisplay(ValueInput data, SettingsDisplayOutput output) {
        output.acceptTitle(this.name);
        output.acceptEntry(TradeRulesNode.VALUE_RULES,data.keySet().size());
    }

}
