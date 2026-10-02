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
import com.example.vitalhome.ui.viewmodels.ProfileMedicoViewModel;

/**
 * Fragmento para visualizar el perfil médico del usuario y sus contactos de emergencia configurados.
 */
public class ProfileMedicoFragment extends Fragment {

    // --- Bloque 1: Atributos de la clase ---
    private ProfileMedicoViewModel viewModel;

    // --- Bloque 2: Inflado de la vista y vinculación con LiveData ---
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile_medico, container, false);
        viewModel = new ViewModelProvider(this).get(ProfileMedicoViewModel.class);
        
        TextView tvName = v.findViewById(R.id.tvProfileName);
        TextView tvContact1 = v.findViewById(R.id.tvProfileContact1);
        TextView tvContact2 = v.findViewById(R.id.tvProfileContact2);

        viewModel.getName().observe(getViewLifecycleOwner(), tvName::setText);
        viewModel.getContact1().observe(getViewLifecycleOwner(), s -> {
            if (tvContact1 != null && !s.isEmpty()) tvContact1.setText(s);
        });
        viewModel.getContact2().observe(getViewLifecycleOwner(), s -> {
            if (tvContact2 != null && !s.isEmpty()) tvContact2.setText(s);
        });

        View arrowBack = v.findViewById(R.id.btnBackArrowProfile);
        if (arrowBack != null) {
            arrowBack.setOnClickListener(view -> Navigation.findNavController(view).navigateUp());
        }
        
        return v;
    }
}
