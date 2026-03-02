package io.storyflame.android;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import io.storyflame.core.archive.ProjectArchiveLayout;

public final class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textView = new TextView(this);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(32, 32, 32, 32);
        textView.setText(
                "StoryFlame Android\n\n"
                        + "Semana 1 inicializada.\n"
                        + "ZIP v" + ProjectArchiveLayout.SPEC_VERSION + " definido."
        );

        setContentView(textView);
    }
}

