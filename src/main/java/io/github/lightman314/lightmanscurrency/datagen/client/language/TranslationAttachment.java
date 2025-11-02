package io.github.lightman314.lightmanscurrency.datagen.client.language;

import net.minecraft.data.PackOutput;

public abstract class TranslationAttachment extends TranslationProvider {

    private final TranslationProvider parent;

    protected TranslationAttachment(PackOutput output,TranslationProvider parent) {
        super(output);
        this.parent = parent;
    }

    @Override
    public void add(String key, String value) { this.parent.add(key,value); }

}