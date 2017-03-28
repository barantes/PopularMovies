package bruno.com.popularmovies;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import bruno.com.popularmovies.model.MovieData;
import bruno.com.popularmovies.tmdb.MovieImageUtil;

public class MovieDetailActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE = "movie";

    private MovieData mMovie;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMovie();
        setContentView(R.layout.activity_movie_detail);
        setViews();
    }

    private void setViews() {
        ImageView poster = (ImageView) findViewById(R.id.iv_movie_poster);
        Picasso
                .with(this)
                .load(MovieImageUtil.getPosterUrl(this, mMovie))
                .into(poster);

        TextView title = (TextView) findViewById(R.id.tv_movie_title);
        title.setText(mMovie.getOriginalTitle());

        TextView releaseDate = (TextView) findViewById(R.id.tv_release_date);
        releaseDate.setText(String.format(getString(R.string.release_date), mMovie.getReleaseDate()));

        RatingBar rating = (RatingBar) findViewById(R.id.rb_average_rating);
        rating.setRating(getRating());

        TextView overview = (TextView) findViewById(R.id.tv_overview);
        overview.setText(mMovie.getOverview());
    }

    private float getRating() {
        // Ratings in the API response range from 0.5 to 10
        // Let's divide by two to fit a 5-stars rating bar:
        return (float) mMovie.getVoteAverage() / 2.0f;
    }

    private void setMovie() {
        mMovie = getIntent().getParcelableExtra(EXTRA_MOVIE);
    }
}
