package io.github.lightman314.lightmanscurrency.features.tickets.recipe;

import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.resource.NormalItemStorage;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public final class TicketStationRecipeInput implements RecipeInput, ISidedContext {

    public static final int MAX_CODE_LENGTH = 16;
    public static final String VALID_CODE_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_ ";

    public static boolean isValidCode(String code) {
        if(code.isEmpty())
            return false;
        if(code.length() > MAX_CODE_LENGTH)
            return false;
        for(int i = 0; i < code.length(); ++i) {
            if(!validCodeChar(code.charAt(i)))
                return false;
        }
        return true;
    }

    public static boolean validCodeChar(char codeChar) { return VALID_CODE_CHARS.indexOf(codeChar) != -1; }

    private final ISidedContext context;
    private final NormalItemStorage storage;
    private final TicketRecipeData data;
    public TicketStationRecipeInput(ISidedContext context,NormalItemStorage storage,TicketRecipeData data) {
        this.context = context;
        this.storage = storage;
        this.data = data;
    }

    @Override
    public boolean isClient() { return this.context.isClient(); }

    @Override
    public ItemStack getItem(int index) { return this.storage.getStack(index); }

    @Override
    public int size() { return this.storage.size(); }

    public TicketRecipeData getData() { return this.data; }

    public String getCode() { return this.data.code(); }
    public int getDurability() { return this.data.durability(); }

}
