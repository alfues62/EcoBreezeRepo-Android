package com.m4gti.ecobreeze.ui.activities;

import android.graphics.Paint;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.m4gti.ecobreeze.R;

public class VerMas extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vermas); // Asegúrate de que este sea el layout correcto

        Spinner tipo = findViewById(R.id.filtro_gas);

        // Crear un arreglo con la opción "Seleccionar gas" al principio
        String[] gasesConOpcionDefault = new String[TipoGas.values().length + 1]; // +1 por la opción adicional
        gasesConOpcionDefault[0] = "Seleccionar gas"; // Primer opción que será visible
        for (int i = 0; i < TipoGas.values().length; i++) {
            gasesConOpcionDefault[i + 1] = TipoGas.values()[i].getTexto(); // Rellenar el resto con los gases
        }

        // Usamos el ArrayAdapter para llenar el Spinner con los nombres de los gases, incluyendo la opción por defecto
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, gasesConOpcionDefault);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        tipo.setAdapter(adaptador);

        // Configura el TextView y su OnClickListener
        TextView textLink = findViewById(R.id.atras_link);
        textLink.setOnClickListener(v -> {
            // Reemplazar el contenido actual con el HomeFragment
            Fragment homeFragment = new HomeFragment(); // Instancia del Fragment
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, homeFragment); // Cambia "fragment_container" al id del contenedor de tu Fragment
            transaction.addToBackStack(null); // Opcional: agrega la transacción a la pila
            transaction.commit();
        });
        TextView atrasLink = findViewById(R.id.atras_link);
        atrasLink.setPaintFlags(atrasLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

    }
}