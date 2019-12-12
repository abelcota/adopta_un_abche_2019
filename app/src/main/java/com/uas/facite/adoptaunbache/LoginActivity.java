package com.uas.facite.adoptaunbache;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    //CREAMOS LAS VARIABLES PARA LOS CONTROLES QUE USAREMOS
    EditText txt_usuario;
    EditText txt_password;
    Button boton_login;
    ProgressDialog progressDialog;
    String URL_WEB_SERVICE = "http://facite.uas.edu.mx/adoptaunbache/api/get_usuarios.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //IDENTIFICAR LOS CONTROLES DEL LAYOUT
        txt_usuario = (EditText)findViewById(R.id.editTextUsuario);
        txt_password = (EditText)findViewById(R.id.editTextPassword);
        boton_login = (Button)findViewById(R.id.cirLoginButton);
        //Capturamos el evento click del boton lgoin
        boton_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mandamos llamar el metodo hacer login
                hacerlogin();
            }
        });
    }

    private void hacerlogin()
    {
        //Obtener los valores de los editText
        String usuario = txt_usuario.getText().toString();
        String pass = txt_password.getText().toString();
        UsuarioLogin ul = new UsuarioLogin(usuario, pass);
        ul.execute();
    }

    public void AbrirRegistro(View v){
        Intent intent = new Intent(this, RegistroActivity.class);
        startActivity(intent);
    }

    //CLASE PARA HACER LA PETICION AL WEB SERVICE EN SEGUNDO PLANO
    class UsuarioLogin extends AsyncTask<Void, Void, String>{
        //ProgressBar barra;
        String usuario, password;
        //Constructor de la clase Usuario Login
        UsuarioLogin(String usuario, String password){
            this.usuario = usuario;
            this.password = password;
        }
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            //barra = findViewById()
        }
        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            try {
                //convertir la respuesta del web service a un objeto JSON
                JSONObject obj = new JSONObject(s);
                //VERIFICAMOS QUE ESTADO NOS REGRESO
                if(obj.getInt("status") == 0){
                    //si el status del web service fue 0 entonces es un login correcto
                    Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                    //Aqui pondremos el intent para que nos lleve al activity del mapa
                    //Intent intent = new Intent(LoginActivity.this, MapBoxActivity.class);
                    //startActivity(intent);
                }
                else
                {
                    //quiere decir que el estatus fue 1 o 2
                    Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            //CREAMOS UN OBJETO DE LA CLASE RequestHandler
            RequestHandler requestHandler = new RequestHandler();
            //Crear los parametros que se enviaran al web service
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("usuario", usuario);
            parametros.put("password", password);
            //retornamos la respuesta del web service
            return requestHandler.sendPostRequest(URL_WEB_SERVICE, parametros);
        }
    }
}
