package com.example.vitalhome;

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
import org.json.JSONObject;

public class PulseraFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pulsera, container, false);
        
        SharedPreferences p = requireContext().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
        String data = p.getString("ultimos_vitals", "{}");
        
        TextView pulso = v.findViewById(R.id.tvPulsoReal);
        TextView oxig = v.findViewById(R.id.tvOxigenoReal);
        TextView pres = v.findViewById(R.id.tvPresionReal);
        
        try {
            JSONObject obj = new JSONObject(data);
            pulso.setText(obj.has("pulso") ? obj.getInt("pulso") + " LPM" : "-- LPM");
            oxig.setText(obj.has("oxig") ? obj.getInt("oxig") + "%" : "--%");
            pres.setText(obj.has("pres") ? obj.getString("pres") : "--/--");
        } catch (Exception e) {
            pulso.setText("-- LPM");
            oxig.setText("--%");
            pres.setText("--/--");
        }

        v.findViewById(R.id.btnVolverPulsera).setOnClickListener(view -> 
            Navigation.findNavController(view).navigate(R.id.nav_dashboard)
        );
        
        return v;
    }
}