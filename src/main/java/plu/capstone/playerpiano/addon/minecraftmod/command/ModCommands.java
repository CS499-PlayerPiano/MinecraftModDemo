package plu.capstone.playerpiano.addon.minecraftmod.command;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ModCommands {

    public static void registerModCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            TestCommand.register(dispatcher);
            PauseUnpauseCommand.register(dispatcher);
        });
    }

    public static void sendFeedback(CommandContext<ServerCommandSource> context, String text) {
        sendFeedback(context, Text.of(text));
    }
    public static void sendFeedback(CommandContext<ServerCommandSource> context, Text msg) {
        context.getSource().sendFeedback(() -> msg, false);
    }
}
