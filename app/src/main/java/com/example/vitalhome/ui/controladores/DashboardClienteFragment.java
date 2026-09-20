package com.example.vitalhome.ui.controladores;

import android.app.AlertDialog;
import android.content.Intent;
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
import com.example.vitalhome.ui.adapter.MedicationAdapter;
import com.example.vitalhome.data.modelo.Medication;
import androidx.lifecycle.ViewModelProvider;
import com.example.vitalhome.ui.viewmodels.DashboardClienteViewModel;
import java.util.List;

public class DashboardClienteFragment extends Fragment implements MedicationAdapter.AccionesMed {

    private DashboardClienteViewModel viewModel;
    private TextView lblPulso, lblOxigeno;
    private LinearLayout listadoMeds;

    // Contenedores de las pantallas divididas
    private LinearLayout layoutInicio, layoutMedicamentos, layoutContactos;
    private TextView tvTabInicioLabel, tvTabMedicamentosLabel, tvTabContactosLabel;

    // Campos de contactos
    private EditText etContactName1, etContactPhone1, etContactName2, etContactPhone2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dashboard_cliente, container, false);
        viewModel = new ViewModelProvider(this).get(DashboardClienteViewModel.class);

        TextView tvSaludo = v.findViewById(R.id.tvSaludoCliente);
        viewModel.getNombreUsuario().observe(getViewLifecycleOwner(), s -> tvSaludo.setText("Hola, " + s));

        lblPulso = v.findViewById(R.id.tvPulsoCliente);
        lblOxigeno = v.findViewById(R.id.tvOxigenoCliente);
        listadoMeds = v.findViewById(R.id.containerRemedios);

        viewModel.getPulso().observe(getViewLifecycleOwner(), lblPulso::setText);
        viewModel.getOxigeno().observe(getViewLifecycleOwner(), lblOxigeno::setText);
        viewModel.getMedicamentos().observe(getViewLifecycleOwner(), this::armarListaMeds);

        // Bindear secciones
        layoutInicio = v.findViewById(R.id.layoutClienteInicio);
        layoutMedicamentos = v.findViewById(R.id.layoutClienteMedicamentos);
        layoutContactos = v.findViewById(R.id.layoutClienteContactos);

        // Bindear etiquetas de pestañas
        tvTabInicioLabel = v.findViewById(R.id.tvTabInicioLabel);
        tvTabMedicamentosLabel = v.findViewById(R.id.tvTabMedicamentosLabel);
        tvTabContactosLabel = v.findViewById(R.id.tvTabContactosLabel);

        // Observar índice de pestañas DESPUÉS del bindeo de layouts y etiquetas
        viewModel.getTabIndex().observe(getViewLifecycleOwner(), this::switchTab);

        // Campos de contactos
        etContactName1 = v.findViewById(R.id.etContactName1);
        etContactPhone1 = v.findViewById(R.id.etContactPhone1);
        etContactName2 = v.findViewById(R.id.etContactName2);
        etContactPhone2 = v.findViewById(R.id.etContactPhone2);

        viewModel.getContactName1().observe(getViewLifecycleOwner(), etContactName1::setText);
        viewModel.getContactPhone1().observe(getViewLifecycleOwner(), etContactPhone1::setText);
        viewModel.getContactName2().observe(getViewLifecycleOwner(), etContactName2::setText);
        viewModel.getContactPhone2().observe(getViewLifecycleOwner(), etContactPhone2::setText);

        // Configurar navegación de pestañas divididas llamando a viewModel.setTab()
        v.findViewById(R.id.btnTabInicio).setOnClickListener(view -> viewModel.setTab(1));
        v.findViewById(R.id.btnTabMedicamentos).setOnClickListener(view -> viewModel.setTab(2));
        v.findViewById(R.id.btnTabContactos).setOnClickListener(view -> viewModel.setTab(3));

        // Guardar contactos
        v.findViewById(R.id.btnGuardarContactos).setOnClickListener(view -> guardarContactos());

        // Botón ver perfil médico dentro de la pantalla de remedios
        v.findViewById(R.id.btnVerPerfilMedicoCliente).setOnClickListener(view -> 
            Navigation.findNavController(view).navigate(R.id.nav_profile_medico)
        );

        viewModel.getLastVitalsUpdate().observe(getViewLifecycleOwner(), update -> {
            if (update != null) {
                lanzarPopup(update.p, update.o, update.pres);
                viewModel.vitalsMostrado();
            }
        });

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

        viewModel.guardarContactos(n1, p1, n2, p2);
        Toast.makeText(requireContext(), "Contactos guardados exitosamente", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refreshData();
        if (getView() != null) {
            setOnClickListeners(getView());
        }
    }

    private void armarListaMeds(List<Medication> items) {
        if (listadoMeds == null) return;
        listadoMeds.removeAllViews();
        
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
        viewModel.tomar(m);
        Toast.makeText(requireContext(), "¡Registrado!", Toast.LENGTH_SHORT).show();
    }

    private void setOnClickListeners(View v) {
        v.findViewById(R.id.btnSOS).setOnClickListener(view -> {
            Navigation.findNavController(view).navigate(R.id.nav_sos_countdown);
        });

        v.findViewById(R.id.btnMedirAhoraCliente).setOnClickListener(view -> {
            viewModel.medirAhora();
        });

        v.findViewById(R.id.btnSwitchRoleCliente).setOnClickListener(view -> {
            viewModel.switchRole();
            recargarApp();
        });

        v.findViewById(R.id.btnLogoutCliente).setOnClickListener(view -> {
            viewModel.logout();
            requireActivity().finish();
        });

        v.findViewById(R.id.btnCallCaregiver).setOnClickListener(view -> {
            String primaryContactPhone = etContactPhone1.getText().toString().trim();
            if (!primaryContactPhone.isEmpty()) {
                Toast.makeText(requireContext(), "Llamando a: " + etContactName1.getText().toString() + " (" + primaryContactPhone + ")...", Toast.LENGTH_LONG).show();
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
}
