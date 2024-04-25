package plu.capstone.playerpiano.addon.minecraftmod.piano;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sk89q.worldedit.util.report.SystemInfoReport;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class PianoController {

    HttpClient client = HttpClient.newHttpClient();

    private static PianoController instance;

    private String uuid;

    private Gson gson = new Gson();

    public static PianoController getInstance() {
        if(instance == null) instance = new PianoController();
        return instance;
    }

    JsonArray songList;

    public void onConnect(String uuid) {
        this.uuid = uuid;

        sendGetRequest("songs", body -> {
            try {
                songList = gson.fromJson(body, JsonArray.class);
                System.out.println(songList);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void pauseUnpauseSong() {
        sendPostRequest("control/pause", "", System.out::println);
    }

    private void sendPostRequest(String url, String body, Consumer<String> callback) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8898/api/" + url))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(body))
                .build();
        client.sendAsync(request, BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(callback);

    }

    private void sendGetRequest(String url, Consumer<String> callback) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8898/api/" + url))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        client.sendAsync(request, BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(callback);

    }

    public void skipSong() {
        sendPostRequest("control/skip", "", System.out::println);
    }

    public List<String> getSongTitlesWithUnderscores() {

        List<String> titles = new ArrayList<>();

        for(int i = 0; i < songList.size(); i++) {
            JsonObject song = songList.get(i).getAsJsonObject();
            String name = song.get("name").getAsString();
            name = name.replace(" ", "_");
            titles.add(name);
        }

        return titles;
    }

    public boolean playSongWithUnderscores(String song) {
        song = song.replace("_", " ");

        for(int i = 0; i < songList.size(); i++) {
            JsonObject songObj = songList.get(i).getAsJsonObject();
            if(songObj.get("name").getAsString().equalsIgnoreCase(song)) {
                sendPostRequest("control/play", songObj.toString(), System.out::println);
                return true;
            }
        }
        return false;
    }
}
