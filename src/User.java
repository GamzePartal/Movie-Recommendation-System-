import java.util.HashMap;

//bir kullanıcıyı ve o kullanıcının film puanlarını tutar
public class User {

    public int userId;
    public HashMap<Integer, Integer> ratings; //kullanıcının puanladığı filmleri tutar movieId,rating

    public User(int userId) {
        this.userId  = userId;
        this.ratings = new HashMap<>();
    }

    // kullanıcı idsi verilen filme parametre olarak verilen puanı verir
    public void addRating(int movieId, int rating) {
        if (rating > 0) {
            ratings.put(movieId, rating);
        }
    }

    // filme verilen puanın döndürür izlemediyse sıfır döner
    public int getRating(int movieId) {
        return ratings.getOrDefault(movieId, 0);
    }
}