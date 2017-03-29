package bruno.com.popularmovies;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.List;

import bruno.com.popularmovies.adapter.MovieAdapter;
import bruno.com.popularmovies.model.MovieData;
import bruno.com.popularmovies.model.Movies;
import bruno.com.popularmovies.tmdb.MovieService;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemSelected;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity
        implements MovieAdapter.MovieClickListener {

    private static final String TAG = MainActivity.class.getName();
    private static final String KEY_MOVIES = "movies";
    private static final String KEY_SORT_OPTION = "sort";

    @BindView(R.id.rv_movies)
    RecyclerView mRvMovies;

    @BindView(R.id.spinner)
    Spinner mSpinner;

    private CompositeSubscription mSubscriptions;
    private Movies mMovies;
    private MovieService mMovieService;
    private ProgressDialog mProgressDialog;

    private static final int POSITION_SORT_POPULAR = 0;
    private static final int POSITION_SORT_TOP_RATED = 1;
    private int mCurrentSortOption = POSITION_SORT_POPULAR;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSubscriptions = new CompositeSubscription();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initRecyclerView();
        initMovieService();
        initData(savedInstanceState);
    }

    private void initMovieService() {
        Retrofit retrofit = buildRetrofit();
        mMovieService = retrofit.create(MovieService.class);
    }

    private void initData(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Log.d(TAG, "initData(): restoring saved state");
            mCurrentSortOption = savedInstanceState.containsKey(KEY_SORT_OPTION) ?
                    savedInstanceState.getInt(KEY_SORT_OPTION) : POSITION_SORT_POPULAR;

            if(savedInstanceState.containsKey(KEY_MOVIES)) {
                Log.d(TAG, "initData(): loading movies from saved state");
                mMovies = savedInstanceState.getParcelable(KEY_MOVIES);
                displayMovies();
                return;
            }
        }

        Log.d(TAG, "initData(): will load live movie data");
        checkPermissionsAndLoadMovies();
    }

    private void checkPermissionsAndLoadMovies() {
        RxPermissions rxPermissions = new RxPermissions(this);
        Subscription subscription = rxPermissions
                .request(Manifest.permission.INTERNET)
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "checkPermissionsAndLoadMovies.onCompleted()");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "checkPermissionsAndLoadMovies.onError()", e);
                    }

                    @Override
                    public void onNext(Boolean granted) {
                        Log.d(TAG, "checkPermissionsAndLoadMovies.onNext()");
                        if (granted) {
                            loadMovies();
                        } else {
                            Toast.makeText(MainActivity.this, R.string.error_internet_permission_denied, Toast.LENGTH_LONG).show();
                        }
                    }
                });
        mSubscriptions.add(subscription);
    }

    private void loadMovies() {
        showProgressDialog();
        Observable<Movies> observable;
        switch (mCurrentSortOption) {
            case POSITION_SORT_TOP_RATED:
                observable = getTopRatedMovies();
                break;

            default:
                observable = getPopularMovies();
                break;
        }

        Subscription subscription = observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Movies>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "loadPopularMovies.onCompleted()");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "loadPopularMovies.onError()", e);
                        hideProgressDialog();
                        showErrorMessage(getString(R.string.load_movies_error));
                    }

                    @Override
                    public void onNext(Movies movies) {
                        Log.d(TAG, "loadPopularMovies.onNext()");
                        mMovies = movies;
                        displayMovies();
                    }
                });
        mSubscriptions.add(subscription);
    }

    private Observable<Movies> getPopularMovies() {
        return mMovieService.getPopularMovies(getString(R.string.tmdbApiKey));
    }

    private Observable<Movies> getTopRatedMovies() {
        return mMovieService.getTopRatedMovies(getString(R.string.tmdbApiKey));
    }

    private void displayMovies() {
        if (mMovies != null) {
            List<MovieData> list = mMovies.getResults();
            MovieAdapter adapter = new MovieAdapter(list, this);
            mRvMovies.setAdapter(adapter);
            hideProgressDialog();
        }
    }

    private Retrofit buildRetrofit() {
        return new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(getString(R.string.tmdbApiBaseUrl))
                .build();
    }

    private void initRecyclerView() {
        mRvMovies.setHasFixedSize(true);
        GridLayoutManager layoutManager = new AutofitGridLayoutManager(this,
                getResources().getDimensionPixelSize(R.dimen.movie_thumbnail_width)
                        + getResources().getDimensionPixelSize(R.dimen.padding_1x));
        mRvMovies.setLayoutManager(layoutManager);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_MOVIES, mMovies);
        outState.putInt(KEY_SORT_OPTION, mCurrentSortOption);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (mSubscriptions != null) {
            mSubscriptions.unsubscribe();
        }
        super.onDestroy();
    }

    private void openMovieDetails(MovieData movie) {
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE, movie);
        startActivity(intent);
    }

    private void showProgressDialog() {
        if(mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.progress_dialog_text));
            mProgressDialog.setCancelable(true);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if(mProgressDialog != null) {
            mProgressDialog.dismiss();
            // currently not cancelling any ongoing network requests if user
            // cancels the progress dialog
        }
    }

    private void showErrorMessage(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    @Override
    public void onMovieClick(MovieData movie) {
        openMovieDetails(movie);
    }

    @OnItemSelected(R.id.spinner)
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        Log.d(TAG, String.format("onItemSelected(): position = %d", position));
        if(position != mCurrentSortOption) {
            mCurrentSortOption = position;
            loadMovies();
        }
    }
}
