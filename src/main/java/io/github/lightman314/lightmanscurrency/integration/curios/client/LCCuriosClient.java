package io.github.lightman314.lightmanscurrency.integration.curios.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCTags;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import io.github.lightman314.lightmanscurrency.client.helpers.KeyInputHelpers;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletItem;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCuriosHelper;
import io.github.lightman314.lightmanscurrency.network.message.curios.CPacketOpenATM;
import io.github.lightman314.lightmanscurrency.network.message.curios.CPacketOpenTerminal;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;
import top.theillusivec4.curios.api.client.ICurioRenderer;

@Mod(value = LCApi.MODID,dist = Dist.CLIENT,depends = "curios")
public class LCCuriosClient {

    public static final KeyMapping KEY_TERMINAL = new KeyMapping(LCText.Resources.KEY_TERMINAL.getKey(),KeyConflictContext.IN_GAME,InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_BACKSLASH,KeyMapping.Category.INVENTORY);
    public static final KeyMapping KEY_ATM = new KeyMapping(LCText.Resources.KEY_ATM.getKey(),KeyConflictContext.IN_GAME,InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_EQUAL,KeyMapping.Category.INVENTORY);

    public LCCuriosClient(IEventBus modBus) {
        //Register Event Listeners
        modBus.addListener(LCCuriosClient::clientSetup);
        modBus.addListener(LCCuriosClient::registerKeyBindings);
        NeoForge.EVENT_BUS.addListener(LCCuriosClient::clientTick);
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for(var holder : LCItems.REGISTER.getEntries()) {
                if(holder.get() instanceof WalletItem item)
                    ICurioRenderer.register(item,WalletCurioRenderer.SOURCE);
            }
        });
    }

    private static void clientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if(KeyInputHelpers.consumeAllClicks(KEY_TERMINAL) && player != null && mc.screen == null) {
            if(LCCuriosHelper.get().hasItem(player,s -> s.is(LCTags.Items.NETWORK_TERMINAL)))
                CPacketOpenTerminal.sendPacket();
        }

        if(KeyInputHelpers.consumeAllClicks(KEY_ATM) && player != null && mc.screen == null) {
            if(LCCuriosHelper.get().hasItem(player,s -> s.is(LCTags.Items.ATM)))
                CPacketOpenATM.sendPacket();
        }

    }

    private static void registerKeyBindings(RegisterKeyMappingsEvent event) {
        event.register(KEY_TERMINAL);
        event.register(KEY_ATM);
    }

}
