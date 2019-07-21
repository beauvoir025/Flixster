package com.example.flixster;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flixster.adapters.MovieAdapter;
import com.example.flixster.models.Movie;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    private static final String MOVIE_URL ="https://api.themoviedb.org/3/movie/now_playing?api_key=a07e22bc18f5cb106bfe4cc1f83ad8ed";

     List<Movie> movies;
     //Using a RecyclerView has the following key steps: -ok
    //Add RecyclerView support library to the Gradle build file -ok
    //Define a model class to use as the data source -ok
    //Add a RecyclerView to your activity to display the items -ok
    //Create a custom row layout XML file to visualize the item -ok
    //Create a RecyclerView.Adapter and ViewHolder to render the item -ok
    //Bind the adapter to the data source to populate the RecyclerView

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView rvMovies =  findViewById(R.id.rvMovies);
        movies = new ArrayList<>();
        final MovieAdapter adapter = new MovieAdapter(this, movies);
        rvMovies.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvMovies.setAdapter(adapter);


        AsyncHttpClient client = new AsyncHttpClient();
        client.get(MOVIE_URL, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray movieJsonArray = response.getJSONArray("results");
                      movies.addAll(Movie.fromJsonArray(movieJsonArray));
                      adapter.notifyDataSetChanged();
                    Log.d("smile", movies.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    public static class DetailActivity extends YouTubeBaseActivity {

        private   static  final String YOUTUBE_API_KEY = "AIzaSyDFK40h3r8cO45UP2QslIxJRIEP1wIyzjA";
        static  final String TRAILERS_API ="https://api.themoviedb.org/3/movie/%d/videos?api_key=a07e22bc18f5cb106bfe4cc1f83ad8ed";

        TextView tvTitle;
        TextView tvOverview;
        RatingBar ratingBar;
        YouTubePlayerView youTubePlayerView;

        Movie movie;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_detail);

            tvTitle = findViewById(R.id.tvTitle);
            tvOverview = findViewById(R.id.tvOverview);
            ratingBar = findViewById(R.id.ratingBar);
            youTubePlayerView = findViewById(R.id.player);

            String title = getIntent().getStringExtra("title");
            movie = (Movie) Parcels.unwrap(getIntent().getParcelableExtra("movie"));
            tvTitle.setText(movie.getTitle());
            tvOverview.setText(movie.getOverview());
            ratingBar.setRating((float)movie.getVoteAverage());
            AsyncHttpClient client = new AsyncHttpClient();
            client.get(String.format(TRAILERS_API, movie.getMovieId()), new JsonHttpResponseHandler()  {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    try {
                        JSONArray results = response.getJSONArray("results");
                        if (results.length() == 0){
                            return;
                        }
                        JSONObject movieTrailer = results.getJSONObject(0);
                       String youtubekey = movieTrailer.getString("key");
                        initializeYoutube(youtubekey);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                }
            });





        }

        private void initializeYoutube(final String youtubekey) {

            youTubePlayerView.initialize(YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener() {
                @Override
                public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                    Log.d("smile", "on init success");
                    if (movie.getVoteAverage() >= 5){
                        youTubePlayer.loadVideo(youtubekey);
                        youTubePlayerView.animate();
                    }
                    else{
                        youTubePlayer.cueVideo(youtubekey);
                    }
                    // do any work here to cue video, play video, etc.
                   // youTubePlayer.cueVideo(youtubekey);
                }

                @Override
                public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                    Log.d("smile", "on init failure");
                }
            });

        }



    }
}
