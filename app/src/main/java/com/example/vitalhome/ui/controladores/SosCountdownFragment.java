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
import com.example.vitalhome.ui.viewmodels.SosCountdownViewModel;

public class SosCountdownFragment extends Fragment {

    private SosCountdownViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sos_countdown, container, false);
        viewModel = new ViewModelProvider(this).get(SosCountdownViewModel.class);
        
        TextView tvCountdown = v.findViewById(R.id.textCountdown);

        viewModel.getCountdownText().observe(getViewLifecycleOwner(), tvCountdown::setText);
        viewModel.getTimerFinished().observe(getViewLifecycleOwner(), finished -> {
            if (Boolean.TRUE.equals(finished)) {
                viewModel.eventoConsumido();
                Navigation.findNavController(v).navigate(R.id.nav_sos);
            }
        });
        
        v.findViewById(R.id.btnCancelSOS).setOnClickListener(view -> {
            viewModel.estoyBien();
            Navigation.findNavController(view).navigateUp();
        });

        v.findViewById(R.id.btnConfirmSOSNow).setOnClickListener(view -> {
            viewModel.confirmSOS();
        });

        v.findViewById(R.id.btnBackArrowSOS).setOnClickListener(view -> {
            viewModel.cancelTimer();
            Navigation.findNavController(view).navigateUp();
        });

        viewModel.startTimer();

        return v;
    }
}
