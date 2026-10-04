package com.example.vitalhome.ui.controladores;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.vitalhome.R;
import androidx.lifecycle.ViewModelProvider;
import com.example.vitalhome.ui.viewmodels.DashboardCuidadorViewModel;
import com.example.vitalhome.utils.NotificationHelper;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

/**
 * Fragmento correspondiente al Dashboard principal para el perfil Cuidador.
 * Ofrece acceso a las secciones de monitoreo de pulsera, plan de medicamentos, estado del hogar e historial médico,
 * e implementa la escucha en tiempo real de alertas SOS desde la nube.
 */
public class DashboardCuidadorFragment extends Fragment {

    // --- Bloque 1: Atributos de la clase ---
    private DashboardCuidadorViewModel viewModel;
    private ListenerRegistration alertListener;

    // --- Bloque 2: Inflado e inicialización de vistas ---
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard_cuidador, container, false);
        viewModel = new ViewModelProvider(this).get(DashboardCuidadorViewModel.class);
        
        TextView tvSaludo = root.findViewById(R.id.tvSaludo);
        TextView tvLabelPulsera = root.findViewById(R.id.tvLabelPulsera);
        TextView tvPulseraInfo = root.findViewById(R.id.tvPulseraInfo);
        TextView tvMedsSummary = root.findViewById(R.id.tvMedsSummary);

        viewModel.getNombreUsuario().observe(getViewLifecycleOwner(), s -> tvSaludo.setText(getString(R.string.greeting_hello, s)));
        viewModel.getNombreAdultoMayor().observe(getViewLifecycleOwner(), s -> {
            if (tvLabelPulsera != null) tvLabelPulsera.setText(getString(R.string.label_pulsera_of, s));
        });
        viewModel.getVitalsSummary().observe(getViewLifecycleOwner(), info -> {
            if (tvPulseraInfo != null && info != null) tvPulseraInfo.setText(info);
        });
        viewModel.getMedicinasCount().observe(getViewLifecycleOwner(), count -> {
            if (tvMedsSummary != null) tvMedsSummary.setText(getString(R.string.label_meds_count, count));
        });

        setClickListeners(root);
        iniciarEscuchadorAlertasEnVivo();
        return root;
    }

    // --- Bloque 3: Escucha en tiempo real de alertas en Cloud Firestore ---
    private void iniciarEscuchadorAlertasEnVivo() {
        if (!isAdded()) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance("default");

        db.collection("users")
            .addSnapshotListener((snapshots, e) -> {
                if (e != null || snapshots == null || !isAdded()) return;
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    String docRol = doc.getString("rol");
                    if ("cliente".equals(docRol)) {
                        escucharAlertasAbuelo(db, doc.getId());
                    }
                }
            });
    }

    private void escucharAlertasAbuelo(FirebaseFirestore db, String abueloDocId) {
        if (alertListener != null) {
            alertListener.remove();
        }

        alertListener = db.collection("users")
            .document(abueloDocId)
            .collection("alerts")
            .whereEqualTo("resuelto", false)
            .addSnapshotListener((snapshots, e) -> {
                if (e != null || snapshots == null || !isAdded()) return;
                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        String tipo = dc.getDocument().getString("tipo");
                        String desc = dc.getDocument().getString("descripcion");
                        if (getContext() != null) {
                            NotificationHelper.sendSOS(requireContext(), desc != null ? desc : "Emergencia");
                            Toast.makeText(requireContext(), "🚨 ¡ALERTA " + tipo + " EN VIVO!: " + desc, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
    }

    // --- Bloque 4: Ciclo de vida - onResume y onDestroyView ---
    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refreshData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (alertListener != null) {
            alertListener.remove();
            alertListener = null;
        }
    }

    // --- Bloque 5: Configuración de navegación por tarjetas ---
    private void setClickListeners(View v) {
        v.findViewById(R.id.cardVitals).setOnClickListener(x -> Navigation.findNavController(x).navigate(R.id.nav_pulsera));
        v.findViewById(R.id.cardMeds).setOnClickListener(x -> Navigation.findNavController(x).navigate(R.id.nav_medicamentos));
        v.findViewById(R.id.btnHistorialMedico).setOnClickListener(x -> Navigation.findNavController(x).navigate(R.id.nav_historial_medico));
    }
}
