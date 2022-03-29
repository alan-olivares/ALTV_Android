package com.example.alanolivares.altv;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Registro extends AppCompatActivity {
    Button inicio;
    EditText nombre,apellido,correo,contrasena;
    private static ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        inicio=(Button) findViewById(R.id.reg);
        nombre=(EditText)findViewById(R.id.Rnombre);
        apellido=(EditText)findViewById(R.id.Rapellido);
        correo=(EditText)findViewById(R.id.Rcorreo);
        contrasena=(EditText)findViewById(R.id.Rcontrasena);
        inicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(contrasena.getWindowToken(), 0);
                attemptLogin();
            }
        });
    }

    private class CargarDatos extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

        }
    }
    private void attemptLogin() {
        // Restablecer errores.
        correo.setError(null);
        contrasena.setError(null);

        // Store values at the time of the login attempt.
        String email = correo.getText().toString();
        String password = contrasena.getText().toString();

        boolean cancel=false;
        View focusView = null;

        // Compruebe si hay una contraseña válida, si el usuario ha ingresado una.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            contrasena.setError(getString(R.string.error_invalid_password));
            focusView = contrasena;
            cancel = true;
        }

        // Compruebe si hay una dirección de correo electrónico válida.
        if (TextUtils.isEmpty(email)) {
            correo.setError(getString(R.string.error_field_required));
            focusView = correo;
            cancel = true;
        } else if (!isEmailValid(email)) {
            correo.setError(getString(R.string.error_invalid_email));
            focusView = correo;
            cancel = true;
        }
        if (cancel) {
            // Hubo un error; No intente iniciar sesión
            // y enfocar el primer campo de formulario con un error.
            focusView.requestFocus();
        } else {
            // Mostrar un hilandero de progreso y iniciar una tarea
            // en segundo plano para realizar el intento de inicio de sesión de usuario.
            progressDialog = ProgressDialog.show(Registro.this,"","Registrando..",true);
            new ConsultarDatos().execute("http://10.0.2.2/ALTV/Consulta.php?correo="+correo.getText().toString());
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 5;
    }

    private class ConsultarDatos extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {


            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            String vercorreo="";
            String vercorreo2=correo.getText().toString();
            JSONArray ja = null;
            try {
                ja = new JSONArray(result);
                vercorreo=ja.getString(0);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(vercorreo.equals(vercorreo2)){
                progressDialog.cancel();
                Toast.makeText(getApplicationContext(), "Este correo ya esta registrado", Toast.LENGTH_LONG).show();
            }
            else{
                SharedPreferences.Editor editor = getSharedPreferences("Usuarios",MODE_PRIVATE).edit();
                editor.putString("correo",correo.getText().toString());
                editor.commit();
                Toast.makeText(getApplicationContext(), "Registro satisfactorio", Toast.LENGTH_LONG).show();
                Intent re = new Intent(Registro.this, MenuLateral.class);
                new CargarDatos().execute("http://10.0.2.2/ALTV/registro.php?correo="+correo.getText().toString()+"&nombre="+nombre.getText().toString()+"&apellido="+apellido.getText().toString()+"&contrasena="+contrasena.getText().toString());
                progressDialog.cancel();
                startActivity(re);
            }

        }
    }

    private String downloadUrl(String myurl) throws IOException {
        Log.i("URL",""+myurl);
        myurl = myurl.replace(" ","%20");
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d("respuesta", "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }
}
