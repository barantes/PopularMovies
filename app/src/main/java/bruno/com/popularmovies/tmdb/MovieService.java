package bruno.com.popularmovies.tmdb;

import bruno.com.popularmovies.model.Movies;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Bruno on 19/03/2017.
 */

public interface MovieService {
    @GET("movie/popular?")
    public Observable<Movies> getPopularMovies(@Query("api_key") String apiKey);

    @GET("movie/top_rated?")
    public Observable<Movies> getTopRatedMovies(@Query("api_key") String apiKey);
}
