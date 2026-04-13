package io.storyflame.android;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import io.storyflame.core.model.Project;

public final class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView overviewText = findViewById(R.id.project_overview_text);
        Button loadExampleButton = findViewById(R.id.load_example_button);

        overviewText.setText(AndroidProjectOverviewFormatter.emptyState());
        loadExampleButton.setOnClickListener(view -> {
            Project project = AndroidProjectPreviewFactory.sampleProject();
            overviewText.setText(AndroidProjectOverviewFormatter.format(project));
        });
    }
}
