package com.movieadvisor.Model;

import java.io.Serializable;
import java.util.ArrayList;

public class Movie implements Serializable {

    private String id;
    private String name;
    private String poster;
    private String overview;
    private String voteAverage;
    private String releaseDate;
    private String voteCount;
    private String backdrop;
    private ArrayList<String> genres;

    public Movie(String id, String name, String poster, String overview, String voteAverage, String releaseDate, String voteCount, String backdrop, ArrayList<String> genres) {
        this.id = id;
        this.name = name;
        this.poster = poster;
        this.overview = overview;
        this.voteAverage = voteAverage;
        this.releaseDate = releaseDate;
        this.voteCount = voteCount;
        this.backdrop = backdrop;
        this.genres = genres;
    }

    public static String getGenreTitle(int genreId) {
        switch (genreId) {
            case 28:
                return "Action";
            case 12:
                return "Adventure";
            case 16:
                return "Animation";
            case 35:
                return "Comedy";
            case 80:
                return "Crime";
            case 99:
                return "Documentary";
            case 18:
                return "Drama";
            case 10751:
                return "Family";
            case 14:
                return "Fantasy";
            case 36:
                return "History";
            case 27:
                return "Horror";
            case 10402:
                return "Music";
            case 9648:
                return "Mystery";
            case 10749:
                return "Romance";
            case 878:
                return "Science Fiction";
            case 10770:
                return "TV Movie";
            case 53:
                return "Thriller";
            case 10752:
                return "War";
            case 37:
                return "Western";
            default:
                return "Unknown";
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPoster() {
        return poster;
    }

    public String getOverview() {
        return overview;
    }

    public String getVoteAverage() {
        return voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getVoteCount() {
        return voteCount;
    }

    public String getBackdrop() {
        return backdrop;
    }

    public ArrayList<String> getGenres() {
        return genres;
    }
}
