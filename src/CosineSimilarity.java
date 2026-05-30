//iki kullanıcının film zevkleri ne kadar benziyor o hesaplanır
// Formül: sim(A,B) = (A·B) / (|A| × |B|)
public class CosineSimilarity {

    public static double compute(User firstUser, User secondUser) {

        // iki kullanıcı da en az bir film puanlamış olmalı
        if (firstUser.ratings.isEmpty() || secondUser.ratings.isEmpty()) return 0.0;

        double dotProduct      = 0.0; // A · B  iki kullanıcının ortak puanlarının çarpımı
        double magnitudeFirst  = 0.0; // |A|²   birinci kullanıcının puan büyüklüğü
        double magnitudeSecond = 0.0; // |B|²

        //Birinci kullanıcının filmlere verdiği tüm puanların kareleri toplanıyor
        for (double rating : firstUser.ratings.values()) {
            magnitudeFirst += rating * rating;
        }

        // second user için hesaplanıyor
        for (double rating : secondUser.ratings.values()) {
            magnitudeSecond += rating * rating;
        }

        //ortak puanlanan filmlere verilen puan hesaplanır
        //birinci kullanıcının puanladığı filmler geziliyor eğer ikinci kullanıcı da aynı filmi puanladıysa onun puanı alınır
        for (int movieId : firstUser.ratings.keySet()) {
            double ratingFromFirst  = firstUser.ratings.get(movieId);
            double ratingFromSecond = secondUser.getRating(movieId); // izlemediyse 0 döner
            dotProduct += ratingFromFirst * ratingFromSecond;
        }

        // Sıfıra bölme koruması
        if (magnitudeFirst == 0.0 || magnitudeSecond == 0.0) return 0.0;

        //cosine similarity formülü
        return dotProduct / (Math.sqrt(magnitudeFirst) * Math.sqrt(magnitudeSecond));
    }
}