package com.example.vitalhome.ui.controladores;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vitalhome.R;
import com.example.vitalhome.ui.adapter.MedicationAdapter;
import com.example.vitalhome.data.modelo.Medication;
import com.example.vitalhome.ui.viewmodels.MedicamentosViewModel;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MedicamentosFragment extends Fragment implements MedicationAdapter.AccionesMed {

    private MedicamentosViewModel viewModel;
    private MedicationAdapter adapter;
    private EditText txtNombre, txtHora;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_medicamentos, container, false);
        viewModel = new ViewModelProvider(this).get(MedicamentosViewModel.class);

        txtNombre = v.findViewById(R.id.etMedNombre);
        txtHora = v.findViewById(R.id.etMedHora);
        Button btnAdd = v.findViewById(R.id.btnGuardarMed);

        txtHora.setOnClickListener(x -> pickTime());
        btnAdd.setOnClickListener(x -> addMed());

        v.findViewById(R.id.btnVolverMeds).setOnClickListener(x ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed()
        );

        View arrowBack = v.findViewById(R.id.btnBackArrowMeds);
        if (arrowBack != null) {
            arrowBack.setOnClickListener(x -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        }

        initList(v);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getPlan().observe(getViewLifecycleOwner(), lista -> adapter.notifyDataSetChanged());
    }

    private void initList(View v) {
        RecyclerView rv = v.findViewById(R.id.rvMedicamentos);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<Medication> plan = viewModel.getPlan().getValue();

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

        viewModel.agregar(n, h);

        txtNombre.setText("");
        txtHora.setText("");
        Toast.makeText(requireContext(), "Guardado", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void tomar(Medication m) {
        viewModel.tomar(m);
        Toast.makeText(requireContext(), "Registrado", Toast.LENGTH_SHORT).show();
    }
}