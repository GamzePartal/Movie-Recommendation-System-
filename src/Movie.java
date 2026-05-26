//Bir filmi temsil eden model sinifi
// Yalnizca ID ve baslik bilgisi tutulur
// genres algoritmada kullanilmadigi icin saklanmaz
public class Movie {
    public int movieId;
    public String title;

    public Movie(int movieId, String title) {
        this.movieId = movieId;
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
