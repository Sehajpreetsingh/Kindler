package com.example.kindler;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Search extends AppCompatActivity implements View.OnClickListener {

    EditText _search;
    Button _button;
    ScrollView _searchResultList;

    ArrayList<BookItem> results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        this._search = findViewById(R.id.searchText);
        this._button = findViewById(R.id.searchButton);
        this._searchResultList = findViewById(R.id.searchResultList);

        if (this._button != null) {
            this._button.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.searchButton:
                this.handleSearch();
                break;
        }
    }

    private void handleSearch() {
        String query = this._search.getText().toString();
        String requestUrl = "https://www.googleapis.com/books/v1/volumes?q=" + query;

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Search.this.handleResponse(response);
//                System.out.println(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(stringRequest);
    }

    private void redrawResults() {
        _searchResultList.removeAllViewsInLayout();

        LinearLayout bookshelf = new LinearLayout(this);
        bookshelf.setOrientation(LinearLayout.VERTICAL);

        //add books to scroll bar
        for (int i = 0; i < results.size(); i++) {
            //create horizontal linear layout to hold book and remove button
            LinearLayout bookView = new LinearLayout(this);
            bookView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            bookView.setOrientation(LinearLayout.HORIZONTAL);
            bookView.setGravity(Gravity.CENTER);

            //create book view
            LinearLayout bookArea = new LinearLayout(this);
            bookArea.setOrientation(LinearLayout.VERTICAL);
            bookArea.setPadding(16,0,16,0);
            bookArea.setLayoutParams( new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f));

            //create text view for book title
            TextView bookTitle = new TextView(this);
            bookTitle.setText(results.get(i).title);
            bookTitle.setTextSize(30);

            //create Image view for book picture
            ImageView bookImage = new ImageView( this);
            int maxHeight = 300;
            int maxWidth = 200;
            bookImage.setBackgroundColor(Color.BLUE);
            bookImage.setMaxHeight(maxHeight);
            bookImage.setMinimumHeight(maxHeight);
            bookImage.setMinimumWidth(maxWidth);
            bookImage.setMaxWidth(maxWidth);
            Picasso.with(getBaseContext()).load(results.get(i).imageLink).into(bookImage);

            //add text view and image view to book linear layout
            bookArea.addView(bookTitle);
            bookArea.addView(bookImage);
            bookView.addView(bookArea);
            bookshelf.addView(bookView);
        }

        //add to scroll view
        _searchResultList.addView(bookshelf);
    }

    private void handleResponse(String response) {
        try {
            ArrayList<BookItem> results = new ArrayList<>();

            JSONObject data = new JSONObject(response);
            JSONArray items = data.getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                JSONObject volumeInfo = item.getJSONObject("volumeInfo");
                JSONArray authorsJSONArray = volumeInfo.getJSONArray("authors");

                BookItem book = new BookItem();
                book.id = item.getString("id");
                book.title = volumeInfo.getString("title");
                for (int j = 0; j < authorsJSONArray.length(); j++) {
                    book.authors.add(authorsJSONArray.getString(j));
                }
                book.description = volumeInfo.optString("description");
                book.imageLink = volumeInfo.getJSONObject("imageLinks").optString("thumbnail");

                results.add(book);
            }

            this.results = results;

            this.redrawResults();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
