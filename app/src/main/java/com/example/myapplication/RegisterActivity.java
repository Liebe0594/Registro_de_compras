package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private Button btnRegistrar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Vincular vistas (Asegúrate que los IDs coincidan con tu XML)
        inputEmail = findViewById(R.id.edit_text_username_register); // Usaremos el campo de usuario como email
        inputPassword = findViewById(R.id.edit_text_password_register);
        btnRegistrar = findViewById(R.id.button_register);

        btnRegistrar.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                inputEmail.setError("Ingresa un correo");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                inputPassword.setError("Ingresa una contraseña");
                return;
            }

            if (password.length() < 6) {
                inputPassword.setError("La contraseña debe tener al menos 6 caracteres");
                return;
            }

            // Crear usuario en Firebase
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "¡Cuenta creada con éxito!", Toast.LENGTH_SHORT).show();
                            // Ir al Login o directo al Main
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            // Si falla, mostramos el error (ej: email ya existe, formato inválido)
                            String error = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                            Toast.makeText(RegisterActivity.this, "Fallo el registro: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}

