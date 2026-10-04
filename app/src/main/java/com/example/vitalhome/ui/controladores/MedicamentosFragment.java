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

/**
 * Fragmento para la gestión y alta de medicamentos dentro del plan diario.
 * Permite agregar nuevos medicamentos mediante TimePickerDialog y registrarlos como tomados.
 */
public class MedicamentosFragment extends Fragment implements MedicationAdapter.AccionesMed {

    // --- Bloque 1: Atributos y referencias de UI ---
    private MedicamentosViewModel viewModel;
    private MedicationAdapter adapter;
    private EditText txtNombre, txtHora;
    private TimePickerDialog timePickerDialog;

    // --- Bloque 2: Inflado e inicialización de componentes ---
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

        View arrowBack = v.findViewById(R.id.btnBackArrowMeds);
        if (arrowBack != null) {
            arrowBack.setOnClickListener(x -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        }

        View btnLimpiar = v.findViewById(R.id.btnLimpiarRemedios);
        if (btnLimpiar != null) {
            btnLimpiar.setOnClickListener(x -> {
                viewModel.limpiarPlanYArchivarEnNube();
                Toast.makeText(requireContext(), getString(R.string.msg_remedios_limpios), Toast.LENGTH_SHORT).show();
            });
        }

        initList(v);

        return v;
    }

    // --- Bloque 3: Observación de cambios en la lista ---
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getPlan().observe(getViewLifecycleOwner(), lista -> {
            if (adapter != null && lista != null) {
                adapter.updateList(lista);
            }
        });
    }

    // --- Bloque 4: Inicialización del RecyclerView ---
    private void initList(View v) {
        RecyclerView rv = v.findViewById(R.id.rvMedicamentos);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<Medication> plan = viewModel.getPlan().getValue();

        SharedPreferences p = requireContext().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
        boolean isAbuelo = "cliente".equals(p.getString("rol", "cuidador"));

        adapter = new MedicationAdapter(plan, isAbuelo, this);
        rv.setAdapter(adapter);
    }

    // --- Bloque 5: Selección de hora mediante TimePickerDialog ---
    private void pickTime() {
        if (timePickerDialog != null && timePickerDialog.isShowing()) {
            timePickerDialog.dismiss();
        }
        Calendar c = Calendar.getInstance();
        timePickerDialog = new TimePickerDialog(requireContext(), (view, h, m) -> {
            if (txtHora != null) {
                txtHora.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m));
            }
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    // --- Bloque 6: Adición de nuevos medicamentos ---
    private void addMed() {
        String n = txtNombre.getText().toString().trim();
        String h = txtHora.getText().toString().trim();

        if (n.isEmpty() || h.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.msg_faltan_datos), Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.agregar(n, h);

        txtNombre.setText("");
        txtHora.setText("");
        Toast.makeText(requireContext(), getString(R.string.msg_guardado), Toast.LENGTH_SHORT).show();
    }

    // --- Bloque 7: Acción de marcar medicamento como tomado ---
    @Override
    public void tomar(Medication m) {
        viewModel.tomar(m);
        Toast.makeText(requireContext(), getString(R.string.msg_registrado), Toast.LENGTH_SHORT).show();
    }

    // --- Bloque 8: Limpieza de recursos en onDestroyView ---
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timePickerDialog != null && timePickerDialog.isShowing()) {
            timePickerDialog.dismiss();
        }
        timePickerDialog = null;

        txtNombre = null;
        txtHora = null;
        adapter = null;
    }
}
