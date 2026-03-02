package io.storyflame.core.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.Instant;

final class JsonMapperFactory {
    private JsonMapperFactory() {
    }

    static Gson create() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
                .create();
    }
}

