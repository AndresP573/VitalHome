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
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vitalhome.R;
import com.example.vitalhome.data.modelo.Medication;
import com.example.vitalhome.ui.adapter.MedicationAdapter;
import com.example.vitalhome.ui.viewmodels.AppViewModelFactory;
import com.example.vitalhome.ui.viewmodels.MedicamentosViewModel;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Fragmento para la gestión y alta de medicamentos dentro del plan diario.
 * Observa la máquina de estados de MedicamentosViewModel e integra ProgressBar de carga.
 */
public class MedicamentosFragment extends Fragment implements MedicationAdapter.AccionesMed {

    private MedicamentosViewModel viewModel;
    private MedicationAdapter adapter;
    private EditText txtNombre, txtHora;
    private ProgressBar pbMedicamentos;
    private Button btnAdd;
    private TimePickerDialog timePickerDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_medicamentos, container, false);
        AppViewModelFactory factory = new AppViewModelFactory(requireActivity().getApplication());
        viewModel = new ViewModelProvider(this, factory).get(MedicamentosViewModel.class);

        txtNombre = v.findViewById(R.id.etMedNombre);
        txtHora = v.findViewById(R.id.etMedHora);
        btnAdd = v.findViewById(R.id.btnGuardarMed);
        pbMedicamentos = v.findViewById(R.id.pbMedicamentos);

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel.getPlan().observe(getViewLifecycleOwner(), lista -> {
            if (adapter != null && lista != null) {
                adapter.updateList(lista);
            }
        });

        viewModel.getEstado().observe(getViewLifecycleOwner(), estado -> {
            if (estado == null) return;
            switch (estado) {
                case CARGANDO:
                    if (pbMedicamentos != null) pbMedicamentos.setVisibility(View.VISIBLE);
                    if (btnAdd != null) btnAdd.setEnabled(false);
                    break;
                case EXITO:
                case VACIO:
                    if (pbMedicamentos != null) pbMedicamentos.setVisibility(View.GONE);
                    if (btnAdd != null) btnAdd.setEnabled(true);
                    break;
                case ERROR:
                    if (pbMedicamentos != null) pbMedicamentos.setVisibility(View.GONE);
                    if (btnAdd != null) btnAdd.setEnabled(true);
                    Toast.makeText(requireContext(), "Error al cargar medicamentos", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
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

    private void addMed() {
        String n = txtNombre != null ? txtNombre.getText().toString().trim() : "";
        String h = txtHora != null ? txtHora.getText().toString().trim() : "";

        if (n.isEmpty() || h.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.msg_faltan_datos), Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.agregar(n, h);

        if (txtNombre != null) txtNombre.setText("");
        if (txtHora != null) txtHora.setText("");
        Toast.makeText(requireContext(), getString(R.string.msg_guardado), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void tomar(Medication m) {
        viewModel.tomar(m);
        Toast.makeText(requireContext(), getString(R.string.msg_registrado), Toast.LENGTH_SHORT).show();
    }

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
