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
        String name = p.getString("nombreAdultoMayor", "Marta Rodríguez");
        
        ((TextView)v.findViewById(R.id.tvProfileName)).setText(name);

        // Obtener referencias de contactos
        TextView tvContact1 = v.findViewById(R.id.tvProfileContact1);
        TextView tvContact2 = v.findViewById(R.id.tvProfileContact2);

        // Cargar teléfonos guardados dinámicamente
        String c1Name = p.getString("client_contact_name_1", "Hijo — Juan");
        String c1Phone = p.getString("client_contact_phone_1", "1234-5678");
        String c2Name = p.getString("client_contact_name_2", "Vecina — Ana");
        String c2Phone = p.getString("client_contact_phone_2", "8765-4321");

        if (tvContact1 != null && !c1Phone.trim().isEmpty()) {
            tvContact1.setText((c1Name.isEmpty() ? "Contacto 1" : c1Name) + " (" + c1Phone + ")");
        }
        if (tvContact2 != null && !c2Phone.trim().isEmpty()) {
            tvContact2.setText((c2Name.isEmpty() ? "Contacto 2" : c2Name) + " (" + c2Phone + ")");
        }

        v.findViewById(R.id.btnVolverProfile).setOnClickListener(view -> 
            Navigation.findNavController(view).navigateUp()
        );
        
        return v;
    }
}