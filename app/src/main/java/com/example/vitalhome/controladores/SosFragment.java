package com.example.vitalhome.controladores;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.vitalhome.R;

public class SosFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sos, container, false);
        
        v.findViewById(R.id.btnEstoyBien).setOnClickListener(view -> 
            Navigation.findNavController(view).navigate(R.id.nav_dashboard_cliente)
        );
        
        return v;
    }
}