package io.github.lightman314.lightmanscurrency.features.coin_mint;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerServer;
import io.github.lightman314.lightmanscurrency.api.world.blockentity.EasyBlockEntity;
import io.github.lightman314.lightmanscurrency.core.LCBlockEntities;
import io.github.lightman314.lightmanscurrency.core.LCRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;
import java.util.Optional;

public class CoinMintBlockEntity extends EasyBlockEntity implements ITickerServer, MenuProvider {

    @Nullable
    private Component name = null;
    @Nullable
    public Component getCustomName() { return this.name; }

    private final CoinMintStorage storage = new CoinMintStorage();
    public final CoinMintStorage getStorage() { return this.storage; }
    public final ResourceHandler<ItemResource> getItemCapability() { return this.storage.externalAccess; }

    public SingleRecipeInput getRecipeInput() { return new SingleRecipeInput(this.storage.getStack(0)); }

    private Optional<RecipeHolder<CoinMintRecipe>> lastRelevantRecipe = Optional.empty();
    public Optional<Identifier> getCurrentRecipe() { return this.lastRelevantRecipe.map(h -> h.id().identifier()); }
    private int mintTime = 0;
    public int getMintTime() { return this.mintTime; }
    private int clientTotalMintTime = 0;
    private int getRecipeDuration() {
        if(this.isClient())
            return this.clientTotalMintTime;
        return this.lastRelevantRecipe.map(h -> h.value().getDuration()).orElse(0);
    }

    private final ContainerData menuData = new MenuData();
    public ContainerData getMenuData() { return this.menuData; }

    public CoinMintBlockEntity(BlockPos worldPosition, BlockState blockState) { this(LCBlockEntities.COIN_MINT.get(),worldPosition,blockState); }
    protected CoinMintBlockEntity(BlockEntityType<?> type,BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
        this.storage.setListener(this::onStorageChanged);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.storage.serialize(output,"storage");
        output.putInt("timer",this.mintTime);
        output.storeNullable("name",ComponentSerialization.CODEC,this.name);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.storage.deserialize(input,"storage");
        this.mintTime = input.getIntOr("timer",this.mintTime);
        this.name = input.read("name",ComponentSerialization.CODEC).orElse(this.name);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        this.name = components.get(DataComponents.CUSTOM_NAME);
        this.storage.copyFrom(components.getOrDefault(DataComponents.CONTAINER,ItemContainerContents.EMPTY).allItemsCopyStream().toList());
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if(this.name != null)
            components.set(DataComponents.CUSTOM_NAME,this.name);
        if(!this.storage.isEmpty())
            components.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(this.storage.getContents()));
    }

    //Don't know why this is deprecated when vanilla chest BE's use this same thing
    // to remove implicit components from the item
    // My best guess is they're removing this completely in the future in favor of only using implicit components
    @Override
    @SuppressWarnings("deprecation")
    public void removeComponentsFromTag(ValueOutput output) {
        output.discard("name");
        output.discard("storage");
        output.discard("timer");
    }

    @Override
    public void onLoad() {
        this.lastRelevantRecipe = this.getRelevantRecipe();
    }

    private void onStorageChanged() {
        this.setChanged();
        this.checkRecipes();
    }

    public void checkRecipes() {
        Optional<RecipeHolder<CoinMintRecipe>> newRecipe = this.getRelevantRecipe();
        if(!this.lastRelevantRecipe.equals(newRecipe)) {
            this.lastRelevantRecipe = newRecipe;
            this.mintTime = 0;
            this.setChanged();
        }
    }

    @Override
    public void serverTick() {
        if(this.lastRelevantRecipe.isPresent()) {
            CoinMintRecipe recipe = this.lastRelevantRecipe.get().value();
            if(recipe.matches(this.getRecipeInput(),this.level) && this.storage.getAmountAsInt(0) >= recipe.getIngredientCount() && this.hasOutputSpace()) {
                this.mintTime++;
                if(this.mintTime >= recipe.getDuration()) {
                    this.mintTime = 0;
                    this.mintCoin();
                    float volume = LCConfig.SERVER.coinMintSoundVolume.get();
                    if(volume > 0)
                        this.level.playSound(null,this.worldPosition,SoundEvents.ANVIL_LAND,SoundSource.BLOCKS,volume,1f);
                }
                this.setChanged();
            }
        }
        else if(this.mintTime > 0) {
            this.mintTime = 0;
            this.setChanged();
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        for(int i = 0; i < this.storage.size(); ++i)
            Block.popResource(this.level,pos,this.storage.getStack(0));
        this.storage.clear();
    }

    public boolean hasOutputSpace() {
        //Determine how many more coins can fit in the output slot based on the item input
        if(this.lastRelevantRecipe.isEmpty())
            return false;
        CoinMintRecipe recipe = this.lastRelevantRecipe.get().value();
        ItemStack mintOuput = recipe.assemble(this.getRecipeInput());
        ItemStack currentoutputSlot = this.storage.getStack(1);
        if(currentoutputSlot.isEmpty())
            return true;
        else if(!ItemStack.isSameItemSameComponents(mintOuput,currentoutputSlot))
            return false;
        return currentoutputSlot.getMaxStackSize() - currentoutputSlot.getCount() >= mintOuput.getCount();
    }

    @Nullable
    public Optional<RecipeHolder<CoinMintRecipe>> getRelevantRecipe() {
        SingleRecipeInput recipeInput = this.getRecipeInput();
        if(recipeInput.isEmpty())
            return Optional.empty();
        if(this.level instanceof ServerLevel sl)
            return sl.getServer().getRecipeManager().getRecipeFor(LCRecipeTypes.COIN_MINT.get(),recipeInput,sl);
        return Optional.empty();
    }

    private void mintCoin() {
        this.lastRelevantRecipe = this.getRelevantRecipe();
        if(this.lastRelevantRecipe.isEmpty())
            return;

        CoinMintRecipe recipe = this.lastRelevantRecipe.get().value();
        SingleRecipeInput input = this.getRecipeInput();
        ItemStack output = recipe.assemble(input);
        if(output.isEmpty())
            return;

        //Extract the ingredients, and insert the resulting items
        try(Transaction transaction = Transaction.openRoot()) {
            int extracted = this.storage.extract(0,ItemResource.of(input.getItem(0)),recipe.getIngredientCount(),transaction);
            if(extracted == recipe.getIngredientCount()) {
                int inserted = this.storage.insert(1,ItemResource.of(output),output.getCount(),transaction);
                if(inserted == output.getCount())
                    transaction.commit();
            }
        }
    }

    @Override
    public Component getDisplayName() { return this.name == null ? this.getBlockState().getBlock().getName() : this.name; }
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) { return new CoinMintMenu(containerId,player,this); }
    @Override
    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) { buffer.writeBlockPos(this.worldPosition); }

    private class MenuData implements ContainerData {
        @Override
        public int get(int dataId) {
            return switch (dataId) {
                case 0 -> CoinMintBlockEntity.this.mintTime;
                case 1 -> CoinMintBlockEntity.this.getRecipeDuration();
                default -> 0;
            };
        }
        @Override
        public void set(int dataId,int value) {
            switch (dataId) {
                case 0 -> CoinMintBlockEntity.this.mintTime = value;
                case 1 -> CoinMintBlockEntity.this.clientTotalMintTime = value;
            }
        }
        @Override
        public int getCount() { return 2; }

    }

}
