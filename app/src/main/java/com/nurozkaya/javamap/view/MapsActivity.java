package com.nurozkaya.javamap.view;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.nurozkaya.javamap.R;
import com.nurozkaya.javamap.databinding.ActivityMapsBinding;
import com.nurozkaya.javamap.model.Place;
import com.nurozkaya.javamap.roomdb.PlaceDao;
import com.nurozkaya.javamap.roomdb.PlaceDatabase;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    //izin için
    ActivityResultLauncher<String> permissionLauncher;
    LocationManager locationManager;
    LocationListener locationListener;

    SharedPreferences sharedPreferences;
    boolean info;

    //roomdb
    PlaceDatabase db;
    PlaceDao placeDao;

    //onmaplongclickden konum alıyoruz place e param olarak yollamak için
    Double selectedLatitude;
    Double selectedLongitude;

    //rx
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    Place selectedPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //izin için çağırıyorum
        registerLauncher();

        sharedPreferences = this.getSharedPreferences("com.nurozkaya.javamap", MODE_PRIVATE);
        info = false;

        //roomdb
        //MapsActivity.this ya da getApplicationContext()
        db = Room.databaseBuilder(getApplicationContext(), PlaceDatabase.class, "Places").build();
        placeDao = db.placeDao();

        selectedLatitude = 0.0;
        selectedLongitude = 0.0;

        //kullanıcı bir yer seçmededn save yapamaz
        binding.saveButton.setEnabled(false);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);

        //gelen intenti al(infoyu)
        Intent intent = getIntent();
        String intentInfo = intent.getStringExtra("info");

        if (intentInfo.equals("new")) {
            binding.saveButton.setVisibility(View.VISIBLE);
            //invis dersek de görünmez ama yeri kalır
            //gone deyince yerine başka bişeyler gelebilir
            binding.deleteButton.setVisibility(View.GONE);

            //------------yeni eklenecekse bunları yap tekrar kestik ordan
            //user location
            //casting
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                //konum değiştiğinde ne yapılacak
                @SuppressLint("CommitPrefEdits")
                @Override
                public void onLocationChanged(@NonNull Location location) {

                    //info diye birşey yoksa değeri false olsun
                    info = sharedPreferences.getBoolean("info", false);

                    //haritada rahatca gezinebilmek için bir kez çalışır
                    if (!info) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                        sharedPreferences.edit().putBoolean("info", true).apply();
                    }
                    //System.out.println("location" +location.toString());
                }
            };

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //request permission
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Snackbar.make(binding.getRoot(), "Permission for maps", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //request permission
                            //her iki türlü de izni istememiz gerekiyor
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                        }
                    }).show();
                } else {
                    //request permission
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                }
            } else {
                //izin verilmişse
                //0 0 almak fazla enerji tüketir 10 saniyede bir 10000
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                //ilk izin istendiğinde de çağırılabilir registerLauncherda
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                //gerçekten bir konum geliyorsa kamerayı buna döndr
                if (lastLocation != null) {
                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                }

                //şu anki konumu gösterir mavi nokta ile ya da yukardaki shared pref ile
                mMap.setMyLocationEnabled(true);
            }
        } else {
            //öncesindeki anotas varsa temizle
            mMap.clear();

            //placei al cast edip(yollanan yer)
            selectedPlace = (Place) intent.getSerializableExtra("place");

            LatLng latLng = new LatLng(selectedPlace.latitude, selectedPlace.longitude);
            mMap.addMarker(new MarkerOptions().position(latLng).title(selectedPlace.name));
            //kamerayı da oraya çevir
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

            binding.placeNameText.setText(selectedPlace.name);
            binding.saveButton.setVisibility(View.GONE);
            binding.deleteButton.setVisibility(View.VISIBLE);
        }


        // Add a marker in Sydney and move the camera
        //enlem boylam belirleme(secilen konumu gösterme)
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16));
        //CameraUpdateFactory.newLatLng(sydney)
    }

    private void registerLauncher() {
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    //izin verildiyse tekrar kontrol yapıyoruz
                    if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        //gerçekten bir konum geliyorsa kamerayı buna döndr
                        if (lastLocation != null) {
                            LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                        }
                    }
                }
                //result ==false izi verilmediyse
                else {
                    Toast.makeText(MapsActivity.this, "Permissin Needed!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        //sadece bir tane nokta koyabilsin
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));

        selectedLatitude = latLng.latitude;
        selectedLongitude = latLng.longitude;

        //kullanıcı bir yer seçene kadar save butonuna tıklanamaz yapmak için
        binding.saveButton.setEnabled(true);

    }

    //xmlde save ve delete verdik onclicke
    public void save(View view) {
        //3 params
        Place place = new Place(binding.placeNameText.getText().toString(), selectedLatitude, selectedLongitude);

        //threading -> main UI thread,
        //Default (CPU Intensive) arka planda yoğun işlemler listeyi dizmek , arka arkaya devamlı işlemci yoran
        //IO Thread -> grdi çıktı network db operasyon, netten veri istmeek

        //io threadde çalıştır dedim, böyle de çalışır
        //placeDao.insert(place).subscribeOn(Schedulers.io()).subscribe();

        //disposable ile de çalışır(kullan at)
        //arka planda yapıcam ama mainde gözlicem
        compositeDisposable.add(placeDao.insert(place)
                .subscribeOn(Schedulers.io())
                // .observeOn(AndroidSchedulers.mainThread())
                .subscribe(MapsActivity.this::handleResponse));//boyle çalşır handler

    }

    //gelen cevabı ele al(maine don)
    //place kaydedince ana ekrana geri don
    private void handleResponse() {
        Intent intent = new Intent(MapsActivity.this, MainActivity.class);
        //btn act temizler
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void delete(View view) {

        if(selectedPlace!=null) {
            compositeDisposable.add(placeDao.delete(selectedPlace)
                    .subscribeOn(Schedulers.io())
                    //.observeOn(AndroidSchedulers.mainThread())
                    .subscribe(MapsActivity.this::handleResponse));
        }
        //butun call lar çöpe, hafızada yer tutmaz
        //compositeDisposable.clear();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}