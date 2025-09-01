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
import com.example.financas.ui.main.MainViewModel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private NotificationsViewModel notificationsViewModel;
    private MainViewModel mainViewModel;
    private Calendar startDate;
    private Calendar endDate;
    private SimpleDateFormat sdf;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notifications, container, false);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        binding.setViewModel(notificationsViewModel);
        View root = binding.getRoot();

        sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Initialize dates (now handled by MainViewModel)
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();

        // Set initial dates from MainViewModel's global dates
        if (mainViewModel.globalStartDate.getValue() != null) {
            startDate.setTime(mainViewModel.globalStartDate.getValue());
        }
        if (mainViewModel.globalEndDate.getValue() != null) {
            endDate.setTime(mainViewModel.globalEndDate.getValue());
        }

        updateDateButtonsText();

        binding.buttonStartDate.setOnClickListener(v -> showDatePickerDialog(true));
        binding.buttonEndDate.setOnClickListener(v -> showDatePickerDialog(false));

        binding.buttonFilter.setOnClickListener(v -> {
            mainViewModel.setGlobalDates(startDate.getTime(), endDate.getTime());
        });

        // Observe totalDespesas from NotificationsViewModel
        notificationsViewModel.totalDespesas.observe(getViewLifecycleOwner(), total -> {
            // The data binding in the XML already handles this, but a manual update can be done here if needed.
        });

        // Observe global dates from MainViewModel to trigger total expenses load
        mainViewModel.globalStartDate.observe(getViewLifecycleOwner(), globalStartDate -> {
            notificationsViewModel.loadTotalDespesasForPeriod(globalStartDate, mainViewModel.globalEndDate.getValue());
        });

        mainViewModel.globalEndDate.observe(getViewLifecycleOwner(), globalEndDate -> {
            notificationsViewModel.loadTotalDespesasForPeriod(mainViewModel.globalStartDate.getValue(), globalEndDate);
        });

        return root;
    }

    private void showDatePickerDialog(boolean isStartDate) {
        Calendar calendar = isStartDate ? startDate : endDate;
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            updateDateButtonsText();
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
