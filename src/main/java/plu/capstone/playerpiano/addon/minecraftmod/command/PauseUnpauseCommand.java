package plu.capstone.playerpiano.addon.minecraftmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import plu.capstone.playerpiano.addon.minecraftmod.piano.PianoController;

public class PauseUnpauseCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("piano-pause").executes(PauseUnpauseCommand::execute));
    }

    private static int execute(CommandContext<ServerCommandSource> context) {

        PianoController.getInstance().pauseUnpauseSong();

        return 1;
    }

}
