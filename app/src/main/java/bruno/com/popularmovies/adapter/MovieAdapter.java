package bruno.com.popularmovies.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import bruno.com.popularmovies.R;
import bruno.com.popularmovies.model.MovieData;
import bruno.com.popularmovies.tmdb.MovieImageUtil;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Bruno on 19/03/2017.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    private List<MovieData> mMovies;
    private MovieClickListener mMovieClickListener;

    public MovieAdapter(List<MovieData> movies, MovieClickListener listener) {
        this.mMovies = movies;
        this.mMovieClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(mMovies != null) {
            MovieData movie = mMovies.get(position);
            if(movie != null) {
                Picasso
                        .with(holder.ivPoster.getContext())
                        .load(getMoviePosterUrl(movie, holder.ivPoster.getContext()))
                        .into(holder.ivPoster);
            }
        }
    }

    private String getMoviePosterUrl(MovieData movie, Context context) {
        return MovieImageUtil.getPosterUrl(context, movie);
    }

    @Override
    public int getItemCount() {
        if(mMovies != null) {
            return mMovies.size();
        }
        return 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.iv_movie_poster)
        ImageView ivPoster;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onMovieClick();
        }

        private void onMovieClick() {
            int position = getAdapterPosition();
            if(mMovieClickListener != null
                    && position != RecyclerView.NO_POSITION) {
                MovieData movie = mMovies.get(position);
                mMovieClickListener.onMovieClick(movie);
            }
        }
    }

    public interface MovieClickListener {
        void onMovieClick(MovieData movie);
    }
}
