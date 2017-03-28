package bruno.com.popularmovies.tmdb;

import android.content.Context;

import bruno.com.popularmovies.R;
import bruno.com.popularmovies.model.MovieData;

/**
 * Created by Bruno on 19/03/2017.
 */

public class MovieImageUtil {
    public static String getPosterUrl(Context context, MovieData movie) {
        if(context != null
                && movie != null) {
            StringBuilder url = new StringBuilder(context.getString(R.string.tmdbImageBaseUrl))
                    .append(context.getString(R.string.movieThumbnailWidth))
                    .append("/")
                    .append(movie.getPosterPath());
            return url.toString();
        }
        return null;
    }
}
