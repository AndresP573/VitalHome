package com.example.vitalhome.controladores;

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
import com.example.vitalhome.R;

public class DashboardCuidadorFragment extends Fragment {

    private AppRepo repo;
    private View root;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_dashboard_cuidador, container, false);
        repo = new AppRepo(requireContext());
        
        setClickListeners(root);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (root != null) {
            actualizarInterfaz(root);
        }
    }

    private void actualizarInterfaz(View v) {
        SharedPreferences prefs = requireContext().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
        String u = prefs.getString("nombre", "Usuario");
        String a = prefs.getString("nombreAdultoMayor", "Familiar");

        ((TextView)v.findViewById(R.id.tvSaludo)).setText("Hola, " + u);
        
        TextView lbl = v.findViewById(R.id.tvLabelPulsera);
        if (lbl != null) lbl.setText("⌚ Pulsera de " + a);
        
        int cant = repo.getMeds().size();
        
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