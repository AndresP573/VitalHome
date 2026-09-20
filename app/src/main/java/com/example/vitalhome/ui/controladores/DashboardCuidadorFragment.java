package com.example.vitalhome.ui.controladores;

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
import androidx.lifecycle.ViewModelProvider;
import com.example.vitalhome.ui.viewmodels.DashboardCuidadorViewModel;

public class DashboardCuidadorFragment extends Fragment {

    private DashboardCuidadorViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard_cuidador, container, false);
        viewModel = new ViewModelProvider(this).get(DashboardCuidadorViewModel.class);
        
        TextView tvSaludo = root.findViewById(R.id.tvSaludo);
        TextView tvLabelPulsera = root.findViewById(R.id.tvLabelPulsera);
        TextView tvMedsSummary = root.findViewById(R.id.tvMedsSummary);

        viewModel.getNombreUsuario().observe(getViewLifecycleOwner(), s -> tvSaludo.setText("Hola, " + s));
        viewModel.getNombreAdultoMayor().observe(getViewLifecycleOwner(), s -> {
            if (tvLabelPulsera != null) tvLabelPulsera.setText("⌚ Pulsera de " + s);
        });
        viewModel.getMedicinasCount().observe(getViewLifecycleOwner(), count -> {
            if (tvMedsSummary != null) tvMedsSummary.setText(count + " en el plan");
        });

        setClickListeners(root);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refreshData();
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