package io.storyflame.app.electron;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.storyflame.app.project.ProjectApplicationService;
import io.storyflame.app.project.ProjectCharacterApplicationService;
import io.storyflame.app.project.ProjectEditorApplicationService;
import io.storyflame.app.project.ProjectStructureApplicationService;
import io.storyflame.app.project.ProjectTagApplicationService;
import io.storyflame.core.analysis.EmotionAnalysisService;
import io.storyflame.core.storage.ProjectArchiveStore;
import io.storyflame.core.storage.ProjectBackupService;
import io.storyflame.core.storage.ProjectStoragePaths;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public final class StoryFlameElectronBridgeServer {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    public static void main(String[] args) throws IOException {
        int port = parsePort(args);
        HttpServer server = createServer(port, newDefaultSessionService());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0), "storyflame-electron-bridge-shutdown"));
        server.start();
        System.out.println("STORYFLAME_ELECTRON_BRIDGE_PORT=" + server.getAddress().getPort());
    }

    static HttpServer createServer(int port, ElectronBridgeSessionService sessionService) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.setExecutor(Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable, "storyflame-electron-bridge");
            thread.setDaemon(true);
            return thread;
        }));
        registerRoutes(server, sessionService);
        return server;
    }

    static ElectronBridgeSessionService newDefaultSessionService() {
        var store = new ProjectArchiveStore(ProjectStoragePaths.defaultDesktopProjectsDirectory());
        var backups = new ProjectBackupService(ProjectStoragePaths.defaultDesktopBackupsDirectory(), 8, Duration.ofMinutes(5));
        return new ElectronBridgeSessionService(
                new ProjectApplicationService(store, backups),
                new ProjectCharacterApplicationService(),
                new ProjectEditorApplicationService(),
                new ProjectStructureApplicationService(),
                new ProjectTagApplicationService(),
                new EmotionAnalysisService()
        );
    }

    private static void registerRoutes(HttpServer server, ElectronBridgeSessionService sessionService) {
        server.createContext("/api/health", exchange -> handle(exchange, "GET", () -> ok("Bridge ativo.", new HealthDto("ok"))));
        server.createContext("/api/base-directory", exchange -> handle(exchange, "GET", () -> ok(
                "Diretorio base carregado.",
                new BaseDirectoryDto(sessionService.baseDirectory())
        )));
        server.createContext("/api/projects/local", exchange -> handle(exchange, "GET", () -> ok(
                "Projetos locais carregados.",
                sessionService.localProjects()
        )));
        server.createContext("/api/session", exchange -> handle(exchange, "GET", () -> {
            var result = sessionService.currentSession();
            return ok(result.message(), result.session());
        }));
        server.createContext("/api/projects/create", exchange -> handle(exchange, "POST", () -> {
            CreateProjectPayload payload = readJson(exchange, CreateProjectPayload.class);
            var result = sessionService.createProject(payload == null ? "" : payload.title, payload == null ? "" : payload.author);
            return ok(result.message(), result.session());
        }));
        server.createContext("/api/projects/open", exchange -> handle(exchange, "POST", () -> {
            OpenProjectPayload payload = requirePayload(readJson(exchange, OpenProjectPayload.class), "Informe o caminho do arquivo.");
            var result = sessionService.openProject(payload.path);
            return ok(result.message(), result.session());
        }));
        server.createContext("/api/session/metadata", exchange -> handle(exchange, "POST", () -> {
            MetadataPayload payload = readJson(exchange, MetadataPayload.class);
            var result = sessionService.updateMetadata(payload == null ? "" : payload.title, payload == null ? "" : payload.author);
            return ok(result.message(), result.session());
        }));
        server.createContext("/api/session/select-scene", exchange -> handle(exchange, "POST", () -> {
            SelectScenePayload payload = requirePayload(readJson(exchange, SelectScenePayload.class), "Informe capitulo e cena.");
            var result = sessionService.selectScene(payload.chapterId, payload.sceneId);
            return ok(result.message(), result.session());
        }));
        server.createContext("/api/session/scene", exchange -> handle(exchange, "POST", () -> {
            SceneDraftPayload payload = readJson(exchange, SceneDraftPayload.class);
            var result = sessionService.updateSceneDraft(
                    payload == null ? "" : payload.title,
                    payload == null ? "" : payload.synopsis,
                    payload == null ? "" : payload.content
            );
            return ok(result.message(), result.session());
        }));
        server.createContext("/api/characters/select", exchange -> handle(exchange, "POST", () -> {
            CharacterSelectPayload payload = requirePayload(readJson(exchange, CharacterSelectPayload.class), "Informe o personagem.");
            var result = sessionService.selectCharacter(payload.characterId);
            return ok(result.message(), result.session());
        }));
        server.createContext("/api/characters/create", exchange -> handle(exchange, "POST", () -> {
            CharacterPayload payload = readJson(exchange, CharacterPayload.class);
            var result = sessionService.createCharacter(
                    payload == null ? "" : payload.name,
                    payload == null ? "" : payload.description
            );
            return ok(result.message(), result.session());
        }));
        server.createContext("/api/characters/update", exchange -> handle(exchange, "POST", () -> {
            CharacterPayload payload = readJson(exchange, CharacterPayload.class);
            var result = sessionService.updateCharacter(
                    payload == null ? "" : payload.name,
                    payload == null ? "" : payload.description
            );
            return ok(result.message(), result.session());
        }));
        server.createContext("/api/characters/delete", exchange -> handle(exchange, "POST", () -> {
            var result = sessionService.deleteCharacter();
            return ok(result.message(), result.session());
        }));
        server.createContext("/api/tags/select", exchange -> handle(exchange, "POST", () -> {
            TagSelectPayload payload = requirePayload(readJson(exchange, TagSelectPayload.class), "Informe a tag.");
            var result = sessionService.selectTag(payload.tagId);
            return ok(result.message(), result.session());
        }));
        server.createContext("/api/tags/create", exchange -> handle(exchange, "POST", () -> {
            TagPayload payload = readJson(exchange, TagPayload.class);
            var result = sessionService.createTag(
                    payload == null ? "" : payload.id,
                    payload == null ? "" : payload.label,
                    payload == null ? "" : payload.template
            );
            return ok(result.message(), result.session());
        }));
        server.createContext("/api/tags/update", exchange -> handle(exchange, "POST", () -> {
            TagPayload payload = readJson(exchange, TagPayload.class);
            var result = sessionService.updateTag(
                    payload == null ? "" : payload.id,
                    payload == null ? "" : payload.label,
                    payload == null ? "" : payload.template
            );
            return ok(result.message(), result.session());
        }));
        server.createContext("/api/tags/delete", exchange -> handle(exchange, "POST", () -> {
            var result = sessionService.deleteTag();
            return ok(result.message(), result.session());
        }));
        server.createContext("/api/search/run", exchange -> handle(exchange, "POST", () -> {
            SearchPayload payload = readJson(exchange, SearchPayload.class);
            return ok("Busca executada.", sessionService.search(payload == null ? "" : payload.query));
        }));
        server.createContext("/api/analysis/emotion/run", exchange -> handle(exchange, "POST", () -> {
            return ok("Analise emocional executada.", sessionService.runEmotionAnalysis());
        }));
        server.createContext("/api/analysis/emotion/current", exchange -> handle(exchange, "GET", () -> {
            return ok("Analise emocional carregada.", sessionService.currentEmotionAnalysis());
        }));
        server.createContext("/api/structure/chapter/add", exchange -> handle(exchange, "POST", () -> {
            var result = sessionService.addChapter();
            return ok(result.message(), result.session());
        }));
        server.createContext("/api/structure/chapter/remove", exchange -> handle(exchange, "POST", () -> {
            var result = sessionService.removeChapter();
            return ok(result.message(), result.session());
        }));
        server.createContext("/api/structure/chapter/move", exchange -> handle(exchange, "POST", () -> {
            OffsetPayload payload = readJson(exchange, OffsetPayload.class);
            var result = sessionService.moveChapter(payload == null ? 0 : payload.offset);
            return ok(result.message(), result.session());
        }));
        server.createContext("/api/structure/scene/add", exchange -> handle(exchange, "POST", () -> {
            var result = sessionService.addScene();
            return ok(result.message(), result.session());
        }));
        server.createContext("/api/structure/scene/remove", exchange -> handle(exchange, "POST", () -> {
            var result = sessionService.removeScene();
            return ok(result.message(), result.session());
        }));
        server.createContext("/api/structure/scene/move", exchange -> handle(exchange, "POST", () -> {
            OffsetPayload payload = readJson(exchange, OffsetPayload.class);
            var result = sessionService.moveScene(payload == null ? 0 : payload.offset);
            return ok(result.message(), result.session());
        }));
        server.createContext("/api/projects/save", exchange -> handle(exchange, "POST", () -> {
            var result = sessionService.saveProject();
            return ok(result.message(), result.session());
        }));
        server.createContext("/api/projects/delete", exchange -> handle(exchange, "POST", () -> {
            OpenProjectPayload payload = requirePayload(readJson(exchange, OpenProjectPayload.class), "Informe o caminho do arquivo.");
            var result = sessionService.deleteLocalProject(payload.path);
            return ok(result.message(), result.session());
        }));
        server.createContext("/api/profiles/select", exchange -> handle(exchange, "POST", () -> {
            ProfileSelectPayload payload = requirePayload(readJson(exchange, ProfileSelectPayload.class), "Informe o personagem.");
            var result = sessionService.selectProfile(payload.characterId);
            return ok(result.message(), result.session());
        }));
        server.createContext("/api/profiles/prefix", exchange -> handle(exchange, "POST", () -> {
            ProfilePrefixPayload payload = readJson(exchange, ProfilePrefixPayload.class);
            var result = sessionService.updateProfilePrefix(payload == null ? "" : payload.prefix);
            return ok(result.message(), result.session());
        }));
        server.createContext("/api/profiles/tags/add", exchange -> handle(exchange, "POST", () -> {
            ProfileTagPayload payload = requirePayload(readJson(exchange, ProfileTagPayload.class), "Informe a tag.");
            var result = sessionService.addTagToProfile(payload.tagId);
            return ok(result.message(), result.session());
        }));
        server.createContext("/api/profiles/tags/remove", exchange -> handle(exchange, "POST", () -> {
            ProfileTagPayload payload = requirePayload(readJson(exchange, ProfileTagPayload.class), "Informe a tag.");
            var result = sessionService.removeTagFromProfile(payload.tagId);
            return ok(result.message(), result.session());
        }));
    }

    private static void handle(HttpExchange exchange, String expectedMethod, Callable<Object> supplier) throws IOException {
        try {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 204, "");
                return;
            }
            if (!expectedMethod.equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, error("Metodo nao suportado.", List.of("Use " + expectedMethod + ".")));
                return;
            }
            sendJson(exchange, 200, supplier.call());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            sendJson(exchange, 400, error(messageOf(exception, "Requisicao invalida."), List.of(messageOf(exception, "Requisicao invalida."))));
        } catch (Exception exception) {
            sendJson(exchange, 500, error(messageOf(exception, "Falha interna no bridge."), List.of(messageOf(exception, "Falha interna no bridge."))));
        } finally {
            exchange.close();
        }
    }

    private static <T> T requirePayload(T payload, String message) {
        if (payload == null) {
            throw new IllegalArgumentException(message);
        }
        return payload;
    }

    private static <T> T readJson(HttpExchange exchange, Class<T> type) throws IOException {
        if (exchange.getRequestBody() == null) {
            return null;
        }
        try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, type);
        } catch (JsonSyntaxException exception) {
            throw new IllegalArgumentException("JSON invalido.");
        }
    }

    private static BridgeResponse ok(String message, Object data) {
        return new BridgeResponse("ok", Objects.requireNonNullElse(message, ""), data, List.of());
    }

    private static BridgeResponse error(String message, List<String> errors) {
        return new BridgeResponse("error", Objects.requireNonNullElse(message, ""), null, errors == null ? List.of() : List.copyOf(errors));
    }

    private static void sendJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
        send(exchange, statusCode, GSON.toJson(body));
    }

    private static void send(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "application/json; charset=utf-8");
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static int parsePort(String[] args) {
        for (String arg : args) {
            if (arg != null && arg.startsWith("--port=")) {
                return Integer.parseInt(arg.substring("--port=".length()));
            }
        }
        return 0;
    }

    private static String messageOf(Exception exception, String fallback) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? fallback : message;
    }

    private record BridgeResponse(String status, String message, Object data, List<String> errors) {
    }

    private record CreateProjectPayload(String title, String author) {
    }

    private record OpenProjectPayload(String path) {
    }

    private record MetadataPayload(String title, String author) {
    }

    private record SelectScenePayload(String chapterId, String sceneId) {
    }

    private record SceneDraftPayload(String title, String synopsis, String content) {
    }

    private record CharacterSelectPayload(String characterId) {
    }

    private record CharacterPayload(String name, String description) {
    }

    private record TagSelectPayload(String tagId) {
    }

    private record TagPayload(String id, String label, String template) {
    }

    private record SearchPayload(String query) {
    }

    private record OffsetPayload(int offset) {
    }

    private record HealthDto(String status) {
    }

    private record BaseDirectoryDto(String path) {
    }

    private record ProfileSelectPayload(String characterId) {
    }

    private record ProfilePrefixPayload(String prefix) {
    }

    private record ProfileTagPayload(String tagId) {
    }

}
