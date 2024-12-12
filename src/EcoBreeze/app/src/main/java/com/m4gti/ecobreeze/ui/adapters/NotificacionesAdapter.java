package com.m4gti.ecobreeze.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.m4gti.ecobreeze.R;
import com.m4gti.ecobreeze.models.Notificacion;

import java.util.List;

public class NotificacionesAdapter extends RecyclerView.Adapter<NotificacionesAdapter.NotificacionViewHolder> {

    private List<Notificacion> notificaciones;

    public NotificacionesAdapter(List<Notificacion> notificaciones) {
        this.notificaciones = notificaciones;
    }

    @NonNull
    @Override
    public NotificacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notificacion, parent, false);
        return new NotificacionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificacionViewHolder holder, int position) {
        Notificacion notificacion = notificaciones.get(position);
        holder.tituloTextView.setText(notificacion.getTitulo());
        holder.cuerpoTextView.setText(notificacion.getCuerpo());
        holder.fechaTextView.setText(notificacion.getFecha());
    }

    @Override
    public int getItemCount() {
        return notificaciones.size();
    }

    // ViewHolder para cada notificación
    public static class NotificacionViewHolder extends RecyclerView.ViewHolder {
        TextView tituloTextView;
        TextView cuerpoTextView;
        TextView fechaTextView;

        public NotificacionViewHolder(View itemView) {
            super(itemView);
            tituloTextView = itemView.findViewById(R.id.tituloNotificacion);
            cuerpoTextView = itemView.findViewById(R.id.cuerpoNotificacion);
            fechaTextView = itemView.findViewById(R.id.fechaNotificacion);
        }
    }
}