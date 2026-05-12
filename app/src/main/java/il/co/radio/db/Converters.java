package il.co.radio.db;

import android.util.Log;

import androidx.room.TypeConverter;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class Converters {

    private static final String TAG = "Converters";

    @TypeConverter
    public static String fromStringList(List<String> list) {
        if (list == null) return null;
        return new JSONArray(list).toString();
    }

    @TypeConverter
    public static List<String> toStringList(String value) {
        if (value == null) return null;
        try {
            JSONArray array = new JSONArray(value);
            List<String> list = new ArrayList<>(array.length());
            for (int i = 0; i < array.length(); i++) {
                list.add(array.optString(i));
            }
            return list;
        } catch (JSONException e) {
            Log.w(TAG, "toStringList failed: " + value);
            return new ArrayList<>();
        }
    }
}
