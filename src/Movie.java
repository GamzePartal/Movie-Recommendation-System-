// Bir filmi temsil eder
// Sadece ID ve başlık tutulur genres algoritma için gerekli değil
public class Movie {

    public int    movieId;
    public String title;

    public Movie(int movieId, String title) {
        this.movieId = movieId;
        this.title   = title;
    }

    @Override
    public String toString() { return title; }
}