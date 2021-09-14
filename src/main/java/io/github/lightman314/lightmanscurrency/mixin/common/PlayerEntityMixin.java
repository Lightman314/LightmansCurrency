package io.github.lightman314.lightmanscurrency.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import io.github.lightman314.lightmanscurrency.extendedinventory.ExtendedPlayerInventory;
import io.github.lightman314.lightmanscurrency.extendedinventory.container.ExtendedPlayerContainer;
import io.github.lightman314.lightmanscurrency.integration.backpacked.SuperExtensionHolder;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
//import io.github.lightman314.currencymod.integration.backpacked.container.SuperExtendedPlayerContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
	
	@Shadow
	@Final
	@Mutable
	public PlayerInventory inventory;
	
	@Shadow
	@Final
	@Mutable
	public PlayerContainer container;
	
    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void constructorTail(World world, BlockPos pos, float spawnAngle, GameProfile profile, CallbackInfo ci)
	{
    	//CurrencyMod.LOGGER.info("Attempting to run PlayerEntityMixin code.");
    	if(LightmansCurrency.isCuriosLoaded())
    		return;
    	
    	if(!LightmansCurrency.isBackpackedLoaded()) //Normal extension
    	{
    		//LightmansCurrency.LOGGER.info("Running PlayerEntityMixin code (NORMAL).");
			PlayerEntity player = (PlayerEntity)(Object)this;
			this.inventory = new ExtendedPlayerInventory(player);
			this.container = new ExtendedPlayerContainer(this.inventory, !world.isRemote, player);
			player.openContainer = this.container;
			return;
    	}
    	else
    	{
    		//Attempt super extension
    		//LightmansCurrency.LOGGER.info("Running PlayerEntityMixin code (BACKPACKED).");
			PlayerEntity player = (PlayerEntity)(Object)this;
			
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
    	/*else //Backpacked compatible extension
		{
			CurrencyMod.LOGGER.info("Running PlayerEntityMixin code (BACKPACKED).");
			PlayerEntity player = (PlayerEntity)(Object)this;
			this.inventory = new SuperExtendedPlayerInventory(player);
			this.container = new SuperExtendedPlayerContainer(this.inventory, !world.isRemote, player);
			player.openContainer = this.container;
			return;
		}*/
		
	}
	
}
