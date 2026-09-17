package com.example.vitalhome.controladores;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.vitalhome.R;
import com.example.vitalhome.adapter.MedicationAdapter;
import com.example.vitalhome.modelo.AlertEvent;
import com.example.vitalhome.modelo.Medication;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardClienteFragment extends Fragment implements MedicationAdapter.AccionesMed {

    private AppRepo repo;
    private TextView lblPulso, lblOxigeno;
    private LinearLayout listadoMeds;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dashboard_cliente, container, false);
        repo = new AppRepo(requireContext());

        SharedPreferences p = requireContext().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
        String name = p.getString("nombreAdultoMayor", "Usuario");

        ((TextView)v.findViewById(R.id.tvSaludoCliente)).setText("Hola, " + name);
        lblPulso = v.findViewById(R.id.tvPulsoCliente);
        lblOxigeno = v.findViewById(R.id.tvOxigenoCliente);
        listadoMeds = v.findViewById(R.id.containerRemedios);

        // Listeners de los nuevos sensores
        v.findViewById(R.id.cardSensorFall).setOnClickListener(view -> 
            Navigation.findNavController(view).navigate(R.id.nav_alertas)
        );
        
        v.findViewById(R.id.cardSensorWater).setOnClickListener(view -> 
            Navigation.findNavController(view).navigate(R.id.nav_alertas)
        );

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        refrescarSignos();
        armarListaMeds();
        if (getView() != null) {
            SharedPreferences p = requireContext().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
            setOnClickListeners(getView(), p.getString("nombreAdultoMayor", "Usuario"));
        }
    }

    private void armarListaMeds() {
        if (listadoMeds == null) return;
        listadoMeds.removeAllViews();
        List<Medication> items = repo.getMeds();
        items.sort((m1, m2) -> Boolean.compare(m2.isTaken(), m1.isTaken()));
        
        for (Medication m : items) {
            View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_medication, listadoMeds, false);
            ((TextView)row.findViewById(R.id.tvMedName)).setText(m.getNombre());
            
            TextView badge = row.findViewById(R.id.tvMedBadge);
            View btn = row.findViewById(R.id.btnTakeMed);

            if (m.isTaken()) {
                badge.setText("Tomado");
                btn.setVisibility(View.GONE);
            } else {
                badge.setText(m.getHora());
                btn.setOnClickListener(click -> tomar(m));
            }
            listadoMeds.addView(row);
        }
    }

    @Override
    public void tomar(Medication m) {
        List<Medication> items = repo.getMeds();
        for (Medication item : items) {
            if (item.getId().equals(m.getId())) {
                item.setTaken(true);
                break;
            }
        }
        repo.saveMeds(items);
        repo.addEvent(new AlertEvent("MEDICINA", "Tomó " + m.getNombre(), "Casa", getTime()));
        armarListaMeds();
        Toast.makeText(requireContext(), "¡Registrado!", Toast.LENGTH_SHORT).show();
    }

    private void refrescarSignos() {
        String json = requireContext().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE)
                .getString("ultimos_vitals", "{}");
        try {
            JSONObject data = new JSONObject(json);
            if (data.has("pulso")) lblPulso.setText(data.getInt("pulso") + " LPM");
            if (data.has("oxig")) lblOxigeno.setText(data.getInt("oxig") + "%");
        } catch (Exception ignored) {}
    }

    private void setOnClickListeners(View v, String abuelo) {
        // Ahora el SOS lleva a la cuenta regresiva (de la rama BotonSOS)
        v.findViewById(R.id.btnSOS).setOnClickListener(view -> {
            Navigation.findNavController(view).navigate(R.id.nav_sos_countdown);
        });

        v.findViewById(R.id.btnMedirAhoraCliente).setOnClickListener(view -> {
            int p = (int) (Math.random() * 30 + 65);
            int o = (int) (Math.random() * 5 + 95);
            String pres = (int)(Math.random()*25+110) + "/" + (int)(Math.random()*15+70);
            
            repo.addEvent(new AlertEvent("SALUD", "Control: " + p + " LPM", "Casa", getTime()));
            
            requireContext().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE).edit()
                .putString("ultimos_vitals", "{\"pulso\":" + p + ",\"oxig\":" + o + ",\"pres\":\"" + pres + "\"}").apply();
            
            refrescarSignos();
            lanzarPopup(p, o, pres);
        });

        v.findViewById(R.id.btnSwitchRoleCliente).setOnClickListener(view -> {
            requireContext().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE).edit()
                .putString("rol", "cuidador").apply();
            recargarApp();
        });

        v.findViewById(R.id.btnLogoutCliente).setOnClickListener(view -> {
            requireContext().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE).edit()
                .putBoolean("isLoggedIn", false).apply();
            requireActivity().finish();
        });

        v.findViewById(R.id.btnCallCaregiver).setOnClickListener(view -> {
            Toast.makeText(requireContext(), "Llamando a tu cuidador...", Toast.LENGTH_LONG).show();
            // Aquí iría la lógica de Intent.ACTION_CALL si tuviéramos permisos
        });
    }

    private void lanzarPopup(int p, int o, String r) {
        View pop = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_vitals, null);
        AlertDialog alert = new AlertDialog.Builder(requireContext()).setView(pop).create();
        if (alert.getWindow() != null) alert.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ((TextView) pop.findViewById(R.id.tvPopPulso)).setText(p + " LPM");
        ((TextView) pop.findViewById(R.id.tvPopOxigeno)).setText(o + "%");
        ((TextView) pop.findViewById(R.id.tvPopPresion)).setText(r);
        
        pop.findViewById(R.id.btnCerrarPop).setOnClickListener(x -> alert.dismiss());
        alert.show();
    }

    private void recargarApp() {
        startActivity(new Intent(requireActivity(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        requireActivity().finish();
    }

    private String getTime() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }
}