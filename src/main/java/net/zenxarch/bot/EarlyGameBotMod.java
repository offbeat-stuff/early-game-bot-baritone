package net.zenxarch.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import com.mojang.brigadier.arguments.StringArgumentType;

@Environment(EnvType.CLIENT)
public class EarlyGameBotMod implements ClientModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("zenbot");

	@Override
	public void onInitializeClient() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		LOGGER.info("ZenBot loaded have fun botting.");
		ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal("zcraft").then(
			ClientCommandManager.argument("item",StringArgumentType.string()).executes(
                context -> {

				}
			)
		));
		
	}

	public static Logger getLogger() {
		return LOGGER;
	}
}
