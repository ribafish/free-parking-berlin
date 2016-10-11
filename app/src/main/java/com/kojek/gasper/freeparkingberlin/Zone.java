package com.kojek.gasper.freeparkingberlin;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by gaspe on 10. 10. 2016.
 */
public class Zone {
    private String name;
    private int number;
    private ArrayList<LatLng> points;

    public Zone (String name, int number, ArrayList<LatLng> points) {
        this.name = name;
        this.number = number;
        this.points = points;
    }

    public Zone (int number) {
        this.name = Integer.toString(number);
        this.number = number;
        this.points = new ArrayList<>();
    }

    public Zone addPoint(double latitude, double longitude) {
        points.add(new LatLng(latitude, longitude));
        return this;
    }

    public Zone removeAllPoints(){
        points.clear();
        return this;
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    public ArrayList<LatLng> getPoints() {
        return points;
    }
}
