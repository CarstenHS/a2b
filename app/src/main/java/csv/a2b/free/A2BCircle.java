package csv.a2b.free;

import com.google.android.gms.maps.model.Circle;

/**
 * Created by der_geiler on 03-11-2015.
 */
class A2BCircle
{
    private Circle circle;
    private String name;
    A2BCircle(){}

    A2BCircle(Circle c, String name)
    {
        circle = c;
        this.name = name;
    }
    Circle getCircle(){return circle;}
    String getName(){return name;}
    void setName(String name){this.name = name;}
}