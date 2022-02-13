package net.zenxarch.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.data.server.RecipesProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import com.mojang.brigadier.arguments.StringArgumentType;
import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;

@Environment(EnvType.CLIENT)
public class EarlyGameBotMod implements ClientModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("zenbot");
    boolean hasToCraft;
	boolean waitForBaritone;
	@Override
	public void onInitializeClient() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		LOGGER.info("ZenBot loaded have fun botting.");
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			if(waitForBaritone){
				if(!BaritoneAPI.getProvider().getPrimaryBaritone().getGetToBlockProcess().isActive()){
                    hasToCraft = true;
					waitForBaritone = false;
				}
			}
			if(hasToCraft){
                if(mc.currentScreen instanceof CraftingScreen){
					if(mc.player.currentScreenHandler.getSlot(0).getStack().getItem().equals(Items.FURNACE)){
						mc.interactionManager.clickSlot(
							mc.player.currentScreenHandler.syncId, 
							0, 0, SlotActionType.QUICK_CRAFT, mc.player);
					} else {
					    RecipeManager rm = mc.getNetworkHandler().getRecipeManager();
					    for(Recipe recipe: rm.values()){
						    if(recipe.getOutput().getItem().equals(Items.FURNACE)){
							    continue;
						    }
                            mc.interactionManager.clickRecipe(
							    mc.player.currentScreenHandler.syncId,
						    	recipe,false);
							break;
					    }
					}
				} else {
		            // make player click
					hasToCraft = false;
				}
			}
		});
		ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal("zcraft").then(
			ClientCommandManager.argument("item",StringArgumentType.string()).executes(
                context -> {
					// Obviously we are not in a crafting menu
			        try {
						IBaritone pb = BaritoneAPI.getProvider().getPrimaryBaritone();
						pb.getGetToBlockProcess().getToBlock(Blocks.CRAFTING_TABLE);
						waitForBaritone = true;
					} catch (Exception e) {
						//TODO: handle exception
					}
                    return 0;
				}
			)
		));
		
	}

	public static Logger getLogger() {
		return LOGGER;
	}
}
