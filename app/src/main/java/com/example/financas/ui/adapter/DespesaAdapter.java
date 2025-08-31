package com.example.financas.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;


import com.example.financas.BR;
import com.example.financas.R;
import com.example.financas.model.Despesa;

import java.util.List;

public class DespesaAdapter extends RecyclerView.Adapter<DespesaAdapter.DespesaViewHolder> {

    private List<Despesa> despesas;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Despesa despesa);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public DespesaAdapter(List<Despesa> despesas) {
        this.despesas = despesas;
    }

    @NonNull
    @Override
    public DespesaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewDataBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.item_despesa,
                parent,
                false
        );
        return new DespesaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DespesaViewHolder holder, int position) {
        Despesa despesa = despesas.get(position);
        holder.bind(despesa);
        holder.binding.getRoot().setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), position % 2 == 0 ? R.color.white : R.color.light_gray));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(despesa);
            }
        });
    }

    @Override
    public int getItemCount() {
        return despesas.size();
    }

    public void setDespesas(List<Despesa> newDespesas) {
        this.despesas = newDespesas;
        android.util.Log.d("DespesaAdapter", "setDespesas called with " + newDespesas.size() + " items");
        notifyDataSetChanged();
    }

    static class DespesaViewHolder extends RecyclerView.ViewHolder {
        private final ViewDataBinding binding;

        public DespesaViewHolder(@NonNull ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Despesa despesa) {
            binding.setVariable(BR.despesa, despesa);
            binding.executePendingBindings();
        }
    }
}
