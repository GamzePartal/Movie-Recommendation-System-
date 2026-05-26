//Iki kullanici arasindaki benzerlik skorunu hesaplayan yardimci siniftir
//Formul:   sim(A, B)  =  (A . B)  /  (|A| x |B|)
public class CosineSimilarity {

    //2 kullanıcı arasındaki benzerliği hesaplar
    public static double compute(User userA, User userB) {
        if (userA.ratings.isEmpty() || userB.ratings.isEmpty()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double magA = 0.0;
        double magB = 0.0;

        // A'nın puanladığı filmler üzerinden dot product ve |A| hesapla
        for (int movieId : userA.ratings.keySet()) {
            double ratingA = userA.ratings.get(movieId);
            double ratingB = userB.getRating(movieId); // 0 dönebilir

            dotProduct += ratingA * ratingB;
            magA += ratingA * ratingA;
        }

        // |B| hesapla (B'nin kendi nonzero'ları üzerinden)
        for (int rating : userB.ratings.values()) {
            magB += (double) rating * rating;
        }

        // Sıfır bölme koruması
        if (magA == 0.0 || magB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(magA) * Math.sqrt(magB));
    }
}