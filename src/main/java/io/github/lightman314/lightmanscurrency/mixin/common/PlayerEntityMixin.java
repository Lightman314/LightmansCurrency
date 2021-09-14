package io.github.lightman314.lightmanscurrency.mixin.common;

//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Mutable;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//import com.mojang.authlib.GameProfile;

//import io.github.lightman314.lightmanscurrency.extendedinventory.ExtendedPlayerInventory;
//import io.github.lightman314.lightmanscurrency.extendedinventory.container.ExtendedPlayerContainer;
//import io.github.lightman314.lightmanscurrency.integration.backpacked.SuperExtensionHolder;
//import io.github.lightman314.lightmanscurrency.LightmansCurrency;
//import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
//import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
//import net.minecraft.world.level.Level;

//@Mixin(Player.class)
public class PlayerEntityMixin {
	
	//@Shadow
	//@Final
	//@Mutable
	public Inventory inventory;
	
	//@Shadow
	//@Final
	//@Mutable
	public InventoryMenu container;
	
    /*@Inject(method = "<init>", at = @At(value = "TAIL"))
    private void constructorTail(Level level, BlockPos pos, float spawnAngle, GameProfile profile, CallbackInfo ci)
	{
    	//CurrencyMod.LOGGER.info("Attempting to run PlayerEntityMixin code.");
    	if(LightmansCurrency.isCuriosLoaded())
    		return;
    	
    	if(!LightmansCurrency.isBackpackedLoaded()) //Normal extension
    	{
    		//LightmansCurrency.LOGGER.info("Running PlayerEntityMixin code (NORMAL).");
			Player player = (Player)(Object)this;
			this.inventory = new ExtendedPlayerInventory(player);
			this.container = new ExtendedPlayerContainer(this.inventory, !world.isRemote, player);
			player.openContainer = this.container;
			return;
    	}
    	else
    	{
    		//Attempt super extension
    		//LightmansCurrency.LOGGER.info("Running PlayerEntityMixin code (BACKPACKED).");
			Player player = (Player)(Object)this;
			
			try {
				SuperExtensionHolder extensionHolder = new SuperExtensionHolder(player, !world.isRemote);
				this.inventory = extensionHolder.getInventory();
				this.container = extensionHolder.getContainer();
				player.openContainer = this.container;
			} catch (ClassNotFoundException exception)
			{
				LightmansCurrency.LogError("Error loading Backpacked compatible Extended Inventory or Container.");
			}
    	}
		
	}*/
	
}
