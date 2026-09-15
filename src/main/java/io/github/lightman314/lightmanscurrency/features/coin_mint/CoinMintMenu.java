package io.github.lightman314.lightmanscurrency.features.coin_mint;

import io.github.lightman314.lightmanscurrency.api.world.menu.FancyMenu;
import io.github.lightman314.lightmanscurrency.api.world.menu.slots.EasyResourceSlot;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.builtin.BlockEntityValidator;
import io.github.lightman314.lightmanscurrency.core.LCMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CoinMintMenu extends FancyMenu.Validated {

    private final CoinMintBlockEntity blockEntity;
    public CoinMintBlockEntity getBlockEntity() { return this.blockEntity; }

    private final ContainerData data;

    public CoinMintMenu(int containerID,Player player,CoinMintBlockEntity blockEntity) { this(LCMenuTypes.COIN_MINT.get(),containerID,player,blockEntity); }
    protected CoinMintMenu(MenuType<?> type,int containerId,Player player,CoinMintBlockEntity blockEntity) {
        super(type,containerId,player,new BlockEntityValidator(blockEntity));
        this.blockEntity = blockEntity;
        this.data = this.blockEntity.getMenuData();

        //Slots
        this.addSlot(new EasyResourceSlot(this.blockEntity.getStorage(),0,56,21));
        this.addSlot(new EasyResourceSlot.OutputOnly(this.blockEntity.getStorage(),1,116,21));

        //Inventory Slots
        Inventory inventory = player.getInventory();
        for(int y = 0; y < 3; ++y) {
            for(int x = 0; x < 9; ++x)
                this.addSlot(new Slot(inventory,9 + x + y * 9,8 + x * 18,56 + y * 18));
        }

        //Hotbar
        for(int x = 0; x < 9; ++x)
            this.addSlot(new Slot(inventory,x,8 + x * 18,114));

        this.addDataSlots(this.data);

    }

    public float getMintProgress() {
        int mintTime = this.data.get(0);
        int totalMintTime = this.data.get(1);
        if(mintTime <= 0 || totalMintTime <= 0)
            return 0f;
        return Math.clamp((float)mintTime / (float)totalMintTime,0f,1f);
    }

    @Override
    public ItemStack quickMoveStack(Player player,int index) {
        ItemStack clickedStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if(slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            clickedStack = slotStack.copy();
            if(index < this.blockEntity.getStorage().size()) {
                if(!this.moveStackTo(slotStack,this.blockEntity.getStorage().size(),this.slots.size(),true))
                    return ItemStack.EMPTY;
            }
            else if(!this.moveStackTo(slotStack,0,this.blockEntity.getStorage().size() - 1,false))
                return ItemStack.EMPTY;

            if(slotStack.isEmpty())
                slot.set(ItemStack.EMPTY);
            else
                slot.setChanged();

        }
        return clickedStack;
    }

}
