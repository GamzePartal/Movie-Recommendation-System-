import java.util.HashMap;

// Bir kullanıcıyı temsil eder
// ratings: movieId → puan (1-5), sıfır puanlar saklanmaz
public class User {

    public int                       userId;
    public HashMap<Integer, Integer> ratings;

    public User(int userId) {
        this.userId  = userId;
        this.ratings = new HashMap<>();
    }

    // Puan ekle — 0 ve altını kaydetme, gereksiz yer kaplar
    public void addRating(int movieId, int rating) {
        if (rating > 0) {
            ratings.put(movieId, rating);
        }
    }

    // Kullanıcının o filme verdiği puanı döndür, izlemediyse 0
    public int getRating(int movieId) {
        return ratings.getOrDefault(movieId, 0);
    }
}