package com.example.vitalhome.ui.controladores;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.example.vitalhome.R;
import com.example.vitalhome.data.repository.AppRepo;
import com.example.vitalhome.service.GeminiService;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Fragmento que administra la interacción con el Asistente IA (VitalBot).
 * Maneja estados visuales con ProgressBar, deshabilitación de controles durante la carga y registro explícito de excepciones.
 */
public class AsistenteFragment extends Fragment {

    private static final String TAG = "AsistenteFragment";

    private EditText etPregunta;
    private TextView tvRespuestaIA;
    private Button btnEnviarIA;
    private ProgressBar pbAsistente;
    private ScrollView scrollViewAsistente;
    private GeminiService geminiService;
    private AppRepo appRepo;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_asistente, container, false);

        etPregunta = root.findViewById(R.id.etPregunta);
        tvRespuestaIA = root.findViewById(R.id.tvRespuestaIA);
        btnEnviarIA = root.findViewById(R.id.btnEnviarIA);
        pbAsistente = root.findViewById(R.id.pbAsistente);
        scrollViewAsistente = root.findViewById(R.id.scrollViewAsistente);

        appRepo = new AppRepo(requireContext());
        prefs = requireContext().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
        geminiService = new GeminiService();

        tvRespuestaIA.setText(appRepo.getChatHistory());
        scrollToBottom();

        btnEnviarIA.setOnClickListener(v -> enviarConsulta());

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            Insets systemInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            int bottomPadding = imeInsets.bottom > 0 ? imeInsets.bottom : systemInsets.bottom;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottomPadding);

            return windowInsets;
        });

        return root;
    }

    private void enviarConsulta() {
        String pregunta = etPregunta.getText().toString().trim();
        if (pregunta.isEmpty()) {
            Toast.makeText(getContext(), "Por favor escribe una duda o pregunta", Toast.LENGTH_SHORT).show();
            return;
        }

        setControlesCargando(true);

        String currentTranscript = tvRespuestaIA.getText().toString();
        String nuevoTranscript = currentTranscript + "\n\n👤 Tú:\n" + pregunta + "\n\n🤖 VitalBot:\nPensando...";
        tvRespuestaIA.setText(nuevoTranscript);
        appRepo.saveChatHistory(nuevoTranscript);
        etPregunta.setText("");
        scrollToBottom();

        geminiService.sendMessageWithContext(requireContext(), pregunta, new GeminiService.GeminiCallback() {
            @Override
            public void onSuccess(String responseText) {
                if (getContext() != null) {
                    setControlesCargando(false);
                    String baseText = tvRespuestaIA.getText().toString();
                    String updatedText;
                    if (baseText.endsWith("Pensando...")) {
                        updatedText = baseText.substring(0, baseText.length() - 11) + responseText;
                    } else {
                        updatedText = baseText + "\n\n🤖 VitalBot:\n" + responseText;
                    }
                    tvRespuestaIA.setText(updatedText);
                    appRepo.saveChatHistory(updatedText);
                    scrollToBottom();
                    sincronizarChatEnNube(pregunta, responseText);
                }
            }

            @Override
            public void onError(String error) {
                if (getContext() != null) {
                    setControlesCargando(false);
                    Log.e(TAG, "Error recibiendo respuesta del servicio Gemini: " + error);
                    String fallback = "En este momento no puedo ayudarte con tu consulta, vuelve más tarde";
                    String baseText = tvRespuestaIA.getText().toString();
                    String updatedText;
                    if (baseText.endsWith("Pensando...")) {
                        updatedText = baseText.substring(0, baseText.length() - 11) + fallback;
                    } else {
                        updatedText = baseText + "\n\n🤖 VitalBot:\n" + fallback;
                    }
                    tvRespuestaIA.setText(updatedText);
                    appRepo.saveChatHistory(updatedText);
                    scrollToBottom();
                }
            }
        });
    }

    private void setControlesCargando(boolean cargando) {
        if (btnEnviarIA != null) btnEnviarIA.setEnabled(!cargando);
        if (etPregunta != null) etPregunta.setEnabled(!cargando);
        if (pbAsistente != null) pbAsistente.setVisibility(cargando ? View.VISIBLE : View.GONE);
    }

    private void scrollToBottom() {
        if (scrollViewAsistente != null) {
            scrollViewAsistente.post(() -> scrollViewAsistente.fullScroll(View.FOCUS_DOWN));
        }
    }

    private void sincronizarChatEnNube(String pregunta, String respuesta) {
        try {
            String userEmail = prefs.getString("email", "usuario@vitalhome.com").replace(".", "_");
            Map<String, Object> chatMsg = new HashMap<>();
            chatMsg.put("pregunta", pregunta);
            chatMsg.put("respuesta", respuesta);
            chatMsg.put("timestamp", new Date());

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userEmail)
                    .collection("chat_history")
                    .add(chatMsg)
                    .addOnFailureListener(e -> Log.e(TAG, "Error al sincronizar chat en Firestore", e));
        } catch (Exception e) {
            Log.e(TAG, "Excepción en sincronización de chat en nube", e);
        }
    }
}
