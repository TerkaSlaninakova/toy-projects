package com.example.mycatapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.LruCache;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button buttonNext;
    Button buttonPrev;
    ImageView imageView;
    List<String> listOfCats;
    RequestQueue queue;
    String getCatUrl = "https://api.thecatapi.com/v1/images/search";
    private LruCache<String, Bitmap> memoryCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listOfCats = new ArrayList();

        buttonNext = findViewById(R.id.buttonNext);
        buttonPrev = findViewById(R.id.buttonPrev);
        imageView = findViewById(R.id.image);

        memoryCache = initMemoryCache();

        buttonNext.setOnClickListener(this);
        buttonPrev.setOnClickListener(this);

        queue = Volley.newRequestQueue(this);
    }

    private LruCache<String, Bitmap> initMemoryCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        return new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        getNewCat();
    }

    @Override
    public void onClick(View v) {
        if (v == buttonNext){
            getNewCat();

        }
        if (v == buttonPrev){
            getOldCat();
        }
    }

    private void getNewCat() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, getCatUrl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    listOfCats.add(response.getJSONObject(0).get("id").toString());
                    displayCatImage(response.getJSONObject(0).get("url").toString(), listOfCats.get(listOfCats.size()-1));
                } catch (JSONException e) {
                    out.println(e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                out.println(error);
            }
        });

        queue.add(jsonArrayRequest);
    }

    private Bitmap compressCatImage(Bitmap origBitmap){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int byteCountInMB = origBitmap.getByteCount() / 1024 / 1024;
        Bitmap decompressed;

        if (byteCountInMB < 2) {
            origBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
        }  else if (byteCountInMB < 3) {
            origBitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
        } else if (byteCountInMB < 4) {
            origBitmap.compress(Bitmap.CompressFormat.JPEG, 40, out);
        } else {
            origBitmap.compress(Bitmap.CompressFormat.JPEG, 30, out);
        }
        decompressed = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
        return decompressed;
    }

    private void displayCatImage(String catUrl, final String id) {

        ImageRequest imageRequest = new ImageRequest(catUrl,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        Bitmap decompressed = compressCatImage(bitmap);
                        imageView.setImageBitmap(decompressed);
                        addBitmapToMemoryCache(id, decompressed);
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        out.println(error);
                    }
                });

        queue.add(imageRequest);
    }

    private void getOldCat(){
        if (listOfCats.size() > 1){
            listOfCats.remove(listOfCats.size() -1 );
            Bitmap decoded = getBitmapFromMemCache(listOfCats.get(listOfCats.size() -1 ));
            imageView.setImageBitmap(decoded);
        }
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return memoryCache.get(key);
    }
}
