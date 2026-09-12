package com.example.vitalhome;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class HistorialMedicoFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historial_medico, container, false);

        RecyclerView rv = view.findViewById(R.id.rvHistorialMedico);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        List<MedicalRecord> data = new ArrayList<>();
        data.add(new MedicalRecord("10/09/2026", "Chequeo Cardiológico", "Dr. Ricardo Pérez", "Electrocardiograma normal. Ritmo sinusal estable. Próximo control en 6 meses."));
        data.add(new MedicalRecord("05/08/2026", "Análisis de Sangre", "Lab Vital Central", "Niveles de glucosa controlados. Colesterol dentro de los rangos óptimos."));
        data.add(new MedicalRecord("20/07/2026", "Consulta Oftalmológica", "Dra. Lucía Méndez", "Cambio de graduación en lentes de lectura. Sin signos de cataratas."));
        data.add(new MedicalRecord("12/06/2026", "Control de Presión", "Clínica del Sol", "Presión arterial estable (120/80) tras ajuste de medicación."));

        rv.setAdapter(new MedicalRecordAdapter(data));

        view.findViewById(R.id.btnVolverHistorial).setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.nav_dashboard)
        );

        return view;
    }
}