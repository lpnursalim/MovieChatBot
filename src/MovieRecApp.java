import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.swing.border.EmptyBorder;

public class MovieRecApp {
    private JFrame frame;
    private JPanel homePanel, welcomePanel, recommendationPanel, funFactsPanel;
    private JComboBox<String> moodDropdown, actionDropdown;
    private JTextArea resultArea, funFactsArea;
    private JTextField directorInput, movieInput;
    private CardLayout cardLayout;
    private Map<String, String> moodMap;

    private JButton findDirectorMoviesButton;

    private Connection connection;
    private int selectedMovieId;

    public MovieRecApp() {
        initializeMoodMap();
        initializeDatabaseConnection();
        initializeUI();
    }

    private void initializeMoodMap() {
        moodMap = new HashMap<>();
        moodMap.put("Curious", "Documentary");
        moodMap.put("Briefly Engaging", "Short");
        moodMap.put("Lighthearted", "Comedy");
        moodMap.put("Tense", "Crime");
        moodMap.put("Rugged", "Western");
        moodMap.put("Heartwarming", "Family");
        moodMap.put("Imaginative", "Animation");
        moodMap.put("Intense", "Drama");
        moodMap.put("Affectionate", "Romance");
        moodMap.put("Intrigued", "Mystery");
        moodMap.put("Edgy", "Thriller");
        moodMap.put("Provocative", "Adult");
        moodMap.put("Rhythmic", "Music");
        moodMap.put("Excited", "Action");
        moodMap.put("Wondrous", "Fantasy");
        moodMap.put("Futuristic", "Sci-Fi");
        moodMap.put("Fearful", "Horror");
        moodMap.put("Solemn", "War");
        moodMap.put("Upbeat", "Musical");
        moodMap.put("Adventurous", "Adventure");
        moodMap.put("Moody", "Film-Noir");
    }

    private void initializeDatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://db.relational-data.org:3306/imdb_ijs";
            String username = "guest";
            String password = "relational";
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Connection successful!");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeUI() {
        frame = new JFrame("Movie Recommendation System");
        cardLayout = new CardLayout();
        frame.setLayout(cardLayout);
        frame.getContentPane().setBackground(Color.decode("#a4a4cc"));

        ImageIcon originalBannerIcon = new ImageIcon("assets/film.jpeg"); // Replace with the correct path
        Image scaledImage = originalBannerIcon.getImage().getScaledInstance(500, 100, Image.SCALE_SMOOTH);
        ImageIcon bannerIcon = new ImageIcon(scaledImage);
        int margin = 20;

        // Home Panel
        homePanel = new JPanel();
        homePanel.setLayout(new BorderLayout());
        homePanel.setBackground(Color.decode("#a4a4cc"));
        homePanel.setBorder(new EmptyBorder(margin, margin, margin, margin));

        JLabel bannerLabelHome = new JLabel(bannerIcon);
        JLabel homeLabel = new JLabel("<html>Want to dig deep into the world of films?<br>Try out our chatbot!</html>", SwingConstants.CENTER);
        homeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        homeLabel.setForeground(Color.decode("#2f3d53"));
        JButton getStartedButton = new JButton("Get Started");
        getStartedButton.setFont(new Font("Arial", Font.BOLD, 14));
        getStartedButton.setBackground(Color.decode("#2f3d53"));
        getStartedButton.setForeground(Color.BLACK);
        getStartedButton.addActionListener(e -> cardLayout.show(frame.getContentPane(), "Welcome"));

        homePanel.add(bannerLabelHome, BorderLayout.NORTH);
        homePanel.add(homeLabel, BorderLayout.CENTER);
        homePanel.add(getStartedButton, BorderLayout.SOUTH);

        // Welcome Panel
        welcomePanel = new JPanel();
        welcomePanel.setLayout(new BorderLayout());
        welcomePanel.setBackground(Color.decode("#a4a4cc"));
        welcomePanel.setBorder(new EmptyBorder(margin, margin, margin, margin));

        JLabel bannerLabelWelcome = new JLabel(bannerIcon);
        JLabel welcomeLabel = new JLabel("<html>“Hello! Welcome to the movie information and <br> recommendation system. What is your mood?”</html>", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        welcomeLabel.setForeground(Color.decode("#2f3d53"));
        moodDropdown = new JComboBox<>(moodMap.keySet().toArray(new String[0]));
        moodDropdown.setFont(new Font("Arial", Font.PLAIN, 14));
        moodDropdown.insertItemAt("Pick a mood", 0);
        moodDropdown.setSelectedIndex(0);
        JButton findMoviesButton = new JButton("Find Movies");
        findMoviesButton.setFont(new Font("Arial", Font.BOLD, 14));
        findMoviesButton.setBackground(Color.decode("#2f3d53"));
        findMoviesButton.setForeground(Color.BLACK);
        findMoviesButton.addActionListener(e -> {
            executeQuery1();
            moodDropdown.setSelectedIndex(0);
        });

        JButton backButton1 = new JButton("Back");
        backButton1.setFont(new Font("Arial", Font.BOLD, 14));
        backButton1.setBackground(Color.decode("#2f3d53"));
        backButton1.setForeground(Color.BLACK);
        backButton1.addActionListener(e -> cardLayout.show(frame.getContentPane(), "Home"));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.decode("#a4a4cc"));
        topPanel.add(backButton1, BorderLayout.WEST);
        topPanel.add(welcomeLabel, BorderLayout.CENTER);

        welcomePanel.add(bannerLabelWelcome, BorderLayout.NORTH);
        welcomePanel.add(topPanel, BorderLayout.NORTH);
        welcomePanel.add(moodDropdown, BorderLayout.CENTER);
        welcomePanel.add(findMoviesButton, BorderLayout.SOUTH);

        // Recommendation Panel
        recommendationPanel = new JPanel();
        recommendationPanel.setLayout(new BorderLayout());
        recommendationPanel.setBackground(Color.decode("#a4a4cc"));
        recommendationPanel.setBorder(new EmptyBorder(margin, margin, margin, margin));

        JLabel bannerLabelRecommendation = new JLabel(bannerIcon);
        resultArea = new JTextArea(15, 30);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Arial", Font.PLAIN, 14));
        resultArea.setForeground(Color.decode("#2f3d53"));

        directorInput = new JTextField(20);
        directorInput.setFont(new Font("Arial", Font.PLAIN, 14));
        directorInput.setForeground(Color.decode("#2f3d53"));
        directorInput.setBackground(Color.WHITE);
        directorInput.setText("Enter Director's Name");

        findDirectorMoviesButton = new JButton("Find Director's Movies");
        findDirectorMoviesButton.setFont(new Font("Arial", Font.BOLD, 14));
        findDirectorMoviesButton.setBackground(Color.decode("#2f3d53"));
        findDirectorMoviesButton.setForeground(Color.BLACK);
        findDirectorMoviesButton.addActionListener(e -> {
            executeQuery3();
            directorInput.setText("");
        });

        movieInput = new JTextField(20);
        movieInput.setFont(new Font("Arial", Font.PLAIN, 14));
        movieInput.setForeground(Color.decode("#2f3d53"));
        movieInput.setBackground(Color.WHITE);
        movieInput.setVisible(false);

        actionDropdown = new JComboBox<>(new String[]{"Find Similar Movies", "Fun Facts"});
        actionDropdown.setFont(new Font("Arial", Font.PLAIN, 14));
        actionDropdown.setForeground(Color.decode("#2f3d53"));
        actionDropdown.insertItemAt("Pick an option", 0);
        actionDropdown.setSelectedIndex(0);
        actionDropdown.setVisible(false);
        actionDropdown.addActionListener(e -> {
            if (actionDropdown.getSelectedItem().equals("Find Similar Movies")) {
                executeQuery2();
            } else if (actionDropdown.getSelectedItem().equals("Fun Facts")) {
                executeFunFactsQuery();
            }
            actionDropdown.setSelectedIndex(0);
            movieInput.setText("");
        });

        JPanel directorInputPanel = new JPanel(new FlowLayout());
        directorInputPanel.setBackground(Color.decode("#a4a4cc"));
        directorInputPanel.add(directorInput);
        directorInputPanel.add(findDirectorMoviesButton);

        JPanel movieInputPanel = new JPanel(new FlowLayout());
        movieInputPanel.setBackground(Color.decode("#a4a4cc"));
        movieInputPanel.add(movieInput);
        movieInputPanel.add(actionDropdown);

        JPanel combinedInputPanel = new JPanel();
        combinedInputPanel.setLayout(new BoxLayout(combinedInputPanel, BoxLayout.Y_AXIS));
        combinedInputPanel.setBackground(Color.decode("#a4a4cc"));
        combinedInputPanel.add(directorInputPanel);
        combinedInputPanel.add(movieInputPanel);

        JButton backButton2 = new JButton("Back");
        backButton2.setFont(new Font("Arial", Font.BOLD, 14));
        backButton2.setBackground(Color.decode("#2f3d53"));
        backButton2.setForeground(Color.BLACK);
        backButton2.addActionListener(e -> {
            cardLayout.show(frame.getContentPane(), "Welcome");
            movieInput.setVisible(false);
            actionDropdown.setVisible(false);
            directorInput.setVisible(true);
            findDirectorMoviesButton.setVisible(true);
        });

        JPanel topPanelRec = new JPanel(new BorderLayout());
        topPanelRec.setBackground(Color.decode("#a4a4cc"));
        topPanelRec.add(backButton2, BorderLayout.WEST);

        recommendationPanel.add(bannerLabelRecommendation, BorderLayout.NORTH);
        recommendationPanel.add(topPanelRec, BorderLayout.NORTH);
        recommendationPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        recommendationPanel.add(combinedInputPanel, BorderLayout.SOUTH);

        // Fun Facts Panel
        funFactsPanel = new JPanel();
        funFactsPanel.setLayout(new BorderLayout());
        funFactsPanel.setBackground(Color.decode("#a4a4cc"));
        funFactsPanel.setBorder(new EmptyBorder(margin, margin, margin, margin));

        funFactsArea = new JTextArea(15, 30);
        funFactsArea.setEditable(false);
        funFactsArea.setFont(new Font("Arial", Font.PLAIN, 14));
        funFactsArea.setForeground(Color.decode("#2f3d53"));

        JButton backButtonFunFacts = new JButton("Back");
        backButtonFunFacts.setFont(new Font("Arial", Font.BOLD, 14));
        backButtonFunFacts.setBackground(Color.decode("#2f3d53"));
        backButtonFunFacts.setForeground(Color.BLACK);
        backButtonFunFacts.addActionListener(e -> cardLayout.show(frame.getContentPane(), "Recommendation"));

        JPanel topPanelFunFacts = new JPanel(new BorderLayout());
        topPanelFunFacts.setBackground(Color.decode("#a4a4cc"));
        topPanelFunFacts.add(backButtonFunFacts, BorderLayout.WEST);

        funFactsPanel.add(topPanelFunFacts, BorderLayout.NORTH);
        funFactsPanel.add(new JScrollPane(funFactsArea), BorderLayout.CENTER);

        // Adding panels to Frame
        frame.add(homePanel, "Home");
        frame.add(welcomePanel, "Welcome");
        frame.add(recommendationPanel, "Recommendation");
        frame.add(funFactsPanel, "FunFacts");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void executeQuery1() {
        if (connection == null) {
            resultArea.setText("Database connection is not available.");
            return;
        }
        String mood = (String) moodDropdown.getSelectedItem();
        String genre = moodMap.get(mood);
        if (genre == null) {
            System.out.println("Error: Genre not found for mood: " + mood);
            return;
        }
        String query = "select " +
                "count(*), top.director_fn, top.director_ln, top.actor_fn, top.actor_ln, top.genre " +
                "from (select d.first_name as director_fn, " +
                "d.last_name as director_ln, " +
                "a.first_name as actor_fn, " +
                "a.last_name as actor_ln, " +
                "g.genre " +
                "from " +
                "(select count(*), " +
                "mg.genre " +
                "from " +
                "movies m, " +
                "movies_genres mg " +
                "where " +
                "mg.movie_id = m.id " +
                "and mg.genre = ? " +
                "and m.rank > 8 " +
                "group by " +
                "mg.genre " +
                "order by count(*) desc LIMIT 3) g, " +
                "movies m, " +
                "actors a, " +
                "roles r, " +
                "movies_directors md, " +
                "directors d, " +
                "movies_genres mg " +
                "where " +
                "mg.genre = g.genre " +
                "and md.director_id = d.id " +
                "and md.movie_id = mg.movie_id " +
                "and m.id = mg.movie_id " +
                "and r.actor_id = a.id " +
                "and r.movie_id = mg.movie_id " +
                "and m.rank > 8) top " +
                "group by top.director_fn, top.director_ln, top.actor_fn, top.actor_ln, top.genre " +
                "order by count(*) desc LIMIT 10";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, genre);
            ResultSet rs = stmt.executeQuery();

            resultArea.setText("Did you know that based on your mood, these Directors/Actors \n");
            resultArea.append("made the best movies for you? Go ahead and choose a director\n" );
            resultArea.append(" to explore more about!\n");
            resultArea.append("\n");
            resultArea.append("Top Director/Actor combinations:\n");
            resultArea.append("\n");

            if (!rs.isBeforeFirst()) {
                resultArea.append("No results found for the selected mood.\n");
                return;
            }
            while (rs.next()) {
                String directorFirstName = rs.getString("director_fn");
                String directorLastName = rs.getString("director_ln");
                String actorFirstName = rs.getString("actor_fn");
                String actorLastName = rs.getString("actor_ln");
                resultArea.append("Director: " + directorFirstName + " " + directorLastName + ", Actor: " + actorFirstName + " " + actorLastName + "\n");
            }
            cardLayout.show(frame.getContentPane(), "Recommendation");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void executeQuery2() {
        if (connection == null) {
            resultArea.setText("Database connection is not available.");
            return;
        }

        String movieName = movieInput.getText().trim();
        if (movieName.isEmpty()) {
            resultArea.setText("Please enter the name of the movie you watched.");
            return;
        }

        String query = "SELECT DISTINCT " +
                "m.name AS movie_name, " +
                "d.first_name, " +
                "d.last_name, " +
                "m.rank, m.id " +
                "FROM " +
                "movies m, movies_directors md, directors d, actors a, roles r, movies_genres mg " +
                "WHERE " +
                "m.id = md.movie_id " +
                "AND md.director_id = d.id " +
                "AND m.id = r.movie_id " +
                "AND r.actor_id = a.id " +
                "AND mg.movie_id = m.id AND m.rank > 3 " +
                "AND (a.first_name, a.last_name, d.first_name, d.last_name, mg.genre) IN " +
                "(SELECT " +
                "a.first_name, a.last_name, d.first_name, d.last_name, mg.genre " +
                "FROM " +
                "movies m, actors a, directors d, movies_directors md, roles r, movies_genres mg " +
                "WHERE " +
                "d.id = md.director_id " +
                "AND md.movie_id = m.id " +
                "AND m.id = r.movie_id " +
                "AND r.actor_id = a.id " +
                "AND mg.movie_id = m.id AND " +
                "m.name = ?) " +
                "AND m.name != ? " +
                "ORDER BY m.rank " +
                "DESC LIMIT 10";



        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, movieName);
            stmt.setString(2, movieName);
            ResultSet rs = stmt.executeQuery();
            resultArea.setText("That's a great choice! Here are similar movies by the same director \n that are highly ranked:\n");
            if (!rs.isBeforeFirst()) {
                resultArea.append("No results found for the entered movie.\n");
                return;
            }
            while (rs.next()) {
                String similarMovieName = rs.getString("movie_name");
                float rank = rs.getFloat("rank");
                selectedMovieId = rs.getInt("id");
                resultArea.append("Movie: " + similarMovieName + " (Rank: " + rank + ")\n");
            }

            movieInput.setVisible(true);
            actionDropdown.setVisible(true);
            recommendationPanel.revalidate();
            recommendationPanel.repaint();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void executeQuery3() {
        if (connection == null) {
            resultArea.setText("Database connection is not available.");
            return;
        }
        String directorName = directorInput.getText().trim();
        String[] directorNames = directorName.split(" ");
        if (directorNames.length != 2) {
            resultArea.setText("Please enter both first and last name of the director.");
            return;
        }
        String directorFirstName = directorNames[0];
        String directorLastName = directorNames[1];
        String query = "(" +
                "SELECT DISTINCT " +
                "m.name AS movie_name, " +
                "d.first_name AS director_fn, " +
                "d.last_name AS director_ln, " +
                "m.rank AS ranked " +
                "FROM " +
                "movies m, " +
                "movies_directors md, " +
                "directors d, " +
                "movies_genres mg " +
                "WHERE " +
                "m.id = md.movie_id " +
                "AND md.director_id = d.id " +
                "AND mg.movie_id = m.id " +
                "AND m.rank > 5 " +
                "AND d.first_name = ? " +
                "AND d.last_name = ? " +
                "ORDER BY " +
                "m.rank DESC " +
                "LIMIT 3 " +
                ") " +
                "UNION ALL " +
                "(" +
                "SELECT DISTINCT " +
                "m.name AS movie_name, " +
                "d.first_name AS director_fn, " +
                "d.last_name AS director_ln, " +
                "m.rank AS ranked " +
                "FROM " +
                "movies m, " +
                "movies_directors md, " +
                "directors d, " +
                "movies_genres mg " +
                "WHERE " +
                "m.id = md.movie_id " +
                "AND md.director_id = d.id " +
                "AND mg.movie_id = m.id " +
                "AND m.rank < 5 " +
                "AND d.first_name = ? " +
                "AND d.last_name = ? " +
                "ORDER BY " +
                "m.rank DESC " +
                "LIMIT 3 " +
                ")";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, directorFirstName);
            stmt.setString(2, directorLastName);
            stmt.setString(3, directorFirstName);
            stmt.setString(4, directorLastName);
            ResultSet rs = stmt.executeQuery();
            resultArea.setText("Here are the 3 Best and 3 Worst movies\n");
            resultArea.append("made by this director (If they even have a bad one!)\n");
            resultArea.append("\n");
            resultArea.append("Top 3 and Worst 3 Movies by " + directorName + ":\n");
            resultArea.append("\n");

            if (!rs.isBeforeFirst()) {
                resultArea.append("No results found for the entered director.\n");
                return;
            }
            while (rs.next()) {
                String movieName = rs.getString("movie_name");
                float rank = rs.getFloat("ranked");
                resultArea.append("Movie: " + movieName + " (Rank: " + rank + ")\n");
            }
            resultArea.append("\nOnce you pick and finish watching a movie, let me know how you liked\n");
            resultArea.append("it! I can recommend movies similar to it based on characteristics\n");
            resultArea.append("you liked or you can learn more. What movie did you end up watching?\n");
            movieInput.setVisible(true);
            actionDropdown.setVisible(true);
            directorInput.setVisible(false);
            findDirectorMoviesButton.setVisible(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void executeFunFactsQuery() {
        if (connection == null) {
            resultArea.setText("Database connection is not available.");
            return;
        }

        String movieName = movieInput.getText().trim();
        if (movieName.isEmpty()) {
            resultArea.setText("Please enter the name of the movie.");
            return;
        }

        String query41 = "SELECT first_name, last_name, role FROM roles, actors WHERE roles.movie_id = ? AND actors.id = roles.actor_id ORDER BY RAND() LIMIT 1;";
        String query42 = "SELECT CONCAT(a.first_name, ' ', a.last_name) AS fullname, COUNT(mr.movie_id) AS total_roles FROM roles mr JOIN actors a ON mr.actor_id = a.id WHERE a.id IN (SELECT DISTINCT mr.actor_id FROM roles mr WHERE mr.movie_id = ?) GROUP BY fullname ORDER BY total_roles DESC LIMIT 1;";
        String query43 = "WITH specified_movie_genres AS (SELECT mg.genre FROM movies_genres mg JOIN movies m ON mg.movie_id = m.id WHERE m.id = ?) SELECT COUNT(DISTINCT m.id) AS total_count FROM movies m JOIN movies_genres mg ON m.id = mg.movie_id WHERE NOT EXISTS (SELECT 1 FROM movies_genres mg2 WHERE mg2.movie_id = m.id AND mg2.genre NOT IN (SELECT genre FROM specified_movie_genres)) AND m.id != ?;";
        String query44 = "SELECT ROUND((SELECT COUNT(*) FROM movies WHERE `rank` < (SELECT `rank` FROM movies WHERE id = ?)) / (SELECT COUNT(*) FROM movies WHERE `rank` IS NOT NULL) * 100, 2) AS c;";
        String query45 = "SELECT ROUND((SELECT COUNT(*) * 100 from actors, roles WHERE roles.movie_id = ? AND gender = 'F' AND actors.id = roles.actor_id) / (SELECT COUNT(*) FROM actors, roles WHERE roles.movie_id = ? AND actors.id = roles.actor_id),2) AS pct;";
        try {
            resultArea.setText("Fun Facts about " + movieName + "\n");
            // Execute query41
            try (PreparedStatement stmt = connection.prepareStatement(query41)) {
                stmt.setInt(1, selectedMovieId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String fname = rs.getString("first_name");
                    String lname = rs.getString("last_name");
                    String role = rs.getString("role");
                    resultArea.append("- " + fname + " " + lname + " stars as " + role + ".\n");
                }
            }
            // Execute query42
            try (PreparedStatement stmt = connection.prepareStatement(query42)) {
                stmt.setInt(1, selectedMovieId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String fullname = rs.getString("fullname");
                    int totalRoles = rs.getInt("total_roles") - 1;
                    resultArea.append("- " + fullname + " is the most prolific actor in the movie, starring in \n " + totalRoles + " other movies.\n");
                }
            }
            // Execute query43
            try (PreparedStatement stmt = connection.prepareStatement(query43)) {
                stmt.setInt(1, selectedMovieId);
                stmt.setInt(2, selectedMovieId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int totalCount = rs.getInt("total_count");
                    resultArea.append("- There are " + totalCount + " movies in the database with the same combination \n  of genres. So many possibilities!\n");
                }
            }
            // Execute query44
            try (PreparedStatement stmt = connection.prepareStatement(query44)) {
                stmt.setInt(1, selectedMovieId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int c = rs.getInt("c");
                    if (c >= 65) {
                        resultArea.append("- This movie is rated higher than " + c + "% of movies. It must be pretty good!\n");
                    } else if (c >= 40) {
                        resultArea.append("- This movie is rated higher than " + c + "% of movies. Meh.\n");
                    } else {
                        resultArea.append("- This movie is rated higher than " + c + "% of movies. Oof.\n");
                    }
                }
            }
            // Execute query45
            try (PreparedStatement stmt = connection.prepareStatement(query45)) {
                stmt.setInt(1, selectedMovieId);
                stmt.setInt(2, selectedMovieId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int pct = rs.getInt("pct");
                    resultArea.append("- " + pct + "% of stars in this movie are female.\n");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MovieRecApp::new);
    }
}
