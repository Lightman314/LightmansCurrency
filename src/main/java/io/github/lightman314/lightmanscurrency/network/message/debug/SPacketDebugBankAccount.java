package io.github.lightman314.lightmanscurrency.network.message.debug;

import com.google.gson.JsonElement;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.helpers.JsonHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.data.CodecInteractionHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SPacketDebugBankAccount extends ServerToClientPacket {

    public static final Type<SPacketDebugBankAccount> TYPE = sType("debug_bank_account");
    public static final StreamCodec<RegistryFriendlyByteBuf,SPacketDebugBankAccount> STREAM_CODEC = BankReference.STREAM_CODEC.map(SPacketDebugBankAccount::new,p -> p.account);

    private final BankReference account;
    public SPacketDebugBankAccount(BankReference account) {
        super(TYPE);
        this.account = account;
    }

    @Override
    protected void handle(IPayloadContext context,Player player) {
        BankAccount account = this.account.setSidedContext(ISidedContext.LOGICAL_CLIENT).get();
        if(account != null) {
            CodecInteractionHelper<JsonElement> encoder = CodecInteractionHelper.createJson(player.registryAccess());
            LightmansCurrency.LogInfo("Client Copy of " + account.getName().getString() + ":\n" + JsonHelper.PRETTY_GSON.toJson(encoder.write(account,BankAccount.CODEC)));
            player.sendSystemMessage(Component.literal("Client Copy of " + account.getName().getString() + " has been printed to the logs!"));
        }
        else
            player.sendSystemMessage(Component.literal("Bank Account does not exist on the client!"));
    }

}
