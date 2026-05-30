import java.io.*;
import java.util.*;

// Üç CSV dosyasını okuyup Java nesnelerine dönüştürür
public class CSVReader {

    // main_data.csv → kullanıcı listesi
    public static List<User> readMainData(String filePath) {
        List<User> userList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            String headerLine = reader.readLine();
            if (headerLine == null) return userList;
            headerLine = headerLine.trim(); // \r\n koruması

            String[] columns  = headerLine.split(",");
            int[]    movieIds = new int[columns.length];

            // Başlık satırından film ID'lerini çıkar (index → movieId)
            for (int columnIndex = 1; columnIndex < columns.length; columnIndex++) {
                movieIds[columnIndex] = Integer.parseInt(columns[columnIndex].trim());
            }

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim(); // \r\n koruması
                if (line.isEmpty()) continue;

                String[] values = line.split(",");
                int      userId = Integer.parseInt(values[0].trim());
                User     user   = new User(userId);

                for (int columnIndex = 1; columnIndex < values.length && columnIndex < columns.length; columnIndex++) {
                    int rating = Integer.parseInt(values[columnIndex].trim());
                    // Sadece sıfırdan büyük puanları kaydeder
                    if (rating > 0) {
                        user.addRating(movieIds[columnIndex], rating);
                    }
                }
                userList.add(user);
            }

        } catch (IOException exception) {
            System.err.println("main_data.csv okunamadı: " + exception.getMessage());
        }
        return userList;
    }


    //movieId → Movie nesnesi
    public static Map<Integer, Movie> readMovies(String filePath) {
        Map<Integer, Movie> movieMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            reader.readLine(); // başlık satırını atla

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim(); // \r\n koruması
                if (line.isEmpty()) continue;

                int firstCommaIndex = line.indexOf(',');
                if (firstCommaIndex == -1) continue;

                String idPart   = line.substring(0, firstCommaIndex).trim();
                String restPart = line.substring(firstCommaIndex + 1);

                // Tırnaklı başlık: "Toy Story, The (1995)",Adventure|...
                String movieTitle;
                if (restPart.startsWith("\"")) {
                    int closingQuoteIndex = restPart.indexOf("\"", 1);
                    if (closingQuoteIndex == -1) continue; // bozuk satır, atla
                    movieTitle = restPart.substring(1, closingQuoteIndex);
                } else {
                    // Tırnaksız: Toy Story (1995),Adventure|...
                    int lastCommaIndex = restPart.lastIndexOf(',');
                    movieTitle = (lastCommaIndex != -1)
                            ? restPart.substring(0, lastCommaIndex).trim()
                            : restPart.trim();
                }

                int movieId = Integer.parseInt(idPart);
                movieMap.put(movieId, new Movie(movieId, movieTitle));
            }

        } catch (IOException exception) {
            System.err.println("movies.csv okunamadı: " + exception.getMessage());
        }

        return movieMap;
    }

    // target_user.csv → hedef kullanıcılar main data ile aynı mantık
    public static List<User> readTargetUsers(String filePath) {
        return readMainData(filePath);
    }
}