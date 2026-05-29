import java.util.*;

// Film öneri motoru
//
// Proje dokümanına göre:
// K = heap'ten çekilecek benzer kullanıcı sayısı
// X = her benzer kullanıcıdan alınacak en yüksek puanlı film sayısı
// Toplam öneri = K * X
//
// Örnek:
// K = 3, X = 5 ise toplam 15 öneri listelenir.
public class Recommender {

    private List<User> allUsers;
    private Map<Integer, Movie> movieMap;

    public Recommender(List<User> allUsers, Map<Integer, Movie> movieMap) {
        this.allUsers = allUsers;
        this.movieMap = movieMap;
    }

    // targetUser için öneri üretir
    // similarUserCount = K
    // moviesPerUser = X
    public List<String> recommend(User targetUser, int similarUserCount, int moviesPerUser) {
        List<String> recommendedMovies = new ArrayList<>();

        if (targetUser == null || similarUserCount <= 0 || moviesPerUser <= 0 || allUsers.isEmpty()) {
            return recommendedMovies;
        }

        // 1. Tüm kullanıcıların hedef kullanıcıya similarity skorunu hesapla ve heap'e ekle
        MaxHeap userHeap = new MaxHeap();

        for (User candidate : allUsers) {
            if (candidate.userId == targetUser.userId) {
                continue;
            }

            double similarityScore = CosineSimilarity.compute(targetUser, candidate);

            // 0 similarity gerçek bir benzerlik göstermediği için eklemiyoruz
            if (similarityScore > 0.0) {
                userHeap.insert(candidate, similarityScore);
            }
        }

        // 2. Heap'ten en benzer K kullanıcıyı çek
        List<User> topSimilarUsers = new ArrayList<>();

        for (int i = 0; i < similarUserCount && !userHeap.isEmpty(); i++) {
            HeapNode node = userHeap.extractMax();

            if (node != null) {
                System.out.println("Benzer Kullanıcı " + (i + 1)
                        + ": ID = " + node.user.userId
                        + ", Similarity = " + node.similarity);

                topSimilarUsers.add(node.user);
            }
        }

        // 3. Her benzer kullanıcıdan X tane en yüksek puanlı filmi al
        Set<Integer> alreadyRecommendedMovieIds = new HashSet<>();

        for (User similarUser : topSimilarUsers) {
            List<String> moviesFromThisUser = getTopMovies(
                    similarUser,
                    targetUser,
                    moviesPerUser,
                    alreadyRecommendedMovieIds
            );

            recommendedMovies.addAll(moviesFromThisUser);
        }

        return recommendedMovies;
    }

    // Bir benzer kullanıcının en yüksek puanlı filmlerini heap ile seçer
    private List<String> getTopMovies(User similarUser,
                                      User targetUser,
                                      int movieLimit,
                                      Set<Integer> alreadyRecommendedMovieIds) {

        List<String> selectedMovies = new ArrayList<>();
        MaxHeap movieHeap = new MaxHeap();

        // Benzer kullanıcının puanladığı filmleri movieHeap'e koy
        for (Map.Entry<Integer, Integer> entry : similarUser.ratings.entrySet()) {
            int movieId = entry.getKey();
            int movieRating = entry.getValue();

            // Hedef kullanıcı zaten puanlamışsa önerme
            if (targetUser.getRating(movieId) > 0) {
                continue;
            }

            // Aynı filmi tekrar önerme
            if (alreadyRecommendedMovieIds.contains(movieId)) {
                continue;
            }

            // Film adı yoksa ID basmamak için atla
            if (!movieMap.containsKey(movieId)) {
                continue;
            }

            // MaxHeap User kabul ettiği için movieId'yi proxy User içinde taşıyoruz
            User movieProxy = new User(movieId);
            movieHeap.insert(movieProxy, movieRating);
        }

        // Heap'ten en yüksek puanlı X filmi çek
        while (!movieHeap.isEmpty() && selectedMovies.size() < movieLimit) {
            HeapNode node = movieHeap.extractMax();

            if (node == null) {
                break;
            }

            int movieId = node.user.userId;
            Movie movie = movieMap.get(movieId);

            if (movie == null) {
                continue;
            }

            if (alreadyRecommendedMovieIds.contains(movieId)) {
                continue;
            }

            alreadyRecommendedMovieIds.add(movieId);
            selectedMovies.add(movie.title);
        }

        return selectedMovies;
    }

    // Ekran 2 için: kullanıcının manuel girdiği film puanlarından sanal kullanıcı oluşturur
    public List<String> recommendFromRatings(Map<Integer, Integer> userRatings,
                                             int similarUserCount,
                                             int moviesPerUser) {

        User virtualUser = new User(-1);

        for (Map.Entry<Integer, Integer> entry : userRatings.entrySet()) {
            int movieId = entry.getKey();
            int rating = entry.getValue();

            if (rating >= 1 && rating <= 5) {
                virtualUser.addRating(movieId, rating);
            }
        }

        return recommend(virtualUser, similarUserCount, moviesPerUser);
    }
}