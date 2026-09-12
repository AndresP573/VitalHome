package com.example.vitalhome;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class DashboardCuidadorFragment extends Fragment {

    private AppRepo repo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dashboard_cuidador, container, false);
        repo = new AppRepo(requireContext());

        SharedPreferences prefs = requireContext().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
        String u = prefs.getString("nombre", "Usuario");
        String a = prefs.getString("nombreAdultoMayor", "Familiar");

        ((TextView)v.findViewById(R.id.tvSaludo)).setText("Hola, " + u);
        
        TextView lbl = v.findViewById(R.id.tvLabelPulsera);
        if (lbl != null) lbl.setText("⌚ Pulsera de " + a);
        
        setClickListeners(v);
        actualizarBadges(v);

        return v;
    }

    private void actualizarBadges(View v) {
        int cant = repo.cargarPlanMedico().size();
        TextView bad = v.findViewById(R.id.tvMedsSummary);
        if (bad != null) bad.setText(cant + " en el plan");
    }

    private void setClickListeners(View v) {
        v.findViewById(R.id.cardVitals).setOnClickListener(x -> 
            Navigation.findNavController(x).navigate(R.id.nav_pulsera)
        );

        v.findViewById(R.id.cardMeds).setOnClickListener(x -> 
            Navigation.findNavController(x).navigate(R.id.nav_medicamentos)
        );

        v.findViewById(R.id.btnHogar).setOnClickListener(x -> 
            Navigation.findNavController(x).navigate(R.id.nav_hogar)
        );

        v.findViewById(R.id.btnHistorialMedico).setOnClickListener(x -> 
            Navigation.findNavController(x).navigate(R.id.nav_historial_medico)
        );
    }
}