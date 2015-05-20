package com.thiagowittmann.attend;

/**
 * Created by thiago on 5/20/15.
 */
public class Viewer {
    private String id;
    private String name;
    private String qrid;
    private String attendance;

    public Viewer(String id, String name, String qrid, String attendance){
        this.id = id;
        this.name = name;
        this.qrid = qrid;
        this.attendance = attendance;
    }

    public String getID(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public String getQRID(){
        return this.qrid;
    }

    public String getAttendance(){
        return this.attendance;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
