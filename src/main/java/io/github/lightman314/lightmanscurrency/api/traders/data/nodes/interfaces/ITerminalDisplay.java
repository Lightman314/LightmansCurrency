package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public interface ITerminalDisplay {

    default void applyTerminalTextColor(AtomicInteger color) {}
    default void addTerminalInfo(List<Component> tooltip, @Nullable Player player) {}

}
