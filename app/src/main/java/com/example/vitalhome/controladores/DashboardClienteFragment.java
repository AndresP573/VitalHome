package com.example.vitalhome.controladores;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

    // Contenedores de las pantallas divididas
    private LinearLayout layoutInicio, layoutMedicamentos, layoutContactos;
    private TextView tvTabInicioLabel, tvTabMedicamentosLabel, tvTabContactosLabel;

    // Campos de contactos
    private EditText etContactName1, etContactPhone1, etContactName2, etContactPhone2;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dashboard_cliente, container, false);
        repo = new AppRepo(requireContext());
        prefs = requireContext().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);

        String name = prefs.getString("nombreAdultoMayor", "Usuario");
        ((TextView)v.findViewById(R.id.tvSaludoCliente)).setText("Hola, " + name);

        lblPulso = v.findViewById(R.id.tvPulsoCliente);
        lblOxigeno = v.findViewById(R.id.tvOxigenoCliente);
        listadoMeds = v.findViewById(R.id.containerRemedios);

        // Bindear secciones
        layoutInicio = v.findViewById(R.id.layoutClienteInicio);
        layoutMedicamentos = v.findViewById(R.id.layoutClienteMedicamentos);
        layoutContactos = v.findViewById(R.id.layoutClienteContactos);

        // Bindear etiquetas de pestañas
        tvTabInicioLabel = v.findViewById(R.id.tvTabInicioLabel);
        tvTabMedicamentosLabel = v.findViewById(R.id.tvTabMedicamentosLabel);
        tvTabContactosLabel = v.findViewById(R.id.tvTabContactosLabel);

        // Bindear campos de contactos
        etContactName1 = v.findViewById(R.id.etContactName1);
        etContactPhone1 = v.findViewById(R.id.etContactPhone1);
        etContactName2 = v.findViewById(R.id.etContactName2);
        etContactPhone2 = v.findViewById(R.id.etContactPhone2);

        // Configurar navegación de pestañas divididas
        v.findViewById(R.id.btnTabInicio).setOnClickListener(view -> switchTab(1));
        v.findViewById(R.id.btnTabMedicamentos).setOnClickListener(view -> switchTab(2));
        v.findViewById(R.id.btnTabContactos).setOnClickListener(view -> switchTab(3));

        // Guardar contactos
        v.findViewById(R.id.btnGuardarContactos).setOnClickListener(view -> guardarContactos());

        // Botón ver perfil médico dentro de la pantalla de remedios
        v.findViewById(R.id.btnVerPerfilMedicoCliente).setOnClickListener(view -> 
            Navigation.findNavController(view).navigate(R.id.nav_profile_medico)
        );

        cargarContactos();

        return v;
    }

    private void switchTab(int tabIndex) {
        layoutInicio.setVisibility(tabIndex == 1 ? View.VISIBLE : View.GONE);
        layoutMedicamentos.setVisibility(tabIndex == 2 ? View.VISIBLE : View.GONE);
        layoutContactos.setVisibility(tabIndex == 3 ? View.VISIBLE : View.GONE);

        tvTabInicioLabel.setTextColor(tabIndex == 1 ? Color.parseColor("#1457D9") : Color.parseColor("#62708A"));
        tvTabMedicamentosLabel.setTextColor(tabIndex == 2 ? Color.parseColor("#1457D9") : Color.parseColor("#62708A"));
        tvTabContactosLabel.setTextColor(tabIndex == 3 ? Color.parseColor("#1457D9") : Color.parseColor("#62708A"));
    }

    private void guardarContactos() {
        String n1 = etContactName1.getText().toString().trim();
        String p1 = etContactPhone1.getText().toString().trim();
        String n2 = etContactName2.getText().toString().trim();
        String p2 = etContactPhone2.getText().toString().trim();

        prefs.edit()
            .putString("client_contact_name_1", n1)
            .putString("client_contact_phone_1", p1)
            .putString("client_contact_name_2", n2)
            .putString("client_contact_phone_2", p2)
            .apply();

        Toast.makeText(requireContext(), "Contactos guardados exitosamente", Toast.LENGTH_SHORT).show();
    }

    private void cargarContactos() {
        etContactName1.setText(prefs.getString("client_contact_name_1", ""));
        etContactPhone1.setText(prefs.getString("client_contact_phone_1", ""));
        etContactName2.setText(prefs.getString("client_contact_name_2", ""));
        etContactPhone2.setText(prefs.getString("client_contact_phone_2", ""));
    }

    @Override
    public void onResume() {
        super.onResume();
        refrescarSignos();
        armarListaMeds();
        if (getView() != null) {
            setOnClickListeners(getView());
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
        String json = prefs.getString("ultimos_vitals", "{}");
        try {
            JSONObject data = new JSONObject(json);
            if (data.has("pulso")) lblPulso.setText(data.getInt("pulso") + " LPM");
            if (data.has("oxig")) lblOxigeno.setText(data.getInt("oxig") + "%");
        } catch (Exception ignored) {}
    }

    private void setOnClickListeners(View v) {
        v.findViewById(R.id.btnSOS).setOnClickListener(view -> {
            Navigation.findNavController(view).navigate(R.id.nav_sos_countdown);
        });

        v.findViewById(R.id.btnMedirAhoraCliente).setOnClickListener(view -> {
            int p = (int) (Math.random() * 30 + 65);
            int o = (int) (Math.random() * 5 + 95);
            String pres = (int)(Math.random()*25+110) + "/" + (int)(Math.random()*15+70);
            
            repo.addEvent(new AlertEvent("SALUD", "Control: " + p + " LPM", "Casa", getTime()));
            
            prefs.edit().putString("ultimos_vitals", "{\"pulso\":" + p + ",\"oxig\":" + o + ",\"pres\":\"" + pres + "\"}").apply();
            
            refrescarSignos();
            lanzarPopup(p, o, pres);
        });

        v.findViewById(R.id.btnSwitchRoleCliente).setOnClickListener(view -> {
            prefs.edit().putString("rol", "cuidador").apply();
            recargarApp();
        });

        v.findViewById(R.id.btnLogoutCliente).setOnClickListener(view -> {
            prefs.edit().putBoolean("isLoggedIn", false).apply();
            requireActivity().finish();
        });

        v.findViewById(R.id.btnCallCaregiver).setOnClickListener(view -> {
            String primaryContactPhone = prefs.getString("client_contact_phone_1", "").trim();
            if (!primaryContactPhone.isEmpty()) {
                Toast.makeText(requireContext(), "Llamando a: " + prefs.getString("client_contact_name_1", "Cuidador") + " (" + primaryContactPhone + ")...", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(requireContext(), "Llamando a tu cuidador...", Toast.LENGTH_LONG).show();
            }
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