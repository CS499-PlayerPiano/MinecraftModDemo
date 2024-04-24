package plu.capstone.playerpiano.addon.minecraftmod;

import java.net.URISyntaxException;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plu.capstone.playerpiano.addon.minecraftmod.command.ModCommands;
import plu.capstone.playerpiano.addon.minecraftmod.piano.PianoWSClient;

public class PianoMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("pianomod");

	public static PianoWSClient wsClient;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");

        ModCommands.registerModCommands();

        try {
            wsClient = new PianoWSClient("ws://localhost:8898/ws");
            wsClient.connect();
        } catch (URISyntaxException e) {
            LOGGER.error("Failed to create WebSocket client: " + e.getMessage());
        }

    }
}