package uet.vnu.check_in.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import org.json.JSONArray;
import org.json.JSONException;

public class ArrayUtil{
    public static ArrayList<Object> convert(JSONArray jArr) {
        ArrayList<Object> list = new ArrayList<Object>();
        try {
            for (int i=0, l=jArr.length(); i<l; i++){
                list.add(jArr.get(i));
            }
        } catch (JSONException e) {}

        return list;
    }

    public static JSONArray convert(Collection<Object> list)
    {
        return new JSONArray(list);
    }

    public static Double l2distance(ArrayList<Float> a1, ArrayList<Float> a2){
        double dis2 = 0;
        for (int i = 0; i < a1.size(); i++){
            if(a1.size() >= 128 && a2.size() >= 128) dis2+=Math.pow(a1.get(i) - a2.get(i), 2);
        }
        dis2 = Math.sqrt(dis2);
        return dis2;
    }

}