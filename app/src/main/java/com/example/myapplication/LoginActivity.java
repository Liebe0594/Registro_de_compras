package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen; // Asegúrate de tener la dependencia de splashscreen si usas esta línea

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textViewCrearCuenta;

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
        // Instalar la Splash Screen antes de setContentView (opcional si lo configuraste)
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Vincular vistas
        editTextEmail = findViewById(R.id.edit_text_username_login); // Asumiendo que el ID sigue siendo el mismo, aunque ahora es email
        editTextPassword = findViewById(R.id.edit_text_password_login);
        buttonLogin = findViewById(R.id.button_login);
        textViewCrearCuenta = findViewById(R.id.text_view_crear_cuenta);

        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Por favor, ingresa email y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            // Iniciar sesión con Firebase
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Login exitoso
                            Toast.makeText(LoginActivity.this, "Bienvenido", Toast.LENGTH_SHORT).show();
                            irAlMain();
                        } else {
                            // Si falla el login
                            String error = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                            Toast.makeText(LoginActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        textViewCrearCuenta.setOnClickListener(v -> {
            // Navegar a la pantalla de registro
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void irAlMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Cierra LoginActivity para que no se pueda volver atrás
    }
}


