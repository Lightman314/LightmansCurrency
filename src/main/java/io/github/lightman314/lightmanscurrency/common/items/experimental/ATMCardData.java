package io.github.lightman314.lightmanscurrency.common.items.experimental;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.codecs.LCCodecs;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public record ATMCardData(@Nonnull Optional<BankReference> bankReference, int validation, boolean locked) {

    public static final ATMCardData EMPTY = new ATMCardData(Optional.empty(),-1,false);

    public static final Codec<ATMCardData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(LCCodecs.BANK_REFERENCE.optionalFieldOf("Account").forGetter(ATMCardData::bankReference),
                    Codec.INT.fieldOf("Validation").forGetter(ATMCardData::validation),
                    Codec.BOOL.fieldOf("Locked").forGetter(ATMCardData::locked)
            ).apply(builder, ATMCardData::new)
    );

    public static final StreamCodec<FriendlyByteBuf, ATMCardData> STREAM_CODEC = StreamCodec.of((b, d) -> {
        b.writeBoolean(d.bankReference.isPresent());
        d.bankReference.ifPresent(br -> b.writeNbt(br.save()));
        b.writeInt(d.validation);
        b.writeBoolean(d.locked);
    },b -> {
        Optional<BankReference> br = Optional.empty();
        if(b.readBoolean())
            br = Optional.of(BankReference.load(b.readNbt()));
        return new ATMCardData(br,b.readInt(),b.readBoolean());
    });

    @Nullable
    public BankReference getBankReference() { return this.bankReference.orElse(null); }
    @Nullable
    public BankReference getBankReference(@Nonnull IClientTracker parent) { return this.getBankReference(parent.isClient()); }
    @Nullable
    public BankReference getBankReference(boolean isClient)
    {
        this.bankReference.ifPresent(br -> br.flagAsClient(isClient));
        return this.bankReference.orElse(null);
    }

    @Nonnull
    public ATMCardData withBankReference(@Nullable BankReference bankReference, int validation) { return new ATMCardData(Optional.ofNullable(bankReference),validation,this.locked); }
    @Nonnull
    public ATMCardData withLockedState(boolean locked) { return new ATMCardData(this.bankReference,this.validation,locked); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ATMCardData other)
            return other.bankReference.equals(this.bankReference) && other.validation == this.validation && other.locked == this.locked;
        return false;
    }

    @Override
    public int hashCode() { return Objects.hash(this.bankReference,this.validation,this.locked); }

}
