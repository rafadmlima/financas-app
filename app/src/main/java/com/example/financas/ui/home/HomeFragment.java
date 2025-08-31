package com.example.financas.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.financas.R;
import com.example.financas.databinding.FragmentHomeBinding;
import com.example.financas.model.Despesa;
import com.example.financas.ui.adapter.DespesaAdapter;
import com.example.financas.ui.main.MainViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment implements DespesaAdapter.OnItemClickListener {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private MainViewModel mainViewModel;
    private DespesaAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        binding.setViewModel(homeViewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        View root = binding.getRoot();

        adapter = new DespesaAdapter(new ArrayList<>());
        adapter.setOnItemClickListener(this);
        binding.recyclerViewDespesas.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewDespesas.setAdapter(adapter);

        homeViewModel.despesas.observe(getViewLifecycleOwner(), despesas -> {
            adapter.setDespesas(despesas);
        });

        homeViewModel.deleteStatus.observe(getViewLifecycleOwner(), status -> {
            android.widget.Toast.makeText(getContext(), status, android.widget.Toast.LENGTH_SHORT).show();
        });

        mainViewModel.fabClickEvent.observe(getViewLifecycleOwner(), aVoid -> {
            showAddEditDespesaDialog(null);
        });

        // Observe selectedPeriodIndex from MainViewModel to filter expenses
        mainViewModel.selectedPeriodIndex.observe(getViewLifecycleOwner(), index -> {
            homeViewModel.loadDespesas(
                    mainViewModel.getStartDateForPeriod(index),
                    mainViewModel.getEndDateForPeriod(index)
            );
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainViewModel.setFabVisibility(View.VISIBLE);
    }

    @Override
    public void onItemClick(Despesa despesa) {
        showAddEditDespesaDialog(despesa);
    }

    private void showAddEditDespesaDialog(Despesa despesaToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(despesaToEdit == null ? "Nova Despesa" : "Editar Despesa");

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_despesa, null);
        builder.setView(view);

        EditText editTextTitulo = view.findViewById(R.id.edit_text_titulo);
        EditText editTextValor = view.findViewById(R.id.edit_text_valor);
        EditText editTextObservacao = view.findViewById(R.id.edit_text_observacao);

        if (despesaToEdit != null) {
            editTextTitulo.setText(despesaToEdit.getTitulo());
            editTextValor.setText(String.valueOf(despesaToEdit.getValor()));
            editTextObservacao.setText(despesaToEdit.getObservacao());
        }

        builder.setPositiveButton(despesaToEdit == null ? "Adicionar" : "Salvar", null);
        builder.setNegativeButton("Cancelar", null);

        if (despesaToEdit != null) {
            builder.setNeutralButton("Excluir", (dialog, which) -> {
                homeViewModel.deleteDespesa(despesaToEdit);
                dialog.dismiss();
            });
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String titulo = editTextTitulo.getText().toString().trim();
            String valorStr = editTextValor.getText().toString().trim();
            String observacao = editTextObservacao.getText().toString().trim();

            if (titulo.isEmpty()) {
                android.widget.Toast.makeText(getContext(), "O título é obrigatório!", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            if (valorStr.isEmpty()) {
                android.widget.Toast.makeText(getContext(), "O valor é obrigatório!", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            double valor;
            try {
                valor = Double.parseDouble(valorStr);
            } catch (NumberFormatException e) {
                android.widget.Toast.makeText(getContext(), "Valor inválido!", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            if (valor < 0) {
                android.widget.Toast.makeText(getContext(), "O valor não pode ser negativo!", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                android.widget.Toast.makeText(getContext(), "Usuário não autenticado.", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            String userId = currentUser.getUid();

            if (despesaToEdit == null) {
                Despesa newDespesa = new Despesa(titulo, valor, observacao);
                newDespesa.setDataCadastro(new Date());
                newDespesa.setUserId(userId);
                addDespesaToFirestore(newDespesa);
            } else {
                // Check if documentId is present for update
                if (despesaToEdit.getDocumentId() == null || despesaToEdit.getDocumentId().isEmpty()) {
                    android.widget.Toast.makeText(getContext(), "Erro: ID da despesa não encontrado para atualização.", android.widget.Toast.LENGTH_LONG).show();
                    android.util.Log.e("HomeFragment", "Document ID is null or empty for expense update.");
                    return; // Prevent update if ID is missing
                }
                despesaToEdit.setTitulo(titulo);
                despesaToEdit.setValor(valor);
                despesaToEdit.setObservacao(observacao);
                updateDespesaInFirestore(despesaToEdit);
            }
            dialog.dismiss(); // Dismiss the dialog only if validation passes
        });

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
            dialog.dismiss();
        });
    }

    private void addDespesaToFirestore(Despesa despesa) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("despesas").add(despesa)
                .addOnSuccessListener(documentReference -> {
                    android.widget.Toast.makeText(getContext(), "Despesa adicionada com sucesso!", android.widget.Toast.LENGTH_SHORT).show();
                    homeViewModel.loadDespesas(
                            mainViewModel.getStartDateForPeriod(mainViewModel.selectedPeriodIndex.getValue()),
                            mainViewModel.getEndDateForPeriod(mainViewModel.selectedPeriodIndex.getValue())
                    );
                })
                .addOnFailureListener(e -> {
                    android.widget.Toast.makeText(getContext(), "Erro ao adicionar despesa: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                    android.util.Log.e("HomeFragment", "Erro ao adicionar despesa", e);
                });
    }

    private void updateDespesaInFirestore(Despesa despesa) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("despesas").document(despesa.getDocumentId()).set(despesa)
                .addOnSuccessListener(aVoid -> {
                    android.widget.Toast.makeText(getContext(), "Despesa atualizada com sucesso!", android.widget.Toast.LENGTH_SHORT).show();
                    homeViewModel.loadDespesas(
                            mainViewModel.getStartDateForPeriod(mainViewModel.selectedPeriodIndex.getValue()),
                            mainViewModel.getEndDateForPeriod(mainViewModel.selectedPeriodIndex.getValue())
                    );
                })
                .addOnFailureListener(e -> {
                    android.widget.Toast.makeText(getContext(), "Erro ao atualizar despesa: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                    android.util.Log.e("HomeFragment", "Erro ao atualizar despesa", e);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        mainViewModel.setFabVisibility(View.GONE);
    }
}