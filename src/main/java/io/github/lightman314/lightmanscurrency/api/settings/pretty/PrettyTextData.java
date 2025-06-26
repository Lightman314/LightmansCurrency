package io.github.lightman314.lightmanscurrency.api.settings.pretty;

import net.minecraft.network.chat.Component;

import java.util.List;

public record PrettyTextData(Component machineName, List<Component> lines) { }
