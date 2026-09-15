package com.example.vitalhome.controladores;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vitalhome.R;
import com.example.vitalhome.adapter.MedicationAdapter;
import com.example.vitalhome.modelo.AlertEvent;
import com.example.vitalhome.modelo.Medication;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MedicamentosFragment extends Fragment implements MedicationAdapter.AccionesMed {

    private AppRepo repo;
    private MedicationAdapter adapter;
    private List<Medication> plan;
    private EditText txtNombre, txtHora;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_medicamentos, container, false);
        repo = new AppRepo(requireContext());

        txtNombre = v.findViewById(R.id.etMedNombre);
        txtHora = v.findViewById(R.id.etMedHora);
        Button btnAdd = v.findViewById(R.id.btnGuardarMed);

        txtHora.setOnClickListener(x -> pickTime());
        btnAdd.setOnClickListener(x -> addMed());
        
        v.findViewById(R.id.btnVolverMeds).setOnClickListener(x -> 
            Navigation.findNavController(x).navigate(R.id.nav_dashboard)
        );

        initList(v);

        return v;
    }

    private void initList(View v) {
        RecyclerView rv = v.findViewById(R.id.rvMedicamentos);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        plan = repo.getMeds();
        
        plan.sort((m1, m2) -> Boolean.compare(m2.isTaken(), m1.isTaken()));
        
        SharedPreferences p = requireContext().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
        boolean isAbuelo = "cliente".equals(p.getString("rol", "cuidador"));

        adapter = new MedicationAdapter(plan, isAbuelo, this);
        rv.setAdapter(adapter);
    }

    private void pickTime() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(requireContext(), (view, h, m) -> {
            txtHora.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m));
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void addMed() {
        String n = txtNombre.getText().toString().trim();
        String h = txtHora.getText().toString().trim();

        if (n.isEmpty() || h.isEmpty()) {
            Toast.makeText(requireContext(), "Faltan datos che", Toast.LENGTH_SHORT).show();
            return;
        }

        Medication nuevo = new Medication("" + System.currentTimeMillis(), n, h);
        plan.add(0, nuevo);
        plan.sort((m1, m2) -> Boolean.compare(m2.isTaken(), m1.isTaken()));
        
        repo.saveMeds(plan);
        adapter.notifyDataSetChanged();
        
        txtNombre.setText("");
        txtHora.setText("");
        Toast.makeText(requireContext(), "Guardado", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void tomar(Medication m) {
        m.setTaken(true);
        repo.saveMeds(plan);
        repo.addEvent(new AlertEvent("MEDICINA", "Tomó " + m.getNombre(), "Casa", getTime()));
        plan.sort((m1, m2) -> Boolean.compare(m2.isTaken(), m1.isTaken()));
        adapter.notifyDataSetChanged();
        Toast.makeText(requireContext(), "Registrado", Toast.LENGTH_SHORT).show();
    }

    private String getTime() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }
}