package io.github.lightman314.lightmanscurrency.api.bank_account.salary;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.function.ToIntFunction;

public class SalaryData implements ISidedContext {

    public static final Codec<SalaryData> CODEC = MapCodec.unitCodec(SalaryData::new);
    public static final StreamCodec<RegistryFriendlyByteBuf,SalaryData> STREAM_CODEC = StreamHelper.uncheckedUnit(SalaryData::new);

    private BankAccount parent;
    private ToIntFunction<SalaryData> indexGetter = s -> -1;

    @Override
    public boolean isClient() { return this.parent == null || this.parent.isClient(); }

    public void handlePacket(FancyPacketMap data) {

    }

    public void tick() {

    }

    public final SalaryData init(BankAccount account,ToIntFunction<SalaryData> indexGetter) {
        this.parent = account;
        this.indexGetter = indexGetter;
        return this;
    }

    public static void init(List<SalaryData> salaries, BankAccount account) {
        for(SalaryData salary : salaries)
            salary.init(account,salaries::indexOf);
    }

}
