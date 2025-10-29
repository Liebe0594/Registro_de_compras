package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonRegister;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        editTextUsername = findViewById(R.id.edit_text_username_register);
        editTextPassword = findViewById(R.id.edit_text_password_register);
        buttonRegister = findViewById(R.id.button_register);

        buttonRegister.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dbHelper.agregarUsuario(username, password)) {
                Toast.makeText(this, "Usuario registrado exitosamente. Ahora puedes iniciar sesión.", Toast.LENGTH_LONG).show();
                finish(); // Cierra RegisterActivity para volver a LoginActivity
            } else {
                Toast.makeText(this, "Error al registrar. El nombre de usuario ya existe.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
