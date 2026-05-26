import java.util.*;

// CosineSimilarity, MaxHeap ve film katalogu kullanarak hedef kullaniciya X*K film oneri listesi uretir

public class Recommender {

    private List<User> allUsers;         // main_data.csv'den gelen 600 kullanıcı
    private Map<Integer, Movie> movies;  // movieId → Movie

    public Recommender(List<User> allUsers, Map<Integer, Movie> movies) {
        this.allUsers = allUsers;
        this.movies = movies;
    }


    public List<String> recommend(User targetUser, int X, int K) {
        List<String> recommendations = new ArrayList<>();

        if (X <= 0 || K <= 0 || allUsers.isEmpty()) {
            return recommendations;
        }

        // 1. Tüm kullanıcılar için similarity hesapla ve heap'e ekle
        MaxHeap heap = new MaxHeap();
        for (User u : allUsers) {
            if (u.userId == targetUser.userId) continue; // kendisiyle karşılaştırma
            double sim = CosineSimilarity.compute(targetUser, u);
            heap.insert(u, sim);
        }

        // 2. En benzer X kullanıcıyı heap'ten çek
        List<User> topUsers = new ArrayList<>();
        for (int i = 0; i < X && !heap.isEmpty(); i++) {
            HeapNode node = heap.extractMax();
            topUsers.add(node.user);
        }

        // 3. Her kullanıcıdan K en yüksek puanlı filmi al
        //    (Hedef kullanıcının zaten puanladığı filmleri atlıyoruz)
        for (User similarUser : topUsers) {
            List<String> topMovies = getTopKMovies(similarUser, targetUser, K);
            recommendations.addAll(topMovies);
        }

        return recommendations;
    }

    // Bir kullanıcının K en yüksek puanlı filmini döner , hedef kullanıcının izlediklerini atlar
    private List<String> getTopKMovies(User similarUser, User targetUser, int K) {
        // movieId → rating çiftlerini puana göre büyükten küçüğe sırala
        List<Map.Entry<Integer, Integer>> entries = new ArrayList<>(similarUser.ratings.entrySet());

        entries.sort((a, b) -> b.getValue() - a.getValue()); // azalan sıra

        List<String> result = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : entries) {
            if (result.size() >= K) break;

            int movieId = entry.getKey();

            // Hedef kullanıcı bu filmi zaten izlemişse atla
            if (targetUser.getRating(movieId) > 0) continue;

            // Film adını bul
            Movie movie = movies.get(movieId);
            if (movie != null) {
                result.add(movie.title);
            } else {
                result.add("Film ID: " + movieId); // movies.csv'de yoksa
            }
        }

        return result;
    }

    // ekran 2 için: kullanıcının elle girdiği puanlar
    public List<String> recommendFromRatings(Map<Integer, Integer> movieRatings, int X, int K) {
        // Sahte bir "hedef kullanıcı" oluştur (ID: -1, ana veride yok)
        User virtualUser = new User(-1);
        for (Map.Entry<Integer, Integer> entry : movieRatings.entrySet()) {
            virtualUser.addRating(entry.getKey(), entry.getValue());
        }
        return recommend(virtualUser, X, K);
    }
}