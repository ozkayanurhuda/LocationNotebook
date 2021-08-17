package com.nurozkaya.javamap.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.Room;

import com.nurozkaya.javamap.R;
import com.nurozkaya.javamap.adapter.PlaceAdapter;
import com.nurozkaya.javamap.databinding.ActivityMainBinding;
import com.nurozkaya.javamap.model.Place;
import com.nurozkaya.javamap.roomdb.PlaceDao;
import com.nurozkaya.javamap.roomdb.PlaceDatabase;

import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    PlaceDatabase db;
    PlaceDao placeDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //binding
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        View view =binding.getRoot();
        setContentView(view);

        //db işlemleri mapsAct ile aynı
        db= Room.databaseBuilder(getApplicationContext(),PlaceDatabase.class,"Places").build();
        placeDao=db.placeDao();

        compositeDisposable.add(placeDao.getAll()
        .subscribeOn(Schedulers.io())
        //.observeOn(AndroidSchedulers.mainThread())
        .subscribe(MainActivity.this::handleResponse)
        );
    }

    //getallda flowable list oldugu için param vermek zorunda
    private void handleResponse(List<Place> placeList) {

        binding.rv.setLayoutManager(new LinearLayoutManager(this));
        PlaceAdapter placeAdapter= new PlaceAdapter(placeList);
        binding.rv.setAdapter(placeAdapter);
    }

    //menuyu bağlamak
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        getMenuInflater().inflate(R.menu.travel_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //menuden bişey seçmek için
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //yeni bir yer eklemek istiyorsak maps act sayfasına
        if(item.getItemId()==R.id.add_place) {
            Intent intent= new Intent(MainActivity.this, MapsActivity.class);
            //yeni bir place eklemek istiyor
            intent.putExtra("info", "new");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}