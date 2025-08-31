package com.example.financas.ui.notifications;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.financas.model.Despesa;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Date;

public class NotificationsViewModel extends ViewModel {

    private final MutableLiveData<Double> _totalDespesas = new MutableLiveData<>();
    public LiveData<Double> totalDespesas = _totalDespesas;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration totalDespesasListenerRegistration;

    public NotificationsViewModel() {
        // No period-related initialization here.
        // The fragment will call loadTotalDespesasForPeriod with the initial dates.
    }

    // This method will be called by the Fragment with the calculated dates
    public void loadTotalDespesasForPeriod(Date startDate, Date endDate) {
        android.util.Log.d("NotificationsViewModel", "loadTotalDespesasForPeriod called");
        // Remove previous listener if it exists
        if (totalDespesasListenerRegistration != null) {
            totalDespesasListenerRegistration.remove();
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            android.util.Log.d("NotificationsViewModel", "currentUser is null, setting total to 0.0");
            _totalDespesas.setValue(0.0);
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

        totalDespesasListenerRegistration = query.addSnapshotListener((value, error) -> {
            if (error != null) {
                android.util.Log.e("NotificationsViewModel", "Error listening for total despesas", error);
                _totalDespesas.setValue(0.0);
                return;
            }

            double sum = 0.0;
            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    Despesa despesa = doc.toObject(Despesa.class);
                    sum += despesa.getValor();
                }
            }
            android.util.Log.d("NotificationsViewModel", "Total despesas calculated: " + sum);
            _totalDespesas.setValue(sum);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Remove the listener when the ViewModel is no longer used
        if (totalDespesasListenerRegistration != null) {
            totalDespesasListenerRegistration.remove();
            totalDespesasListenerRegistration = null;
        }
    }
}