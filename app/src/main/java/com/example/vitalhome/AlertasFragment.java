package com.example.vitalhome;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AlertasFragment extends Fragment {

    private AppRepo repo;
    private AlertAdapter adapter;
    private List<AlertEvent> lista;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_alertas, container, false);
        repo = new AppRepo(requireContext());
        lista = repo.cargarHistorial();

        RecyclerView rv = v.findViewById(R.id.rvAlertas);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AlertAdapter(lista);
        rv.setAdapter(adapter);

        v.findViewById(R.id.btnBorrarTodo).setOnClickListener(x -> {
            lista.clear();
            repo.guardarHistorial(new ArrayList<>());
            adapter.notifyDataSetChanged();
            Toast.makeText(requireContext(), "Historial limpio", Toast.LENGTH_SHORT).show();
        });

        v.findViewById(R.id.btnVolverAlertas).setOnClickListener(x -> 
            Navigation.findNavController(x).navigate(R.id.nav_dashboard)
        );

        return v;
    }
}