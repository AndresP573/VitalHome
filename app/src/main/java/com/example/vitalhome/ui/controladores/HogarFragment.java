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

public class HogarFragment extends Fragment {

    private HogarViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_hogar, container, false);
        viewModel = new ViewModelProvider(this).get(HogarViewModel.class);
        
        TextView tvEstadoPuerta = v.findViewById(R.id.tvEstadoPuerta);
        Button btnSimularPuerta = v.findViewById(R.id.btnSimularPuerta);
        TextView tvEstadoFuego = v.findViewById(R.id.tvEstadoFuego);
        Button btnSimularFuego = v.findViewById(R.id.btnSimularFuego);

        viewModel.getPuertaAbierta().observe(getViewLifecycleOwner(), abierta -> {
            if (abierta) {
                tvEstadoPuerta.setText("ABIERTA");
                tvEstadoPuerta.setTextColor(Color.parseColor("#EF4444"));
                btnSimularPuerta.setText("Cerrar puerta");
            } else {
                tvEstadoPuerta.setText("CERRADA");
                tvEstadoPuerta.setTextColor(Color.parseColor("#10B981"));
                btnSimularPuerta.setText("Simular apertura");
            }
        });

        viewModel.getFuegoDetectado().observe(getViewLifecycleOwner(), fuego -> {
            if (fuego) {
                tvEstadoFuego.setText("¡FUEGO!");
                tvEstadoFuego.setTextColor(Color.parseColor("#EF4444"));
                btnSimularFuego.setText("Apagar alarma");
            } else {
                tvEstadoFuego.setText("SIN HUMO");
                tvEstadoFuego.setTextColor(Color.parseColor("#10B981"));
                btnSimularFuego.setText("Simular fuego");
            }
        });

        btnSimularPuerta.setOnClickListener(view -> viewModel.togglePuerta());
        btnSimularFuego.setOnClickListener(view -> viewModel.toggleFuego());

        v.findViewById(R.id.btnVolverHogar).setOnClickListener(view -> 
            requireActivity().getOnBackPressedDispatcher().onBackPressed()
        );

        View arrowBack = v.findViewById(R.id.btnBackArrowHogar);
        if (arrowBack != null) {
            arrowBack.setOnClickListener(x -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        }
        
        return v;
    }
}
