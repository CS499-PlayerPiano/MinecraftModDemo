package plu.capstone.playerpiano.addon.minecraftmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import plu.capstone.playerpiano.addon.minecraftmod.piano.PianoController;

public class PlaySongCommand {

    private static final SongSuggester SONG_SUGGESTER = new SongSuggester();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("piano-playsong")
                .then(CommandManager.argument("song", StringArgumentType.string()).suggests(SONG_SUGGESTER)
                        .executes(PlaySongCommand::execute)));
    }

    private static int execute(CommandContext<ServerCommandSource> context) {

        boolean success = PianoController.getInstance().playSongWithUnderscores(StringArgumentType.getString(context, "song"));

        if (!success) {
            ModCommands.sendFeedback(context, "Failed to play song. Check the logs for more information.");
        }


        return 1;
    }

}
