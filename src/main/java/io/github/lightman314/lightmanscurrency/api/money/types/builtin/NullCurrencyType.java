package io.github.lightman314.lightmanscurrency.api.money.types.builtin;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValueParser;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class NullCurrencyType extends CurrencyType {

    public static final ResourceLocation TYPE = new ResourceLocation(MoneyAPI.MODID, "null");
    public static final NullCurrencyType INSTANCE = new NullCurrencyType();

    private NullCurrencyType() { super(TYPE); }

    @Nonnull
    @Override
    protected MoneyValue sumValuesInternal(@Nonnull List<MoneyValue> values) {
        return null;
    }
    @Override
    public boolean hasPlayersMoneyChanged(@Nonnull Player player) { return false; }
    @Override
    public void getAvailableMoney(@Nonnull Player player, @Nonnull MoneyView.Builder builder) {}
    @Override
    public void giveMoneyToPlayer(@Nonnull Player player, @Nonnull MoneyValue value) { }
    @Override
    public boolean takeMoneyFromPlayer(@Nonnull Player player, @Nonnull MoneyValue value) { return false; }
    @Override
    public MoneyValue loadMoneyValue(@Nonnull CompoundTag valueTag) {
        if(valueTag.contains("Free", Tag.TAG_BYTE) && valueTag.getBoolean("Free"))
            return MoneyValue.free();
        return MoneyValue.empty();
    }
    @Override
    public MoneyValue loadMoneyValueJson(@Nonnull JsonObject json) { return GsonHelper.getAsBoolean(json, "Free", false) ? MoneyValue.free() : MoneyValue.empty(); }

    @Nullable
    @Override
    public MoneyValueParser getValueParser() {return null; }

    @Override
    public List<Object> getInputHandlers(@Nullable Player player) { return new ArrayList<>(); }

}
