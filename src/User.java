import java.util.HashMap;

public class User {
    public int userId;
    // movieId -> rating (0 olmayan puanlar tutulur, bellek tasarrufu)
    public HashMap<Integer, Integer> ratings;

    public User(int userId) {
        this.userId = userId;
        this.ratings = new HashMap<>();
    }

    public void addRating(int movieId, int rating) {
        if (rating > 0) {
            ratings.put(movieId, rating);
        }
    }

    public int getRating(int movieId) {
        return ratings.getOrDefault(movieId, 0);
    }
}
