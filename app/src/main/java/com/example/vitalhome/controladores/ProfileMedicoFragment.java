package com.example.vitalhome.controladores;

import android.content.Context;
import android.content.SharedPreferences;
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

public class ProfileMedicoFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile_medico, container, false);
        
        SharedPreferences p = requireContext().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
        String name = p.getString("nombreAdultoMayor", "Usuario");
        
        ((TextView)v.findViewById(R.id.tvProfileName)).setText(name);

        v.findViewById(R.id.btnVolverProfile).setOnClickListener(view -> 
            Navigation.findNavController(view).navigateUp()
        );
        
        return v;
    }
}