package com.example.financas.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.financas.util.SingleLiveEvent;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainViewModel extends ViewModel {

    private final MutableLiveData<Integer> _fabVisibility = new MutableLiveData<>();
    public LiveData<Integer> fabVisibility = _fabVisibility;

    private final SingleLiveEvent<Void> _fabClickEvent = new SingleLiveEvent<>();
    public LiveData<Void> fabClickEvent = _fabClickEvent;

    private final MutableLiveData<Integer> _selectedPeriodIndex = new MutableLiveData<>();
    public LiveData<Integer> selectedPeriodIndex = _selectedPeriodIndex;

    private final MutableLiveData<Date> _globalStartDate = new MutableLiveData<>();
    public LiveData<Date> globalStartDate = _globalStartDate;

    private final MutableLiveData<Date> _globalEndDate = new MutableLiveData<>();
    public LiveData<Date> globalEndDate = _globalEndDate;

    private final MutableLiveData<List<String>> _periodOptions = new MutableLiveData<>();
    public LiveData<List<String>> periodOptions = _periodOptions;

    public MainViewModel() {
        _fabVisibility.setValue(android.view.View.GONE);
        _periodOptions.setValue(Arrays.asList("Últimos 7 dias", "Últimos 30 dias", "Todo o período"));
        _selectedPeriodIndex.setValue(0); // Default to "Últimos 7 dias"

        if (_globalStartDate.getValue() == null || _globalEndDate.getValue() == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            _globalStartDate.setValue(calendar.getTime());

            calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            _globalEndDate.setValue(calendar.getTime());
        }
    }

    public void setFabVisibility(int visibility) {
        _fabVisibility.setValue(visibility);
    }

    public void onFabClicked() {
        _fabClickEvent.call();
    }

    public void setSelectedPeriodIndex(int index) {
        _selectedPeriodIndex.setValue(index);
    }

    public void setGlobalDates(Date startDate, Date endDate) {
        _globalStartDate.setValue(startDate);
        _globalEndDate.setValue(endDate);
    }

    public Date getStartDateForPeriod(int periodIndex) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        switch (periodIndex) {
            case 0: // Últimos 7 dias
                calendar.add(Calendar.DAY_OF_YEAR, -6); // Today + 6 previous days
                return calendar.getTime();
            case 1: // Últimos 30 dias
                calendar.add(Calendar.DAY_OF_YEAR, -29); // Today + 29 previous days
                return calendar.getTime();
            case 2: // Todo o período
                return null; // No start date filter
            default:
                return null;
        }
    }

    public Date getEndDateForPeriod(int periodIndex) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime(); // Always up to the end of today
    }
}
