package plu.capstone.playerpiano.addon.minecraftmod.piano;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Consumer;


public class PianoController {

    HttpClient client = HttpClient.newHttpClient();

    private static PianoController instance;

    private String uuid;

    public static PianoController getInstance() {
        if(instance == null) instance = new PianoController();
        return instance;
    }

    public void onConnect(String uuid) {
        this.uuid = uuid;
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

}
