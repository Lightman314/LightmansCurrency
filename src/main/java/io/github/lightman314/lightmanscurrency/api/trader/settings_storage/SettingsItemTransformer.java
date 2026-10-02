package io.github.lightman314.lightmanscurrency.api.trader.settings_storage;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;

import java.util.List;

public abstract class SettingsItemTransformer {

    public static ItemStack transformItemAfterWrite(ItemStack item,ValueInput input,Component machineName,Player player, List<ISettingsStorageIO> settings) {
        for(SettingsItemTransformer transformer : LCRegistries.Trader.SETTINGS_ITEM_TRANSFORMER) {
            if(transformer.isForItem(item))
                return transformer.tryAppendAdditionalData(item,input,machineName,player,settings);
        }
        return item;
    }

    protected abstract boolean isForItem(ItemStack item);
    protected abstract ItemStack tryAppendAdditionalData(ItemStack item,ValueInput input,Component machineName,Player player,List<ISettingsStorageIO> settings);

    public abstract static class PrettyLineWriter extends SettingsItemTransformer {
        @Override
        protected final ItemStack tryAppendAdditionalData(ItemStack item, ValueInput input, Component machineName, Player player, List<ISettingsStorageIO> settings) {
            SettingsDisplayOutput.Simple output = new SettingsDisplayOutput.Simple();
            for(ISettingsStorageIO s : settings)
                s.appendDisplay(input,output);
            return this.writeLinesToStack(item,new PrettyLineData(machineName,player,output.lines()));
        }

        protected abstract ItemStack writeLinesToStack(ItemStack item,PrettyLineData data);

        protected record PrettyLineData(Component machineName,Player player,List<Component> lines) {}

    }

}