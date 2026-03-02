package io.storyflame.android;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import io.storyflame.core.model.Project;
import io.storyflame.core.storage.ProjectArchiveStore;
import java.nio.file.Path;

public final class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Path projectsDirectory = getFilesDir().toPath().resolve("projects");
        ProjectArchiveStore store = new ProjectArchiveStore(projectsDirectory);
        Project project = store.createProject("Android Sample", "StoryFlame");
        Path savedPath = store.save(project);
        Project loaded = store.open(savedPath);

        TextView textView = new TextView(this);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(32, 32, 32, 32);
        textView.setText(
                "StoryFlame Android\n\n"
                        + "Persistencia local ativa.\n"
                        + "Projeto salvo em:\n" + savedPath + "\n\n"
                        + "Titulo carregado: " + loaded.getTitle()
        );

        setContentView(textView);
    }
}
