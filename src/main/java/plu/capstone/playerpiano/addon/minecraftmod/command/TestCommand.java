package plu.capstone.playerpiano.addon.minecraftmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class TestCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("hello").executes(TestCommand::execute));
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        String text = "Hello, " + context.getSource().getPlayer().getDisplayName().getString() + "!";
        ModCommands.sendFeedback(context, text);
        return 1;
    }

}
