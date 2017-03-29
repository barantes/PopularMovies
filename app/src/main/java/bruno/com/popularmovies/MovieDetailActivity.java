package bruno.com.popularmovies;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import bruno.com.popularmovies.model.MovieData;
import bruno.com.popularmovies.tmdb.MovieImageUtil;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieDetailActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE = "movie";

    private MovieData mMovie;

    @BindView(R.id.tv_movie_title)
    TextView mTvTitle;

    @BindView(R.id.tv_release_date)
    TextView mTvReleaseDate;

    @BindView(R.id.rb_average_rating)
    RatingBar mRbRating;

    @BindView(R.id.tv_overview)
    TextView mTvOverview;

    @BindView(R.id.iv_movie_poster)
    ImageView mIvPoster;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar appBar = getSupportActionBar();
        if(appBar != null) {
            appBar.setDisplayHomeAsUpEnabled(true);
        }
        setMovie();
        setContentView(R.layout.activity_movie_detail);
        ButterKnife.bind(this);
        setViews();
    }

    private void setViews() {
        Picasso
                .with(this)
                .load(MovieImageUtil.getPosterUrl(this, mMovie))
                .into(mIvPoster);

        mTvTitle.setText(mMovie.getOriginalTitle());
        mTvReleaseDate.setText(String.format(getString(R.string.release_date), mMovie.getReleaseDate()));
        mRbRating.setRating(getRating());
        mTvOverview.setText(mMovie.getOverview());
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
