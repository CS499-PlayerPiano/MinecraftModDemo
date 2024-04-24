package plu.capstone.playerpiano.addon.minecraftmod.piano;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import plu.capstone.playerpiano.addon.minecraftmod.PianoMod;
import plu.capstone.playerpiano.addon.minecraftmod.Scroller;

public class PianoWSClient extends WebSocketClient {

    private static final Gson GSON = new Gson();

    //true is black key, false is white key
    private static final boolean[] KEYBOARD_LAYOUT = {false, true, false, false, true, false, true, false, false, true, false, true, false, true, false, false, true, false, true, false, false, true, false, true, false, true, false, false, true, false, true, false, false, true, false, true, false, true, false, false, true, false, true, false, false, true, false, true, false, true, false, false, true, false, true, false, false, true, false, true, false, true, false, false, true, false, true, false, false, true, false, true, false, true, false, false, true, false, true, false, false, true, false, true, false, true, false, false};

    private static final BlockState AIR = Blocks.AIR.getDefaultState();
    private static final BlockState WHITE = Blocks.QUARTZ_SLAB.getDefaultState();
    private static final BlockState BLACK = Blocks.GRAY_TERRACOTTA.getDefaultState();

    long uptime = 0;


    public PianoWSClient(String uri) throws URISyntaxException {
        super(new URI(uri));

        PianoMod.LOGGER.info("[WS] Connecting to " + uri);

        new Timer().scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {

                //update all blocks every 500ms
                //uptime
                updateSignText(new BlockPos(50, -8, -273), toSigntext(
                        EMPTY,
                        Text.literal("Uptime: "),
                        Text.literal("" + uptime).withColor(0xFF0000),
                        EMPTY
                ));

                //now playing
                updateSignText(new BlockPos(50, -9, -271), toSigntext(
                        Text.literal("Now Playing: "),
                        Text.literal(nowPlayingTitle.next()).withColor(0xFF0000),
                        nowPlayingTitle.getOrigMsg().equals("Nothing") ? EMPTY : Text.literal("by"),
                        Text.literal(nowPlayingArtist.next()).withColor(0xFF0000)
                ));

            }
        }, 500, 500);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        PianoMod.LOGGER.info("[WS] Connected to " + this.getURI());
    }

    @Override
    public void onMessage(String message) {

        try {
            JsonObject json = GSON.fromJson(message, JsonObject.class);

            if(json.has("packetId")) {
                String packetId = json.get("packetId").getAsString();
                JsonObject packet = json.get("data").getAsJsonObject();
                switch(packetId) {
                    case "connected": parseConnected(packet); break;
                    case "timestamp": parseTimestamp(packet); break;
                    case "notesPlayed": parseNotesPlayed(packet); break;
                    case "songFinished": parseSongFinished(packet); break;
                    case "songStarted": parseSongStart(packet); break;
                    case "queueUpdated": parseQueueUpdated(packet); break;
                    case "songPaused": parseSongPaused(packet); break;
                    case "statistics": parseStatistics(packet); break;
                    default: PianoMod.LOGGER.warn("[WS] Unknown packet ID: " + packetId); break;
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
           // PianoMod.LOGGER.error("[WS] Error parsing message: " + e.getMessage());
            //System.out.println(message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        PianoMod.LOGGER.info("[WS] Connection closed: " + code + " - " + reason);
    }

    @Override
    public void onError(Exception ex) {
        PianoMod.LOGGER.error("[WS] Error: " + ex.getMessage());
    }

    private void parseConnected(JsonObject packet) {
        String sessionId = packet.get("sessionID").getAsString();
        PianoMod.LOGGER.info("[WS] Connected with session ID: " + sessionId);
        PianoController.getInstance().onConnect(sessionId);
    }

    private Scroller nowPlayingTitle = new Scroller();
    private Scroller nowPlayingArtist = new Scroller();
    private Text EMPTY = Text.literal("");
    private void parseStatistics(JsonObject packet) {
        //TODO
        long totalNotesPlayed = packet.get("totalNotesPlayed").getAsLong();
        long totalSongsPlayed = packet.get("totalSongsPlayed").getAsLong();
        long totalSustainPedalPressed = packet.get("totalSustainPedalPressed").getAsLong();
        uptime = packet.get("uptime").getAsLong();

        JsonArray topSongs = packet.get("songsPlayed").getAsJsonArray();



        //total notes
        updateSignText(new BlockPos(50, -8, -272), toSigntext(
                EMPTY,
                Text.literal("Total Notes: "),
                Text.literal("" + totalNotesPlayed).withColor(0xFF0000),
                EMPTY
        ));

        //total songs
        updateSignText(new BlockPos(50, -8, -271), toSigntext(
                EMPTY,
                Text.literal("Total Plays: "),
                        Text.literal("" + totalSongsPlayed).withColor(0xFF0000),
                EMPTY
        ));

        //total sustain pedal
        updateSignText(new BlockPos(50, -8, -270), toSigntext(
                EMPTY,
                Text.literal("Total Pedal: "),
                Text.literal("" + totalSustainPedalPressed).withColor(0xFF0000),
                EMPTY
        ));


        //top songs


    }


    private void parseTimestamp(JsonObject packet) {
        //TODO
    }

    private void parseSongStart(JsonObject packet) {
        PianoMod.LOGGER.info("[WS] Song started: " + packet.toString());

        String title = packet.get("name").getAsString();
        JsonArray artists = packet.get("artists").getAsJsonArray();
        //comma separated list of artists
        StringBuilder artistString = new StringBuilder();
        for(int i = 0; i < artists.size(); i++) {
            artistString.append(artists.get(i).getAsString());
            if(i < artists.size() - 1) {
                artistString.append(", ");
            }
        }

        nowPlayingTitle.setMsg(title);
        nowPlayingArtist.setMsg(artistString.toString());

        resetPiano();
    }

    private void parseSongFinished(JsonObject packet) {
        PianoMod.LOGGER.info("[WS] Song finished: " + packet.toString());

        nowPlayingTitle.setMsg("Nothing");
        nowPlayingArtist.setMsg("");

        resetPiano();
    }

    private void parseSongPaused(JsonObject packet) {
        PianoMod.LOGGER.info("[WS] Song paused: " + packet.toString());
        //turn off the piano
        resetPiano();
    }

    private void parseQueueUpdated(JsonObject packet) {
        //TODO
    }

    private static final BlockPos STARTING_POS = new BlockPos( 92, -44, -209);

    private void resetPiano() {
        for(int i = 0; i < 88; i++) {
            boolean isBlackKey = KEYBOARD_LAYOUT[i];
            setKey(i, isBlackKey, false);
        }
    }

    private void parseNotesPlayed(JsonObject packet) {
        //Manipulate the piano

        JsonArray notes = packet.get("notes").getAsJsonArray();

        for(int i = 0; i < notes.size(); i++) {
            JsonObject note = notes.get(i).getAsJsonObject();
            byte key = note.get("keyNumber").getAsByte();
            boolean noteOn = note.get("noteOn").getAsBoolean();
            boolean isBlackKey = note.get("isBlackKey").getAsBoolean();

            int arrayIndex = key - 21;
            if(arrayIndex >= 0 && arrayIndex < 88) {
                setKey(arrayIndex, isBlackKey, noteOn);
            }

            //PianoMod.LOGGER.info("[WS] Note played: " + key + " - " + noteOn);
        }

    }

    private void setKey(int arrayIndex, boolean isBlackKey, boolean noteOn) {
        BlockPos backPos = STARTING_POS.subtract(new Vec3i(arrayIndex, 0, 0));
        BlockPos backPosOn = backPos.down();
        if(noteOn) {
            if(isBlackKey) {
                drawBlackKey(backPos, AIR);
                drawBlackKey(backPosOn, BLACK);
            }
            else {
                drawWhiteKey(backPos, AIR);
                drawWhiteKey(backPosOn, WHITE);
            }
        }
        else {
            if(isBlackKey) {
                drawBlackKey(backPos, BLACK);
                drawBlackKey(backPosOn, AIR);
            }
            else {
                drawWhiteKey(backPos, WHITE);
                drawWhiteKey(backPosOn, AIR);
            }
        }

    }

    private void drawBlackKey(BlockPos backPos, BlockState state) {
        changeBlock(backPos, state);
        changeBlock(backPos.north(), state);
        changeBlock(backPos.north(2), state);
        changeBlock(backPos.north(3), state);
    }

    private void drawWhiteKey(BlockPos backPos, BlockState state) {
        changeBlock(backPos, state);
        changeBlock(backPos.north(), state);
        changeBlock(backPos.north(2), state);
        changeBlock(backPos.north(3), state);
        changeBlock(backPos.north(4), state);
        changeBlock(backPos.north(5), state);
    }



    private void changeBlock(BlockPos pos, BlockState bs) {
        if(MinecraftClient.getInstance().isIntegratedServerRunning()) {
            IntegratedServer server = MinecraftClient.getInstance().getServer();
            ServerWorld overworld = server.getOverworld();

            overworld.setBlockState(pos, bs);
        }
        else {
            PianoMod.LOGGER.error("[WS] Not connected to an integrated server!");
        }
    }

    private SignText toSigntext(Text... text) {
        SignText signText = new SignText();
        for(int i = 0; i < Math.max(text.length, 4); i++) {
            signText = signText.withMessage(i, text[i]);
        }
        return signText;
    }

    private void updateSignText(BlockPos pos, SignText signText) {
        if(MinecraftClient.getInstance().isIntegratedServerRunning()) {
            IntegratedServer server = MinecraftClient.getInstance().getServer();
            ServerWorld overworld = server.getOverworld();

            server.executeSync(() -> {

                overworld.getBlockEntity(pos, BlockEntityType.SIGN).ifPresent(sign -> {
                    //System.out.println("updated sign " + pos);
                    sign.setText(signText, true);

                    sign.markDirty();
                });

            });
        }
        else {
            PianoMod.LOGGER.error("[WS] Not connected to an integrated server!");
        }
    }
}
