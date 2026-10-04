package com.example.vitalhome.ui.controladores;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.vitalhome.R;
import com.example.vitalhome.service.GeminiService;

public class AsistenteFragment extends Fragment {

    private EditText etPregunta;
    private TextView tvRespuestaIA;
    private Button btnEnviarIA;
    private GeminiService geminiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_asistente, container, false);

        etPregunta = root.findViewById(R.id.etPregunta);
        tvRespuestaIA = root.findViewById(R.id.tvRespuestaIA);
        btnEnviarIA = root.findViewById(R.id.btnEnviarIA);
        geminiService = new GeminiService();

        btnEnviarIA.setOnClickListener(v -> enviarConsulta());

        return root;
    }

    private void enviarConsulta() {
        String pregunta = etPregunta.getText().toString().trim();
        if (pregunta.isEmpty()) {
            Toast.makeText(getContext(), "Por favor escribe una duda o pregunta", Toast.LENGTH_SHORT).show();
            return;
        }

        tvRespuestaIA.setText("🤖 VitalBot está pensando...");
        etPregunta.setText("");

        geminiService.sendMessage(pregunta, new GeminiService.GeminiCallback() {
            @Override
            public void onSuccess(String responseText) {
                if (getContext() != null) {
                    tvRespuestaIA.setText(responseText);
                }
            }

            @Override
            public void onError(String error) {
                if (getContext() != null) {
                    tvRespuestaIA.setText("❌ Error al consultar a VitalBot: " + error);
                }
            }
        });
    }
}
