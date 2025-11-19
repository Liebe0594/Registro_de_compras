package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textViewCrearCuenta;
    private TextView textViewOlvideContrasena; // <--- NUEVO

    // Instancia de Firebase Auth
    private FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        // Verificar si el usuario ya está logueado al iniciar la actividad
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            irAlMain();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Instalar la Splash Screen
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Vincular vistas
        editTextEmail = findViewById(R.id.edit_text_username_login);
        editTextPassword = findViewById(R.id.edit_text_password_login);
        buttonLogin = findViewById(R.id.button_login);
        textViewCrearCuenta = findViewById(R.id.text_view_crear_cuenta);

        // Vincular el nuevo botón de recuperación
        // (Asegúrate de haber puesto este ID en tu XML como te indiqué arriba)
        textViewOlvideContrasena = findViewById(R.id.text_view_olvide_contrasena);

        // --- LÓGICA DE INICIO DE SESIÓN ---
        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Por favor, ingresa email y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Bienvenido", Toast.LENGTH_SHORT).show();
                            irAlMain();
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                            Toast.makeText(LoginActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // --- LÓGICA DE REGISTRO ---
        textViewCrearCuenta.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // --- NUEVA LÓGICA DE RECUPERAR CONTRASEÑA ---
        textViewOlvideContrasena.setOnClickListener(v -> mostrarDialogoRecuperarContrasena());
    }

    private void irAlMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // --- MÉTODOS DE RECUPERACIÓN ---

    private void mostrarDialogoRecuperarContrasena() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recuperar Contraseña");
        builder.setMessage("Ingresa tu correo para recibir el enlace de restablecimiento:");

        // Input de texto dentro del diálogo
        final EditText inputEmail = new EditText(this);
        inputEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        inputEmail.setHint("correo@ejemplo.com");

        // Ajustes visuales (márgenes)
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(50, 20, 50, 20);
        inputEmail.setLayoutParams(params);

        // Contenedor
        LinearLayout container = new LinearLayout(this);
        container.addView(inputEmail);
        builder.setView(container);

        builder.setPositiveButton("Enviar", (dialog, which) -> {
            String email = inputEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(LoginActivity.this, "Ingresa un correo válido", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(LoginActivity.this, "Formato de correo inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            enviarCorreoDeRecuperacion(email);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void enviarCorreoDeRecuperacion(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(LoginActivity.this,
                            "Correo enviado. Revisa tu bandeja de entrada (y spam).",
                            Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}



