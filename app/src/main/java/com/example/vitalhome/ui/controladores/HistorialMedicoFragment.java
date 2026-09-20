package com.example.vitalhome.ui.controladores;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vitalhome.R;
import com.example.vitalhome.ui.adapter.MedicalRecordAdapter;
import androidx.lifecycle.ViewModelProvider;
import com.example.vitalhome.ui.viewmodels.HistorialMedicoViewModel;

public class HistorialMedicoFragment extends Fragment {

    private HistorialMedicoViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_historial_medico, container, false);
        viewModel = new ViewModelProvider(this).get(HistorialMedicoViewModel.class);

        RecyclerView rv = v.findViewById(R.id.rvHistorialMedico);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        rv.setAdapter(new MedicalRecordAdapter(viewModel.getRecords().getValue()));

        v.findViewById(R.id.btnVolverHistorial).setOnClickListener(view -> 
            requireActivity().getOnBackPressedDispatcher().onBackPressed()
        );

        View arrowBack = v.findViewById(R.id.btnBackArrowHistorial);
        if (arrowBack != null) {
            arrowBack.setOnClickListener(x -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        }

        return v;
    }
}