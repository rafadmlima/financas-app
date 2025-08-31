package com.example.financas.ui.notifications;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.financas.R;
import com.example.financas.databinding.FragmentNotificationsBinding;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private NotificationsViewModel notificationsViewModel;
    private Calendar startDate;
    private Calendar endDate;
    private SimpleDateFormat sdf;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notifications, container, false);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        binding.setViewModel(notificationsViewModel);
        View root = binding.getRoot();

        sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Initialize dates
        startDate = Calendar.getInstance();
        startDate.set(Calendar.DAY_OF_MONTH, 1);
        endDate = Calendar.getInstance();
        endDate.set(Calendar.DAY_OF_MONTH, endDate.getActualMaximum(Calendar.DAY_OF_MONTH));

        updateDateButtonsText();

        binding.buttonStartDate.setOnClickListener(v -> showDatePickerDialog(true));
        binding.buttonEndDate.setOnClickListener(v -> showDatePickerDialog(false));

        

        // Observe totalDespesas from NotificationsViewModel
        notificationsViewModel.totalDespesas.observe(getViewLifecycleOwner(), total -> {
            // The data binding in the XML already handles this, but a manual update can be done here if needed.
        });

        // Initial load
        notificationsViewModel.loadTotalDespesasForPeriod(startDate.getTime(), endDate.getTime());

        return root;
    }

    private void showDatePickerDialog(boolean isStartDate) {
        Calendar calendar = isStartDate ? startDate : endDate;
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            updateDateButtonsText();
            if (!isStartDate) { // Automatically filter when end date is selected
                notificationsViewModel.loadTotalDespesasForPeriod(startDate.getTime(), endDate.getTime());
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void updateDateButtonsText() {
        binding.buttonStartDate.setText(sdf.format(startDate.getTime()));
        binding.buttonEndDate.setText(sdf.format(endDate.getTime()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
