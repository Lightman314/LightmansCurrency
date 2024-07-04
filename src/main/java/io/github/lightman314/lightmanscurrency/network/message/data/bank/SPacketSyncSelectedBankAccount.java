package io.github.lightman314.lightmanscurrency.network.message.data.bank;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketSyncSelectedBankAccount extends ServerToClientPacket {

	private static final Type<SPacketSyncSelectedBankAccount> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"s_data_selected_bank_account"));
	public static final Handler<SPacketSyncSelectedBankAccount> HANDLER = new H();

	final BankReference selectedAccount;
	
	public SPacketSyncSelectedBankAccount(BankReference selectedAccount) { super(TYPE); this.selectedAccount = selectedAccount; }
	
	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull SPacketSyncSelectedBankAccount message) { message.selectedAccount.encode(buffer); }
	private static SPacketSyncSelectedBankAccount decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketSyncSelectedBankAccount(BankReference.decode(buffer)); }

	private static class H extends Handler<SPacketSyncSelectedBankAccount>
	{
		protected H() { super(TYPE, easyCodec(SPacketSyncSelectedBankAccount::encode,SPacketSyncSelectedBankAccount::decode)); }
		@Override
		protected void handle(@Nonnull SPacketSyncSelectedBankAccount message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			LightmansCurrency.PROXY.receiveSelectedBankAccount(message.selectedAccount);
		}
	}
	
}
