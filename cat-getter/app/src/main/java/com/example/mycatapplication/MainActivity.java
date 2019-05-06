package com.example.mycatapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button buttonNext;
    Button buttonPrev;
    TextView textView;
    List<String> listOfCats;
    RequestQueue queue;
    String getCatUrl = "https://api.thecatapi.com/v1/images/search";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listOfCats = new ArrayList();

        buttonNext = findViewById(R.id.buttonNext);
        buttonPrev = findViewById(R.id.buttonPrev);
        textView = findViewById(R.id.textView);

        buttonNext.setOnClickListener(this);
        buttonPrev.setOnClickListener(this);

        queue = Volley.newRequestQueue(this);
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
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, getCatUrl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    listOfCats.add(response.getJSONObject(0).get("id").toString());
                    textView.setText(listOfCats.get(listOfCats.size() -1 ));
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

        queue.add(jsonObjectRequest);
    }


    private void getOldCat(){
        if (listOfCats.size() > 1){
            listOfCats.remove(listOfCats.size() -1 );
            textView.setText(listOfCats.get(listOfCats.size() -1 ));
        }
    }
}
