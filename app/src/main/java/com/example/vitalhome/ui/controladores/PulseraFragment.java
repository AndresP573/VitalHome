package com.example.vitalhome.ui.controladores;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.vitalhome.R;
import androidx.lifecycle.ViewModelProvider;
import com.example.vitalhome.ui.viewmodels.PulseraViewModel;

/**
 * Fragmento que muestra los detalles de los signos vitales obtenidos desde la pulsera inteligente.
 * Consulta y actualiza los signos vitales en vivo desde Cloud Firestore al abrir o reanudar la pantalla.
 */
public class PulseraFragment extends Fragment {

    // --- Bloque 1: Atributo ViewModel ---
    private PulseraViewModel viewModel;

    // --- Bloque 2: Inflado e inicialización de vistas ---
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pulsera, container, false);
        viewModel = new ViewModelProvider(this).get(PulseraViewModel.class);
        
        TextView pulso = v.findViewById(R.id.tvPulsoReal);
        TextView oxig = v.findViewById(R.id.tvOxigenoReal);
        TextView pres = v.findViewById(R.id.tvPresionReal);

        viewModel.getPulso().observe(getViewLifecycleOwner(), pulso::setText);
        viewModel.getOxigeno().observe(getViewLifecycleOwner(), oxig::setText);
        viewModel.getPresion().observe(getViewLifecycleOwner(), pres::setText);

        View arrowBack = v.findViewById(R.id.btnBackArrowPulsera);
        if (arrowBack != null) {
            arrowBack.setOnClickListener(x -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        }
        
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.cargarSignosVitalesDesdeFirestore();
        }
    }
}
