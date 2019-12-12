package com.uas.facite.adoptaunbache;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class RegistroActivity extends AppCompatActivity {
    EditText nombre, usuario, contra;
    Button boton_registrar;
    String URL_WEB_SERVICE = "http://facite.uas.edu.mx/adoptaunbache/api/registro_usuario.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        //identificar los controles
        nombre = (EditText)findViewById(R.id.editNombre);
        usuario = (EditText)findViewById(R.id.editUsuario);
        contra = (EditText)findViewById(R.id.editContra);
        boton_registrar = (Button)findViewById(R.id.botonRegistro);
        //Capturar el evento click del boton registrar
        boton_registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registrarUsuario();
            }
        });
    }
    //MEtodo para realizar el registro en segundo plano
    private void registrarUsuario(){
        //obtenemos los valores escritos en los EditText
        String nom = nombre.getText().toString();
        String usu = usuario.getText().toString();
        String pas = contra.getText().toString();
        //realizamos el registro en segundo plano llamando la clase RegistrarAsync
        RegistroUsuario registroObject = new RegistroUsuario(nom, usu, pas);
        registroObject.execute();
    }

    //CLASE PARA HACER LA PETICION AL WEB SERVICE EN SEGUNDO PLANO
    class RegistroUsuario extends AsyncTask<Void, Void, String> {
        String nombre, usuario, password;
        //Constructor de la clase Usuario REgistro
        RegistroUsuario(String nombre, String usuario, String password){
            this.nombre = nombre;
            this.usuario = usuario;
            this.password = password;
        }
        @Override
        protected String doInBackground(Void... voids) {
            //CREAMOS UN OBJETO DE LA CLASE RequestHandler
            RequestHandler requestHandler = new RequestHandler();
            //Crear los parametros que se enviaran al web service
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("nombre", nombre);
            parametros.put("usuario", usuario);
            parametros.put("pass", password);
            //retornamos la respuesta del web service
            return requestHandler.sendPostRequest(URL_WEB_SERVICE, parametros);
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //convertir la respuesta del web service a un objeto JSON
            try {
                JSONObject obj = new JSONObject(s);
                if (obj.getInt("status" ) == 1){
                    //mandamos la alerta bonita
                    new SweetAlertDialog(RegistroActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Excelente")
                            .setContentText(obj.getString("message"))
                            .show();
                }
                else
                {
                    //mandamos la alerta bonita con el error
                    new SweetAlertDialog(RegistroActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Ups!")
                            .setContentText(obj.getString("message"))
                            .show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void AbrirLogin(View v){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
