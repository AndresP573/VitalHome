package com.example.vitalhome.ui.controladores;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.vitalhome.R;
import com.example.vitalhome.data.modelo.AlertEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SosCountdownFragment extends Fragment {

    private TextView tvCountdown;
    private CountDownTimer timer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sos_countdown, container, false);
        
        tvCountdown = v.findViewById(R.id.textCountdown);
        
        v.findViewById(R.id.btnCancelSOS).setOnClickListener(view -> {
            if (timer != null) timer.cancel();
            Navigation.findNavController(view).navigateUp();
        });

        v.findViewById(R.id.btnConfirmSOSNow).setOnClickListener(view -> {
            if (timer != null) timer.cancel();
            enviarAlertaInmediata(view);
        });

        v.findViewById(R.id.btnBackArrowSOS).setOnClickListener(view -> {
            if (timer != null) timer.cancel();
            Navigation.findNavController(view).navigateUp();
        });

        startTimer(v);

        return v;
    }

    private void startTimer(View root) {
        timer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvCountdown.setText("Notificando en " + (millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                enviarAlertaInmediata(root);
            }
        }.start();
    }

    private void enviarAlertaInmediata(View v) {
        // Lógica de alerta
        AppRepo repo = new AppRepo(requireContext());
        String hora = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        repo.addEvent(new AlertEvent("SOS", "Emergencia confirmada", "Casa", hora));
        
        NotificationHelper.sendSOS(requireContext(), "Familiar");
        
        // Navegar a la pantalla roja de "Avisando" que ya tenías
        Navigation.findNavController(v).navigate(R.id.nav_sos);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }
}