// İki kullanıcı arasındaki benzerliği hesaplar
// Formül: sim(A,B) = (A·B) / (|A| × |B|)
// Sonuç 0.0 (hiç benzer değil) ile 1.0 (aynı zevk) arasındadır
public class CosineSimilarity {

    public static double compute(User firstUser, User secondUser) {

        // Her iki kullanıcı da en az bir film puanlamış olmalı
        if (firstUser.ratings.isEmpty() || secondUser.ratings.isEmpty()) return 0.0;

        double dotProduct      = 0.0; // A · B
        double magnitudeFirst  = 0.0; // |A|²
        double magnitudeSecond = 0.0; // |B|²

        // |A|² → firstUser'ın TÜM puanları üzerinden (sadece ortak filmler değil)
        for (double rating : firstUser.ratings.values()) {
            magnitudeFirst += rating * rating;
        }

        // |B|² → secondUser'ın TÜM puanları üzerinden
        for (double rating : secondUser.ratings.values()) {
            magnitudeSecond += rating * rating;
        }

        // Dot product → her iki kullanıcının da puanladığı filmler üzerinden
        for (int movieId : firstUser.ratings.keySet()) {
            double ratingFromFirst  = firstUser.ratings.get(movieId);
            double ratingFromSecond = secondUser.getRating(movieId); // izlemediyse 0 döner
            dotProduct += ratingFromFirst * ratingFromSecond;
        }

        // Sıfıra bölme koruması
        if (magnitudeFirst == 0.0 || magnitudeSecond == 0.0) return 0.0;

        return dotProduct / (Math.sqrt(magnitudeFirst) * Math.sqrt(magnitudeSecond));
    }
}