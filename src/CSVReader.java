import java.io.*;
import java.util.*;

//Uc CSV dosyasini okuyarak Java nesnelerine donusturen yardimci siniftir
public class CSVReader {

    //main_data.csv okur
    public static List<User> readMainData(String filePath) {
        List<User> users = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String headerLine = br.readLine(); // ilk satır: "user_id,1,2,3,..."
            if (headerLine == null) return users;

            String[] headers = headerLine.split(",");
            // headers[0] = "user_id", headers[1] = "1", headers[2] = "2", ...

            // Movie ID'lerini integer'a çevir (index → movieId)
            int[] movieIds = new int[headers.length];
            for (int i = 1; i < headers.length; i++) {
                movieIds[i] = Integer.parseInt(headers[i].trim());
            }

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                int userId = Integer.parseInt(parts[0].trim());
                User user = new User(userId);

                for (int i = 1; i < parts.length && i < headers.length; i++) {
                    int rating = Integer.parseInt(parts[i].trim());
                    if (rating > 0) {
                        user.addRating(movieIds[i], rating);
                    }
                }
                users.add(user);
            }

        } catch (IOException e) {
            System.err.println("main_data.csv okunamadı: " + e.getMessage());
        }

        return users;
    }


    //movies.csv okur,  movieId → Movie map döner
    public static Map<Integer, Movie> readMovies(String filePath) {
        Map<Integer, Movie> movies = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // header satırını atla

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // Title içinde virgül olabilir (örn: "Grumpier Old Men, The")
                // Bu yüzden sadece ilk virgüle göre böl, rest = title,genres
                int firstComma = line.indexOf(',');
                if (firstComma == -1) continue;

                String idStr = line.substring(0, firstComma).trim();
                String rest = line.substring(firstComma + 1);

                // Genres'i at, sadece title lazım
                // Title tırnak içinde olabilir ya da olmayabilir
                String title;
                if (rest.startsWith("\"")) {
                    // Tırnaklı title: "Toy Story, The (1995)",Adventure|...
                    int endQuote = rest.indexOf("\"", 1);
                    title = rest.substring(1, endQuote);
                } else {
                    // Tırnaksız: Toy Story (1995),Adventure|...
                    int lastComma = rest.lastIndexOf(',');
                    title = (lastComma != -1) ? rest.substring(0, lastComma).trim() : rest.trim();
                }

                int movieId = Integer.parseInt(idStr);
                movies.put(movieId, new Movie(movieId, title));
            }

        } catch (IOException e) {
            System.err.println("movies.csv okunamadı: " + e.getMessage());
        }

        return movies;
    }


    //target_user.csv okur → 10 hedef kullanıcı format main_data.csv ile aynı
    public static List<User> readTargetUsers(String filePath) {
        // main_data.csv ile aynı format, aynı metodu kullanabiliriz
        return readMainData(filePath);
    }
}