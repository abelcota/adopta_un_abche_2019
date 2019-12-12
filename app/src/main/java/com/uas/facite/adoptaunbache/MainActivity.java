package com.uas.facite.adoptaunbache;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    //Variales para identificar el navigation view el boton para abrir el menu y el Drawer Layour
    private NavigationView navegacion;
    private ImageButton botonMenu;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //identificar el drawer layout
        drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        //identificamos el boton menu
        botonMenu = (ImageButton)findViewById(R.id.botonMenu);
        //identificamos el navitacion view
        navegacion = (NavigationView)findViewById(R.id.nav_view);

        navegacion.setNavigationItemSelectedListener(this);

        //CApturamos el evento click del boton menu
        botonMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //verificar si el drawer se encuentra abierto
                if(drawer.isDrawerOpen(Gravity.LEFT))
                    //Cerrar el menu
                    drawer.closeDrawer(Gravity.LEFT);
                else
                    //abrir el menu
                    drawer.openDrawer(Gravity.LEFT);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        //obtenemos el id del elemento del menu seleccionado
        int id = menuItem.getItemId();
        switch (id){
            case R.id.nav_google:
                //abrimos el activity del google maps
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_mapox:
                //abrimos el activity del google maps
                Intent intentMApox = new Intent(MainActivity.this, MapBoxActivity.class);
                startActivity(intentMApox);
                break;
                default:
                    return true;
        }
        return true;
    }
}
