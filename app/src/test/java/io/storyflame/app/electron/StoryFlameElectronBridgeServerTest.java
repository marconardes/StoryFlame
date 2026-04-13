package io.storyflame.app.electron;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpServer;
import io.storyflame.app.project.ProjectApplicationService;
import io.storyflame.app.project.ProjectCharacterApplicationService;
import io.storyflame.app.project.ProjectEditorApplicationService;
import io.storyflame.app.project.ProjectStructureApplicationService;
import io.storyflame.app.project.ProjectTagApplicationService;
import io.storyflame.core.analysis.EmotionAnalysisService;
import io.storyflame.core.storage.ProjectArchiveStore;
import io.storyflame.core.storage.ProjectBackupService;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class StoryFlameElectronBridgeServerTest {
    private static final Gson GSON = new Gson();

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void servesSessionLifecycleOverHttp(@TempDir Path tempDir) throws Exception {
        ElectronBridgeSessionService sessionService = new ElectronBridgeSessionService(
                new ProjectApplicationService(
                        new ProjectArchiveStore(tempDir.resolve("projects")),
                        new ProjectBackupService(tempDir.resolve("backups"), 4, Duration.ZERO)
                ),
                new ProjectCharacterApplicationService(),
                new ProjectEditorApplicationService(),
                new ProjectStructureApplicationService(),
                new ProjectTagApplicationService(),
                new EmotionAnalysisService()
        );
        server = StoryFlameElectronBridgeServer.createServer(0, sessionService);
        server.start();
        HttpClient client = HttpClient.newHttpClient();
        int port = server.getAddress().getPort();

        JsonObject health = postOrGetJson(client, port, "/api/health", "GET", null);
        assertEquals("ok", health.get("status").getAsString());

        JsonObject created = postOrGetJson(
                client,
                port,
                "/api/projects/create",
                "POST",
                "{\"title\":\"Projeto HTTP\",\"author\":\"Autora HTTP\"}"
        );
        assertEquals("ok", created.get("status").getAsString());
        assertEquals("Projeto HTTP", created.getAsJsonObject("data").getAsJsonObject("project").get("title").getAsString());
        String createdPath = created.getAsJsonObject("data").get("path").getAsString();

        JsonObject local = postOrGetJson(client, port, "/api/projects/local", "GET", null);
        assertTrue(local.getAsJsonArray("data").size() >= 1);

        JsonObject addScene = postOrGetJson(client, port, "/api/structure/scene/add", "POST", "{}");
        assertEquals("Cena criada.", addScene.get("message").getAsString());

        JsonObject createCharacter = postOrGetJson(
                client,
                port,
                "/api/characters/create",
                "POST",
                "{\"name\":\"Lina\",\"description\":\"Heroina\"}"
        );
        assertEquals("Personagem criado.", createCharacter.get("message").getAsString());

        JsonObject createTag = postOrGetJson(
                client,
                port,
                "/api/tags/create",
                "POST",
                "{\"id\":\"falc1\",\"label\":\"Falha\",\"template\":\"Texto\"}"
        );
        assertEquals("Tag criada.", createTag.get("message").getAsString());

        JsonObject search = postOrGetJson(
                client,
                port,
                "/api/search/run",
                "POST",
                "{\"query\":\"Projeto\"}"
        );
        assertEquals("ok", search.get("status").getAsString());

        JsonObject analysis = postOrGetJson(
                client,
                port,
                "/api/analysis/emotion/run",
                "POST",
                "{}"
        );
        assertEquals("ok", analysis.get("status").getAsString());

        JsonObject deleted = postOrGetJson(
                client,
                port,
                "/api/projects/delete",
                "POST",
                "{\"path\":\"" + createdPath.replace("\\", "\\\\") + "\"}"
        );
        assertEquals("ok", deleted.get("status").getAsString());

        JsonObject invalidRemove = postOrGetJson(client, port, "/api/structure/chapter/remove", "POST", "{}");
        assertEquals("error", invalidRemove.get("status").getAsString());
    }

    private JsonObject postOrGetJson(HttpClient client, int port, String route, String method, String body)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + route));
        if ("POST".equals(method)) {
            builder.header("Content-Type", "application/json");
            builder.POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
        } else {
            builder.GET();
        }
        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return GSON.fromJson(response.body(), JsonObject.class);
    }
}
