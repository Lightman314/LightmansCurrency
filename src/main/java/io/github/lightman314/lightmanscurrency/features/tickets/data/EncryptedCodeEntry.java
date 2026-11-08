package io.github.lightman314.lightmanscurrency.features.tickets.data;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.features.tickets.TicketHelper;
import io.github.lightman314.lightmanscurrency.features.tickets.TicketDataCache;
import io.github.lightman314.lightmanscurrency.features.tickets.recipe.TicketStationRecipeInput;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.math.BigInteger;

public record EncryptedCodeEntry(BigInteger number) {
    public EncryptedCodeEntry(String code,ISidedContext context) { this(encryptCode(code,TicketHelper.getCodeKey(context))); }
    public EncryptedCodeEntry(String code,String key) { this(encryptCode(code,key)); }

    public static final Codec<EncryptedCodeEntry> CODEC = CodecHelper.BIG_INTEGER.xmap(EncryptedCodeEntry::new,EncryptedCodeEntry::number);
    public static final StreamCodec<ByteBuf,EncryptedCodeEntry> STREAM_CODEC = StreamHelper.BIG_INTEGER.map(EncryptedCodeEntry::new,EncryptedCodeEntry::number);

    public String decryptCode(ISidedContext context) {
        String key = TicketDataCache.TYPE.get(context).getCodeKey();
        StringBuilder result = new StringBuilder();
        StringBuilder blankSpace = new StringBuilder();
        for(int i = 0;i < TicketStationRecipeInput.MAX_CODE_LENGTH;++i) {
            BigInteger entry = this.number.divide(BigInteger.valueOf(i * 63)).remainder(BigInteger.valueOf(64));
            int value = entry.intValueExact();
            char c = key.charAt(value);
            if(c == ' ')
                blankSpace.append(c);
            else {
                result.append(blankSpace).append(c);
                if(!blankSpace.isEmpty())
                    blankSpace = new StringBuilder();
            }
        }
        return result.toString();
    }

    public boolean is(String code,ISidedContext context) { return this.number.equals(encryptCode(code,TicketHelper.getCodeKey(context))); }
    public boolean is(String code,String key) { return this.number.equals(encryptCode(code,key)); }

    private static BigInteger encryptCode(String code,String key) {
        BigInteger number = BigInteger.ZERO;
        for(int i = 0; i < TicketStationRecipeInput.MAX_CODE_LENGTH; ++i) {
            char c = i < code.length() ? code.charAt(i) : ' ';
            int val = key.indexOf(c);
            if(val == -1)
                val = key.indexOf(' ');
            number = number.add(BigInteger.valueOf(val).multiply(BigInteger.valueOf(i * 63)));
        }
        return number;
    }

}
