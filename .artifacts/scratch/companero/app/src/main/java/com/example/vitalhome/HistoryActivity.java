package com.example.vitalhome;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        RecyclerView recyclerView = findViewById(R.id.recyclerEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new EventAdapter(buildMockEvents()));

        findViewById(R.id.btnBackToHome).setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        setupBottomNav();
    }

    private List<Event> buildMockEvents() {
        List<Event> events = new ArrayList<>();
        events.add(new Event(Event.Type.FALL, "Caída detectada", "Hoy, 09:14 · Resuelta"));
        events.add(new Event(Event.Type.WATER, "Fuga de agua, cocina", "Ayer, 18:40 · Resuelta"));
        events.add(new Event(Event.Type.SOS_MANUAL, "SOS manual", "Lun, 20:02 · Resuelta"));
        return events;
    }

    private void setupBottomNav() {
        findViewById(R.id.navAlert).setOnClickListener(v ->
                startActivity(new Intent(this, AlertActivity.class)));

        findViewById(R.id.navProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }
}
