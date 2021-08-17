package com.nurozkaya.javamap.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.nurozkaya.javamap.model.Place;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface PlaceDao {

    // WHERE name=:nameinput
    //Flowable rx
    @Query("SELECT * FROM Place")
    Flowable<List<Place>> getAll();

    //void yerine Comp rx
    @Insert
    Completable insert(Place place);

    @Delete
    Completable delete(Place place);
}
