package com.example.vitalhome.ui.controladores;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.vitalhome.R;
import com.example.vitalhome.ui.viewmodels.HogarViewModel;

/**
 * Fragmento para el monitoreo y simulación de sensores del hogar (puerta y detección de fuego/humo).
 */
public class HogarFragment extends Fragment {

    // --- Bloque 1: Atributos de la clase ---
    private HogarViewModel viewModel;

    // --- Bloque 2: Inflado e integración con ViewModel ---
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_hogar, container, false);
        viewModel = new ViewModelProvider(this).get(HogarViewModel.class);
        
        TextView tvEstadoPuerta = v.findViewById(R.id.tvEstadoPuerta);
        Button btnSimularPuerta = v.findViewById(R.id.btnSimularPuerta);
        TextView tvEstadoFuego = v.findViewById(R.id.tvEstadoFuego);
        Button btnSimularFuego = v.findViewById(R.id.btnSimularFuego);

        // Observación del estado del sensor de la puerta
        viewModel.getPuertaAbierta().observe(getViewLifecycleOwner(), abierta -> {
            if (abierta) {
                tvEstadoPuerta.setText(getString(R.string.door_open));
                tvEstadoPuerta.setTextColor(Color.parseColor("#EF4444"));
                btnSimularPuerta.setText(getString(R.string.btn_close_door));
            } else {
                tvEstadoPuerta.setText(getString(R.string.door_closed));
                tvEstadoPuerta.setTextColor(Color.parseColor("#10B981"));
                btnSimularPuerta.setText(getString(R.string.btn_simulate_door_open));
            }
        });

        // Observación del estado del sensor de fuego
        viewModel.getFuegoDetectado().observe(getViewLifecycleOwner(), fuego -> {
            if (fuego) {
                tvEstadoFuego.setText(getString(R.string.fire_detected));
                tvEstadoFuego.setTextColor(Color.parseColor("#EF4444"));
                btnSimularFuego.setText(getString(R.string.btn_turn_off_alarm));
            } else {
                tvEstadoFuego.setText(getString(R.string.smoke_none));
                tvEstadoFuego.setTextColor(Color.parseColor("#10B981"));
                btnSimularFuego.setText(getString(R.string.btn_simulate_fire));
            }
        });

        // Listeners para las simulaciones de sensores
        btnSimularPuerta.setOnClickListener(view -> viewModel.togglePuerta());
        btnSimularFuego.setOnClickListener(view -> viewModel.toggleFuego());

        // Navegación de retorno mediante la flecha
        View arrowBack = v.findViewById(R.id.btnBackArrowHogar);
        if (arrowBack != null) {
            arrowBack.setOnClickListener(x -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        }
        
        return v;
    }
}
