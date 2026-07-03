package io.github.lightman314.lightmanscurrency.api.client.gui.sprites;

import net.minecraft.resources.Identifier;

public record SimpleSizedSprite(Identifier sprite,int width,int height) implements SizedSprite { }