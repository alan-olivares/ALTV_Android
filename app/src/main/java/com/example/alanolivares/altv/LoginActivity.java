package com.example.alanolivares.altv;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity{

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    Boolean as=true;
    Button boton,reg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Configure el formulario de inicio de sesión.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    as=true;
                    return true;
                }
                as=false;
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);
                attemptLogin();

            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        reg = (Button) findViewById(R.id.registro);
        reg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert=new AlertDialog.Builder(LoginActivity.this);
                alert.setMessage("Por favor mandame un correo mencionandome donde conseguite tu ALTV para registrarte una cuenta");
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String[] to = { "aiomskate@hotmail.com"};
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.setData(Uri.parse("mailto:"));
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT,"");
                        emailIntent.setType("message/rfc822");
                        startActivity(Intent.createChooser(emailIntent, "Email "));
                    }
                }).setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog aler=alert.create();
                aler.show();
            }
        });
    }
    public Boolean isOnlineNet() {

        ConnectivityManager connectivityManager = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo actNetInfo = connectivityManager.getActiveNetworkInfo();

        return (actNetInfo != null && actNetInfo.isConnected());
    }


    private void attemptLogin() {
        // Restablecer errores.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel=false;
        View focusView = null;

        // Compruebe si hay una contraseña válida, si el usuario ha ingresado una.
        if (!isPasswordValid(password)) {
            mPasswordView.setError("Contraseña debe ser mayor a 5 caracteres");
            focusView = mPasswordView;
            cancel = true;
        }

        // Compruebe si hay una dirección de correo electrónico válida.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError("Campo obligatorio");
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError("Campo obligatorio");
            focusView = mEmailView;
            cancel = true;
        }
        if (cancel) {
            // Hubo un error; No intente iniciar sesión
            // y enfocar el primer campo de formulario con un error.
            focusView.requestFocus();
        } else {
            // Mostrar un hilandero de progreso y iniciar una tarea
            // en segundo plano para realizar el intento de inicio de sesión de usuario.
            //progressDialog = ProgressDialog.show(LoginActivity.this,"","Iniciando sesion..",true);
            new JsonTask().execute("https://pastebin.com/raw/0e7W36ga");
            //new ConsultarDatos().execute("http://10.0.2.2/SITEUR/Consulta.php?correo="+mEmailView.getText().toString());
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
    private class JsonTask extends AsyncTask<String,String,String>{
        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd=new ProgressDialog(LoginActivity.this);
            pd.setMessage("Iniciando sessión");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection=null;
            BufferedReader reader = null;
            try{
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line ="";
                while ((line =reader.readLine())!= null){
                    buffer.append(line+"\n");
                    Log.e("Response",">"+line);
                }
                return buffer.toString();
            }catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(connection != null){
                    connection.disconnect();
                }
                try{
                    if(reader!= null){
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(pd.isShowing()){
                pd.dismiss();
            }
            try{
                SharedPreferences preferences = getSharedPreferences("Usuarios",MODE_PRIVATE);
                JSONArray jsonArray = new JSONArray(s);
                String correo,contra;
                correo=mEmailView.getText().toString();
                contra=mPasswordView.getText().toString();

                boolean a=false;
                for (int i=0; i<jsonArray.length(); i++){
                    JSONObject jsonObject= jsonArray.getJSONObject(i);
                    System.out.println(jsonObject.getString("usuario"));
                    System.out.println(jsonObject.getString("password"));
                    if((jsonObject.getString("usuario").equals(correo)&&jsonObject.getString("password").equals(contra))){
                        SharedPreferences.Editor editor = getSharedPreferences("Usuarios",MODE_PRIVATE).edit();
                        editor.putString("correo",correo);
                        editor.putString("contra",contra);
                        editor.putString("nombre",jsonObject.getString("name"));
                        editor.commit();
                        System.out.println(jsonObject.getString("usuario"));
                        System.out.println(jsonObject.getString("password"));
                        System.out.println(correo);
                        System.out.println(contra);
                        a=true;
                        Toast.makeText(getApplicationContext(), "Inicio de sesión satisfactorio", Toast.LENGTH_SHORT).show();
                        Intent re = new Intent(LoginActivity.this, MenuLateral.class);
                        startActivity(re);
                    }
                }
                if(a==false) {
                    Snackbar
                            .make(findViewById(R.id.root_layout), "Correo o contraseña incorrecta",Snackbar.LENGTH_LONG)
                            .show();
                }
                    /*listViewContactos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                });*/
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}

