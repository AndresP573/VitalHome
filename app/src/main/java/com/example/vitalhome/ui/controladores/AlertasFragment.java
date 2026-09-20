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

public class AlertasFragment extends Fragment {

    private AlertasViewModel viewModel;
    private AlertAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_alertas, container, false);
        viewModel = new ViewModelProvider(this).get(AlertasViewModel.class);

        RecyclerView rv = v.findViewById(R.id.rvAlertas);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        adapter = new AlertAdapter(viewModel.getEvents().getValue());
        rv.setAdapter(adapter);

        v.findViewById(R.id.btnBorrarTodo).setOnClickListener(x -> {
            viewModel.clearAll();
            Toast.makeText(requireContext(), "Limpio", Toast.LENGTH_SHORT).show();
        });

        v.findViewById(R.id.btnVolverAlertas).setOnClickListener(x -> 
            requireActivity().getOnBackPressedDispatcher().onBackPressed()
        );

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
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refresh();
    }
}
