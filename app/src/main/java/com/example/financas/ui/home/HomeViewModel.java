package com.example.financas.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.financas.model.Despesa;
import com.example.financas.ui.main.MainViewModel;
import com.example.financas.util.SingleLiveEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<Despesa>> _despesas = new MutableLiveData<>();
    public LiveData<List<Despesa>> despesas = _despesas;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration despesasListenerRegistration;
    private ListenerRegistration despesasListenerRegistration;

    private final SingleLiveEvent<Void> _fabClickEvent = new SingleLiveEvent<>();
    public LiveData<Void> getFabClickEvent() {
        return _fabClickEvent;
    }

    private final SingleLiveEvent<String> _deleteStatus = new SingleLiveEvent<>();
    public LiveData<String> deleteStatus = _deleteStatus;

    private MainViewModel mainViewModel;

    public HomeViewModel() {
        // Initial load without filter, will be updated by Fragment observing MainViewModel
        // No need to call loadDespesas here, as the Fragment will trigger it via MainViewModel observation.
    }

    // This method will be called by the Fragment with the calculated dates
    public void loadDespesas(Date startDate, Date endDate) {
        // Remove previous listener if it exists
        if (despesasListenerRegistration != null) {
            despesasListenerRegistration.remove();
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            _despesas.setValue(new ArrayList<>()); // Clear expenses if no user is logged in
            return;
        }
        String currentUserId = currentUser.getUid();

        Query query = db.collection("despesas")
                .whereEqualTo("userId", currentUserId);

        if (startDate != null) {
            query = query.whereGreaterThanOrEqualTo("dataCadastro", startDate);
        }
        if (endDate != null) {
            query = query.whereLessThanOrEqualTo("dataCadastro", endDate);
        }

        despesasListenerRegistration = query.orderBy("dataCadastro", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }

                    List<Despesa> loadedDespesas = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Despesa despesa = doc.toObject(Despesa.class);
                            despesa.setDocumentId(doc.getId());
                            loadedDespesas.add(despesa);
                        }
                    }
                    _despesas.setValue(loadedDespesas);
                });
    }

    public void deleteDespesa(Despesa despesa) {
        if (despesa.getDocumentId() == null || despesa.getDocumentId().isEmpty()) {
            _deleteStatus.setValue("Erro: ID do documento não encontrado.");
            return;
        }
        db.collection("despesas").document(despesa.getDocumentId()).delete()
                .addOnSuccessListener(aVoid -> {
                    _deleteStatus.setValue("Despesa excluída com sucesso!");
                })
                .addOnFailureListener(e -> {
                    _deleteStatus.setValue("Erro ao excluir despesa: " + e.getMessage());
                });
    }

    public void onFabClicked() {
        android.util.Log.d("HomeViewModel", "FAB Clicked!");
        _fabClickEvent.call();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Remove the listener when the ViewModel is no longer used
        if (despesasListenerRegistration != null) {
            despesasListenerRegistration.remove();
            despesaListenerRegistration = null;
        }
    }
}
