import java.util.HashMap;

//Bir kullaniciyi temsil eden model sinifidir user ID'si ve puanlamalari tutar
public class User {

    public int userId;
    public HashMap<Integer, Integer> ratings; //movieId -> puan (1-5). Yalnizca sifirdan buyuk puanlar saklanir sifirlar tutulmaz

    public User(int userId) {
        this.userId = userId;
        this.ratings = new HashMap<>();
    }

    // Puanı yalnızca sıfırdan büyükse ekler bellek tasarrufu için 0ı saklamaz
    public void addRating(int movieId, int rating) {
        if (rating > 0) {
            ratings.put(movieId, rating);
        }
    }

    //verilen film için puanı döner eğer kullanıcı puanlamadıysa 0 döner
    public int getRating(int movieId) {
        return ratings.getOrDefault(movieId, 0);
    }
}
