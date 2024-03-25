import java.util.*;
import javax.swing.*;
import java.sql.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel implements Runnable {
    static final int GAME_WIDTH = 1000;
    static final int GAME_HEIGHT = (int) (GAME_WIDTH * (0.5555));
    static final Dimension SCREEN_SIZE = new Dimension(GAME_WIDTH, GAME_HEIGHT);
    static final int BALL_DIAMETER = 20;
    static final int PADDLE_WIDTH = 25;
    static final int PADDLE_HEIGHT = 100;
    static final int WINNING_SCORE = 1;
    String PLAYER_ONE_NAME;
    String PLAYER_TWO_NAME;
    Thread gameThread;
    Image image;
    Graphics graphics;
    Random random;
    Paddle paddle1;
    Paddle paddle2;
    Ball ball;
    Score score;
    boolean gameOver = false;

    GamePanel() {
        newPaddles();
        newBall();
        score = new Score(GAME_WIDTH, GAME_HEIGHT);
        this.setFocusable(true);
        this.addKeyListener(new AL());
        this.setPreferredSize(SCREEN_SIZE);
        promptPlayerNames();

        gameThread = new Thread(this);
        gameThread.start();
    }

    public void promptPlayerNames() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));
        JLabel player1Label = new JLabel("Player 1 Name:");
        JTextField player1TextField = new JTextField();
        JLabel player2Label = new JLabel("Player 2 Name:");
        JTextField player2TextField = new JTextField();
        panel.add(player1Label);
        panel.add(player1TextField);
        panel.add(player2Label);
        panel.add(player2TextField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Enter Player Names",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            PLAYER_ONE_NAME = player1TextField.getText();
            PLAYER_TWO_NAME = player2TextField.getText();
        } else {
            PLAYER_ONE_NAME = "Player 1";
            PLAYER_TWO_NAME = "Player 2";
        }
    }

    public Connection getConnection() {
        return DatabaseManager.getConnection();
    }

    public void saveScoresToDatabase(String playerOneName, int playerOneScore, String playerTwoName,
            int playerTwoScore) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = DatabaseManager.getConnection();
            if (connection != null) {
                String sql = "INSERT INTO GamePoints.PlayerPoints (playerOneName, playerTwoName, playerOneScore, playerTwoScore) VALUES (?, ?, ?, ?)";
                preparedStatement = connection.prepareStatement(sql);
                if (playerOneName.length() != 0) {
                    preparedStatement.setString(1, playerOneName);
                } else {
                    preparedStatement.setString(1, "playerOneName");
                }
                if (playerTwoName.length() != 0) {
                    preparedStatement.setString(2, playerTwoName);
                } else {
                    preparedStatement.setString(2, "playerTwoName");
                }
                preparedStatement.setInt(3, playerOneScore);
                preparedStatement.setInt(4, playerTwoScore);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseManager.close(connection, preparedStatement, null);
        }
    }

    public void checkStoredScores() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseManager.getConnection();
            if (connection != null) {
                String sql = "SELECT * FROM GamePoints.PlayerPoints";
                preparedStatement = connection.prepareStatement(sql);
                resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    int playerOneScore = resultSet.getInt("playerOneScore");
                    int playerTwoScore = resultSet.getInt("playerTwoScore");
                    String playerOneName = resultSet.getString("playerOneName");
                    String playerTwoName = resultSet.getString("playerTwoName");
                    if (playerOneName.length() == 0) {
                        playerOneName = "Player1";
                    }
                    if (playerTwoName.length() == 0) {
                        playerTwoName = "Player2";
                    }
                    System.out.printf("%s: %d | %s: %d\n", playerOneName, playerOneScore, playerTwoName,
                            playerTwoScore);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseManager.close(connection, preparedStatement, resultSet);
        }
    }

    public void newBall() {
        random = new Random();
        ball = new Ball((GAME_WIDTH / 2) - (BALL_DIAMETER / 2), random.nextInt(GAME_HEIGHT - BALL_DIAMETER),
                BALL_DIAMETER, BALL_DIAMETER);
    }

    public void newPaddles() {
        paddle1 = new Paddle(0, (GAME_HEIGHT / 2) - (PADDLE_HEIGHT / 2), PADDLE_WIDTH, PADDLE_HEIGHT, 1);
        paddle2 = new Paddle(GAME_WIDTH - PADDLE_WIDTH, (GAME_HEIGHT / 2) - (PADDLE_HEIGHT / 2), PADDLE_WIDTH,
                PADDLE_HEIGHT, 2);
    }

    public void paint(Graphics g) {
        image = createImage(getWidth(), getHeight());
        graphics = image.getGraphics();
        draw(graphics);
        g.drawImage(image, 0, 0, this);
    }

    public void draw(Graphics g) {
        paddle1.draw(g);
        paddle2.draw(g);
        ball.draw(g);
        score.draw(g);
        Toolkit.getDefaultToolkit().sync();
    }

    public void move() {
        paddle1.move();
        paddle2.move();
        ball.move();
    }

    public void checkCollision() {
        if (ball.y <= 0) {
            ball.setYDirection(-ball.yVelocity);
        }
        if (ball.y >= GAME_HEIGHT - BALL_DIAMETER) {
            ball.setYDirection(-ball.yVelocity);
        }
        if (ball.intersects(paddle1)) {
            ball.xVelocity = Math.abs(ball.xVelocity);
            ball.xVelocity++;
            if (ball.yVelocity > 0)
                ball.yVelocity++;
            else
                ball.yVelocity--;
            ball.setXDirection(ball.xVelocity);
            ball.setYDirection(ball.yVelocity);
        }
        if (ball.intersects(paddle2)) {
            ball.xVelocity = Math.abs(ball.xVelocity);
            ball.xVelocity++;
            if (ball.yVelocity > 0)
                ball.yVelocity++;
            else
                ball.yVelocity--;
            ball.setXDirection(-ball.xVelocity);
            ball.setYDirection(ball.yVelocity);
        }
        if (paddle1.y <= 0)
            paddle1.y = 0;
        if (paddle1.y >= (GAME_HEIGHT - PADDLE_HEIGHT))
            paddle1.y = GAME_HEIGHT - PADDLE_HEIGHT;
        if (paddle2.y <= 0)
            paddle2.y = 0;
        if (paddle2.y >= (GAME_HEIGHT - PADDLE_HEIGHT))
            paddle2.y = GAME_HEIGHT - PADDLE_HEIGHT;

        if (ball.x <= 0) {
            score.player2Score++;
            newPaddles();
            newBall();
            // System.out.println("Player 2: " + score.player2);
        }
        if (ball.x >= GAME_WIDTH - BALL_DIAMETER) {
            score.player1Score++;
            newPaddles();
            newBall();
            // System.out.println("Player 1: " + score.player1);
        }
    }

    public void checkGameOver() {
        if (score.player1Score == WINNING_SCORE || score.player2Score == WINNING_SCORE) {
            String winner;
            int winnerScore;
            if (score.player1Score == WINNING_SCORE) {
                winner = "Player 1";
                winnerScore = score.player1Score;
            } else {
                winner = "Player 2";
                winnerScore = score.player2Score;
            }
            gameOver = true;

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            JLabel label = new JLabel(winner + " wins with " + winnerScore + " points!");
            panel.add(label);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JDialog dialog = new JDialog();
            JButton restartButton = new JButton("Restart Game");
            restartButton.addActionListener(e -> {
                saveScoresToDatabase(PLAYER_ONE_NAME, score.player1Score, PLAYER_TWO_NAME, score.player2Score);
                checkStoredScores();
                gameOver = false;
                score.player1Score = 0;
                score.player2Score = 0;
                newPaddles();
                newBall();
                promptPlayerNames();
                gameThread = new Thread(this);
                gameThread.start();
                dialog.dispose();
            });
            buttonPanel.add(restartButton);
            panel.add(buttonPanel);

            dialog.setContentPane(panel);
            dialog.setSize(200, 150);
            dialog.setLocationRelativeTo(this);
            dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        }
    }

    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        while (!gameOver) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            if (delta >= 1) {
                move();
                checkCollision();
                checkGameOver();
                repaint();
                delta--;
            }
        }
    }

    public class AL extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            paddle1.keyPressed(e);
            paddle2.keyPressed(e);
        }

        public void keyReleased(KeyEvent e) {
            paddle1.keyReleased(e);
            paddle2.keyReleased(e);
        }
    }
}
