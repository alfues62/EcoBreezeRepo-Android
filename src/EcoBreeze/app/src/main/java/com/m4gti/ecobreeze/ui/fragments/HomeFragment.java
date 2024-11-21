package com.m4gti.ecobreeze.ui.fragments;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.m4gti.ecobreeze.R;
import com.m4gti.ecobreeze.ui.activities.VerMas;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla el diseño del fragmento
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Configura el TextView y su OnClickListener
        TextView textLink = view.findViewById(R.id.text_link);

        // Subrayar el texto programáticamente
        textLink.setPaintFlags(textLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        textLink.setOnClickListener(v -> {
            // Inicia la actividad VerMas
            Intent intent = new Intent(getActivity(), VerMas.class);
            startActivity(intent);
        });

        return view;

    }

}
