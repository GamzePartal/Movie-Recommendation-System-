import java.util.*;

public class Recommender {

    private List<User> allUsers; //main_data.csv içindeki bütün kullanıcıları tutar, sistem hedef kullanıcıyı bu listedeki kullanıcılarla belirler
    private Map<Integer, Movie> movieMap; //Film ID den film adına ulaşmak için kullanılır çünkü algoritma  ID ile çalışır ama kullanıcıya film adı gösterilir

    public Recommender(List<User> allUsers, Map<Integer, Movie> movieMap) {
        this.allUsers = allUsers;
        this.movieMap = movieMap;
    }

    //Hedef kullanıcıya göre film önerisi üretir
    public List<String> recommend(User targetUser, int similarUserCount, int moviesPerUser) {
        List<String> recommendedMovies = new ArrayList<>();  //önce boş liste oluştur önerilecek film adlarını tutar

        if (targetUser == null || similarUserCount <= 0 || moviesPerUser <= 0 || allUsers.isEmpty()) {
            return recommendedMovies;
        }

        MaxHeap userHeap = new MaxHeap(); //user heap oluştur, kullanıcıları similarity değerine göre tutacak

       // tüm kullanıcılarla similarity hesaplanıyor
        for (User candidate : allUsers) {
            if (candidate.userId == targetUser.userId) continue;

            double similarityScore = CosineSimilarity.compute(targetUser, candidate);

            if (similarityScore > 0.0) { //Similarity 0dan büyükse heape ekle.
                userHeap.insert(candidate, similarityScore);
            }
        }

        System.out.println("Hedef Kullanıcı ID : " + targetUser.userId);
        System.out.println("K=" + similarUserCount + " | X=" + moviesPerUser
                + " | Beklenen toplam=" + (similarUserCount * moviesPerUser));
        System.out.println("Heap'e eklenen kullanıcı sayısı: " + userHeap.getSize());

        //en benzer kullanıcılar burada tut
        List<User> topSimilarUsers = new ArrayList<>();

        for (int i = 0; i < similarUserCount && !userHeap.isEmpty(); i++) {
            HeapNode node = userHeap.extractMax(); // heapten extractMax() ile en yüksek similarity qye sahip kullanıcılar çekilir

            if (node != null) {
                System.out.println("  " + (i + 1) + ". Kullanıcı ID: " + node.user.userId
                        + " | Benzerlik: " + String.format("%.4f", node.similarity));
                topSimilarUsers.add(node.user);
            }
        }

        // aynı filmi tekrar önermeyi engeller
        Set<Integer> alreadyRecommendedMovieIds = new HashSet<>();

        for (User similarUser : topSimilarUsers) {
            recommendedMovies.addAll(  // her kullanıcıdan X tane film alınır
                    getTopMovies(similarUser, targetUser, moviesPerUser, alreadyRecommendedMovieIds)
            );
        }
        System.out.println("Toplam önerilen film: " + recommendedMovies.size());
        return recommendedMovies;
    }

    //Bir benzer kullanıcının en yüksek puan verdiği filmleri seçmek
    private List<String> getTopMovies(User similarUser, User targetUser, int movieLimit, Set<Integer> alreadyRecommendedMovieIds) {

        List<String> selectedMovies = new ArrayList<>();
        MaxHeap movieHeap = new MaxHeap(); //Bu heap filmleri rating değerine göre sıralar

        for (Map.Entry<Integer, Integer> entry : similarUser.ratings.entrySet()) {
            int movieId     = entry.getKey();
            int movieRating = entry.getValue();

            if (targetUser.getRating(movieId) > 0)            continue; //hedef kullanıcı bu filmi zaten puanlamışsa önerilmez
            if (alreadyRecommendedMovieIds.contains(movieId)) continue; //film daha önce önerildiyse tekrar önerilmez
            if (!movieMap.containsKey(movieId))               continue; //film adı movies.csv içinde yoksa önerilmez

            Movie movie = movieMap.get(movieId);
            movieHeap.insertMovie(movie, movieRating);
        }

        while (!movieHeap.isEmpty() && selectedMovies.size() < movieLimit) {
            HeapNode node = movieHeap.extractMax();

            if (node == null || node.movie == null) continue;

            int movieId = node.movie.movieId;

            if (alreadyRecommendedMovieIds.contains(movieId)) continue;

            alreadyRecommendedMovieIds.add(movieId);
            selectedMovies.add(node.movie.title);
        }

        return selectedMovies; //benzer kullanıcıdan seçilen film adları döner
    }

    //ikinci ekran için kullanıcı 5 film seçip puan verdiğinde çalışır
    public List<String> recommendFromRatings(Map<Integer, Integer> userRatings,int similarUserCount, int moviesPerUser) {
        User virtualUser = new User(-1); //gerçek veri setinde olmayan geçici bir kullanıcı oluştur

        //kullanıcının seçtiği filmler ve verdiği puanlar  sanal kullanıcıya eklenir
        for (Map.Entry<Integer, Integer> entry : userRatings.entrySet()) {
            int movieId = entry.getKey();
            int rating  = entry.getValue();

            if (rating >= 1 && rating <= 5) {
                virtualUser.addRating(movieId, rating);
                System.out.println("Film ID: " + movieId + " | Puan: " + rating);
            }
        }

        return recommend(virtualUser, similarUserCount, moviesPerUser);
    }
}