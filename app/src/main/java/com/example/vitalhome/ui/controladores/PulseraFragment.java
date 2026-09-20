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
import com.example.vitalhome.ui.viewmodels.PulseraViewModel;

public class PulseraFragment extends Fragment {

    private PulseraViewModel viewModel;

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

        v.findViewById(R.id.btnVolverPulsera).setOnClickListener(view -> 
            requireActivity().getOnBackPressedDispatcher().onBackPressed()
        );

        View arrowBack = v.findViewById(R.id.btnBackArrowPulsera);
        if (arrowBack != null) {
            arrowBack.setOnClickListener(x -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        }
        
        return v;
    }
}