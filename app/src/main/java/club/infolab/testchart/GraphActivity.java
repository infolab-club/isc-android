package club.infolab.testchart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class GraphActivity extends AppCompatActivity {
    public static final String EXTRA_TEST = "testName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        Intent intent = getIntent();
        String testName = intent.getStringExtra(EXTRA_TEST);
        TextView nameTestView = findViewById(R.id.name_test);
        nameTestView.setText(testName);
    }
}
