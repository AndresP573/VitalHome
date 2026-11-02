package com.example.vitalhome.ui.controladores;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vitalhome.R;
import com.example.vitalhome.ui.adapter.AlertAdapter;
import com.example.vitalhome.ui.viewmodels.AlertasViewModel;
import com.example.vitalhome.ui.viewmodels.AppViewModelFactory;

/**
 * Fragmento responsable de mostrar la pantalla de historial de alertas.
 * Observa la máquina de estados de AlertasViewModel y maneja la visibilidad de ProgressBar.
 */
public class AlertasFragment extends Fragment {

    private AlertasViewModel viewModel;
    private AlertAdapter adapter;
    private ProgressBar pbAlertas;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_alertas, container, false);
        AppViewModelFactory factory = new AppViewModelFactory(requireActivity().getApplication());
        viewModel = new ViewModelProvider(this, factory).get(AlertasViewModel.class);

        pbAlertas = v.findViewById(R.id.pbAlertas);

        RecyclerView rv = v.findViewById(R.id.rvAlertas);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new AlertAdapter(viewModel.getEvents().getValue());
        rv.setAdapter(adapter);

        v.findViewById(R.id.btnBorrarTodo).setOnClickListener(x -> {
            viewModel.clearAll();
            Toast.makeText(requireContext(), getString(R.string.msg_alertas_limpias), Toast.LENGTH_SHORT).show();
        });

        View arrowBack = v.findViewById(R.id.btnBackArrowAlertas);
        if (arrowBack != null) {
            arrowBack.setOnClickListener(x -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        }

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            if (adapter != null && events != null) {
                adapter.updateList(events);
            }
        });

        viewModel.getEstado().observe(getViewLifecycleOwner(), estado -> {
            if (estado == null) return;
            switch (estado) {
                case CARGANDO:
                    if (pbAlertas != null) pbAlertas.setVisibility(View.VISIBLE);
                    break;
                case EXITO:
                case VACIO:
                    if (pbAlertas != null) pbAlertas.setVisibility(View.GONE);
                    break;
                case ERROR:
                    if (pbAlertas != null) pbAlertas.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Error al cargar alertas", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

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
