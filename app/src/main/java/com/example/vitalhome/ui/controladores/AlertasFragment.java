package com.example.vitalhome.ui.controladores;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vitalhome.R;
import com.example.vitalhome.ui.adapter.AlertAdapter;
import androidx.lifecycle.ViewModelProvider;
import com.example.vitalhome.ui.viewmodels.AlertasViewModel;

/**
 * Fragmento responsable de mostrar la pantalla de historial de alertas.
 * Permite visualizar el listado de eventos de alerta registrados y realizar la limpieza total.
 */
public class AlertasFragment extends Fragment {

    // --- Bloque 1: Atributos y componentes de UI ---
    private AlertasViewModel viewModel;
    private AlertAdapter adapter;

    // --- Bloque 2: Inflado de vista e inicialización de componentes ---
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_alertas, container, false);
        viewModel = new ViewModelProvider(this).get(AlertasViewModel.class);

        // Configuración del RecyclerView
        RecyclerView rv = v.findViewById(R.id.rvAlertas);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        adapter = new AlertAdapter(viewModel.getEvents().getValue());
        rv.setAdapter(adapter);

        // Listener para el botón de borrar todas las alertas
        v.findViewById(R.id.btnBorrarTodo).setOnClickListener(x -> {
            viewModel.clearAll();
            Toast.makeText(requireContext(), getString(R.string.msg_alertas_limpias), Toast.LENGTH_SHORT).show();
        });

        // Configuración de botones de navegación / regresar
        v.findViewById(R.id.btnVolverAlertas).setOnClickListener(x -> 
            requireActivity().getOnBackPressedDispatcher().onBackPressed()
        );

        View arrowBack = v.findViewById(R.id.btnBackArrowAlertas);
        if (arrowBack != null) {
            arrowBack.setOnClickListener(x -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        }

        return v;
    }

    // --- Bloque 3: Suscripción a observadores LiveData ---
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            if (adapter != null && events != null) {
                adapter.updateList(events);
            }
        });
    }

    // --- Bloque 4: Ciclo de vida - Actualización en onResume y onDestroyView ---
    @Override
    public void onResume() {
        super.onResume();
        viewModel.refresh();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter = null;
    }
}
