package com.uas.facite.adoptaunbache;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MapBoxActivity extends AppCompatActivity {

    private MapView mapa;
    private MapboxMap mapboxMap;
    private FloatingActionButton BotonAgregarBache;
    //ruta donde mandaremos los datos para que se registre el bache en la BD
    private String WEB_SERVICE = "http://facite.uas.edu.mx/adoptaunbache/api/insertar_bache.php";

    //VARIABLES PARA MOSTRAR EL DISEÑO DE CAPTURAR BACHE
    BottomSheetBehavior botomSheet;
    LinearLayout layout_capturarBache;
    TextView txt_direccion, txt_latitud, txt_longitud;
    ImageButton botonCamara;
    Button botonAdoptar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String key = getString(R.string.MapboxKey);
        //creamos una instancia de mapbox
        Mapbox.getInstance(this,key);
        setContentView(R.layout.activity_map_box);
        //identificamos el visor de nuestro diseño
        mapa = findViewById(R.id.mapViewMapBox);
        //identificar las variables de nuestro diseño
        layout_capturarBache = (LinearLayout)findViewById(R.id.capturar_bache);
        botomSheet = BottomSheetBehavior.from(layout_capturarBache);
        txt_direccion = (TextView)findViewById(R.id.txtDireccion);
        txt_latitud = (TextView)findViewById(R.id.txtLatitud);
        txt_longitud = (TextView)findViewById(R.id.txtLongitud);
        botonCamara = (ImageButton)findViewById(R.id.botonCamara);
        botonAdoptar = (Button)findViewById(R.id.boton_Adoptar);

        //funcionalidad para el boton adoptar
        botonAdoptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //CONVERTIR LA IMAGEN TOMADA O SELECCIONADA A BASE 64
                //sacar la imagen puesta en el boton de la camara y convertirla a String Base 64
                Bitmap foto = ((BitmapDrawable)botonCamara.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                foto.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] fotosbytes = baos.toByteArray();
                String fotoString = Base64.encodeToString(fotosbytes, Base64.DEFAULT);
                //OOBTENEMOS LA DIRECCION; LATITUD; LONGITUD DE LOS CONTROLES
                String direcicon, latitud, longitud;
                direcicon = txt_direccion.getText().toString();
                latitud = txt_latitud.getText().toString();
                longitud = txt_longitud.getText().toString();
                //Creamos un objeto de la clase REgistrar Bache
                RegistrarBache registrar = new RegistrarBache(direcicon, latitud, longitud, fotoString);
                //Ejecutamos el web service
                registrar.execute();
                //Cerramos el botomshet la info del bache
                botomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                //limpiamos los controles del botom shet
                txt_longitud.setText("");
                txt_latitud.setText("");
                txt_direccion.setText("");
                //regresar la imagen de la camara al boton
                botonCamara.setImageResource(R.drawable.ic_camara);

            }
        });

        //Funcionalidad del boton camara
        botonCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Desplegar una alerta con posibles opciones a hacer
                final CharSequence[] opciones = {"Tomar fotografia", "Desde Galeria", "Cancelar"};
                AlertDialog.Builder alerta = new AlertDialog.Builder(MapBoxActivity.this);
                alerta.setTitle("Agregar una Fotografia para el Bache");
                alerta.setItems(opciones, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //PROGRAMAR LA FUNCIONALIDAD DE LAS OPCIONES
                        if(opciones[which].equals("Tomar fotografia")){
                            //SOLICITAR PERMISOS A LA CAMARA EN CASO DE QUE NO LO TENGA
                            //Verificar el SDK del telefono donde se esta ejecutando nuestra app
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                //Verificar si ya tiene permisos para la camara
                                if(ContextCompat.checkSelfPermission(MapBoxActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                                    Intent camara = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    startActivityForResult(camara, 1);
                                }
                                else{
                                    //solicitamos lso permisos a la camara
                                    ActivityCompat.requestPermissions(MapBoxActivity.this,new String[]{Manifest.permission.CAMERA}, 507);
                                    return;
                                }
                            }
                            else{
                                //SI ES UN TELEFONO CON ANDROID 5 o menor abrimos directo la camara
                                Intent camara = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(camara, 1);
                            }
                        }
                        else if(opciones[which].equals("Desde Galeria")){
                            //SOLICITAMOS LOS PERMISOS PARA ABRIR LA GALERIA
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                //Ver si ya tiene permisos
                                if (ContextCompat.checkSelfPermission(MapBoxActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                    Intent galeria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(galeria, 2);
                                } else {
                                    //SOLICITAMOS LOS PERMISOS PARA LA GALERIA
                                    ActivityCompat.requestPermissions(MapBoxActivity.this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE }, 507);
                                    return;
                                }
                            }
                            else{
                                Intent galeria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(galeria, 2);
                            }
                        }
                    }
                });
                //mostrar la alerta
                alerta.show();
            }

        });


        mapa.onCreate(savedInstanceState);
        mapa.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                //referencia hacia el mapa
                MapBoxActivity.this.mapboxMap = mapboxMap;

                mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

                        //CARGAR LOS PUNTOS DEL GeoJson de nuestra Api
                        try {
                            style.addSource(new GeoJsonSource("GEOJSON_PUNTOS",
                                    new URI("http://facite.uas.edu.mx/adoptaunbache/api/getlugares.php")));
                        } catch (URISyntaxException e) {
                           Log.i("ERROR GEOJSON:", e.toString());
                        }
                        //Creamos el icono personalizado para nuestros marcadores (puntos)
                        Bitmap icono = BitmapFactory.decodeResource(getResources(), R.drawable.alarm);
                        //agregar el icono al estilo del mapa
                        style.addImage("BACHE_ICONO", icono);
                        //Crear una capa layer con los datos cargados desde geojson
                        SymbolLayer BachesCapa = new SymbolLayer("BACHES", "GEOJSON_PUNTOS");
                        //Asignamos el icono personalizado a la capa de baches
                        BachesCapa.setProperties(PropertyFactory.iconImage("BACHE_ICONO"));
                        //Asignamos la capa de baches al mapa
                        style.addLayer(BachesCapa);
                        //POSICIONAR EL MARCADOR ESTATICO EN EL CENTRO DEL MAPA
                        ImageView MarcadorPin;
                        MarcadorPin = new ImageView(MapBoxActivity.this);
                        MarcadorPin.setImageResource(R.drawable.ic_pinwarning);
                        //Posicionar el MarcadorPin en el centro del mapa
                        FrameLayout.LayoutParams parametros = new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
                        //aplicamos esos parametros al marcador
                        MarcadorPin.setLayoutParams(parametros);
                        //Agregamos el marcador al mapa cargado
                        mapa.addView(MarcadorPin);
                        //Identificamos el Boton flotante del Layout
                        BotonAgregarBache = (FloatingActionButton)findViewById(R.id.btnAgregarBache);
                        //capturar el click del boton agregar bache
                        BotonAgregarBache.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Obtener las coordenadas x y del centro del mapa
                                final LatLng coordenadas = mapboxMap.getCameraPosition().target;
                                //Obtener la direccion con el metodo le enviamos el view del boton
                                ObtenerDireccion(v);
                                //Mostrar el layout de capturar bache
                                botomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
                                txt_latitud.setText("Latitud: "+coordenadas.getLatitude());
                                txt_longitud.setText("Longitud: " + coordenadas.getLongitude());
                            }
                        });
                    }
                });

                //llevar a la posicion de culiacan
                CameraPosition posicion = new CameraPosition.Builder()
                        .target(new LatLng(24.8087148, -107.3941223)) //estalece la posicion
                        .zoom(10) //establecer el zoom
                        //.bearing(180) //rota la camara
                        .tilt(80) //angulo de inclinacion
                        .build();

                //mover la posicion del mapa
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(posicion), 5000);


            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        MapBoxActivity.super.onActivityResult(requestCode, resultCode, intent);
        //Si se tomo una foto con la camara
        if(requestCode == 1)
        {
            Bitmap foto = (Bitmap)intent.getExtras().get("data");
            Drawable fotodrawable = new BitmapDrawable(foto);
            botonCamara.setImageDrawable(fotodrawable);
        }
        else if(requestCode == 2)
        {
            Uri fotoseleccionada = intent.getData();
            String[] rutaImagen = { MediaStore.Images.Media.DATA };
            Cursor cursor = MapBoxActivity.this.getApplicationContext().
                    getContentResolver().
                    query(fotoseleccionada, rutaImagen, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(rutaImagen[0]);
            String archivoFoto = cursor.getString(columnIndex);
            cursor.close();
            Bitmap foto = (BitmapFactory.decodeFile(archivoFoto));
            Drawable fotodrawable = new BitmapDrawable(foto);
            botonCamara.setImageDrawable(fotodrawable);
        }
    }

    //MEtodo para obtener la direccion en base a la latitud y la longitud
    public void ObtenerDireccion(View view){
        try{
        //Obtener las coordenadas del Marcador Pin en el mapa
        final LatLng coordenadas = mapboxMap.getCameraPosition().target;
        final Point punto = Point.fromLngLat(coordenadas.getLongitude(), coordenadas.getLatitude());
        //Utilizar los servicios de Mapbox para Geodecodificar la direccion en base al punto
        MapboxGeocoding servicio = MapboxGeocoding.builder()
                .accessToken("pk.eyJ1IjoiYWJlbGRleCIsImEiOiJjazFsYjJuODgwMTZzM21waW94MXdkb3VpIn0.CRUvSFmmzZN_UK0IYLt3fA")
                .query(Point.fromLngLat(punto.longitude(), punto.latitude()))
                .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
                .build();

        //Ejecutar el servicio con los parametros que estalecimos
        servicio.enqueueCall(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                //si el resultado no fue no nulo osea que si encontro una direccion
                if(response.body() != null){
                    List<CarmenFeature> resultados = response.body().features();
                    //obtenemos la direccion
                    CarmenFeature direccion = resultados.get(0);
                    //Mostramos la direccion obtenida con una barra de notificacion
                    /*Snackbar.make(view,
                            "Direccion: " + direccion.placeName(),
                            Snackbar.LENGTH_LONG).show();*/
                    txt_direccion.setText(direccion.placeName());
                }
            }
            @Override
             public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                    //Mostramos que no existieeron direcciones que mostrar
                Snackbar.make(view,
                        "Sin resultados que mostrar",
                        Snackbar.LENGTH_LONG).show();
             }
        });
    }
        catch(ServicesException servicesException){
            Log.i("Error del servicio", servicesException.toString());
        }
    }

    //CLASE QUE SE ENCARGARA DE REGISTRAR EL BACHE EN EL WEB SERVICE
    class RegistrarBache extends AsyncTask<Void, Void, String>{
        //Crear las variables de los parametros que se ocupan en el web service
        String direccion, latitud, longitud, foto;
        //Creamos el constructor de la clase
        RegistrarBache(String direccion, String latitud, String longitud, String foto)
        {
            this.direccion = direccion;
            this.latitud = latitud;
            this.longitud = longitud;
            this.foto = foto;
        }

        @Override
        protected String doInBackground(Void... voids) {
            //CREAR UN OBJETO DE LA CLASE RequestHandler
            RequestHandler requestHandler = new RequestHandler();
            //Creamos un hashmap con los parametros que se enviaran
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("nombre", direccion);
            parametros.put("lat", latitud);
            parametros.put("lon", longitud);
            parametros.put("img", foto);
            //Retornamos la respuesta que nos regreso el WEb service
            return requestHandler.sendPostRequest(WEB_SERVICE, parametros);
        }

        @Override
        protected void onPostExecute(String respuesta){
            super.onPostExecute(respuesta);
            //Convertimos la respuesta a un objeto JSON
            try {
                JSONObject object = new JSONObject(respuesta);
                //obtenemos el codigo del status
                int status = object.getInt("status");
                //si el codigo fue 1 entonces se registro correctamente el bache
                if(status == 1){
                    new SweetAlertDialog(MapBoxActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Excelente!")
                            .setContentText(object.getString("message"))
                            .show();
                }
                else{
                    new SweetAlertDialog(MapBoxActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("UPS :/")
                            .setContentText(object.getString("message"))
                            .show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }


}
