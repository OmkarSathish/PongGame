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
    static final int WINNING_SCORE = 2;
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

        gameThread = new Thread(this);
        gameThread.start();
    }

    public Connection getConnection() {
        return DatabaseManager.getConnection();
    }

    public void saveScoresToDatabase(int player1Score, int player2Score) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = DatabaseManager.getConnection();
            if (connection != null) {
                String sql = "INSERT INTO GamePoints.PlayerPoints (player1Score, player2Score) VALUES (?, ?)";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setInt(1, player1Score);
                preparedStatement.setInt(2, player2Score);
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
                    int player1Score = resultSet.getInt("player1Score");
                    int player2Score = resultSet.getInt("player2Score");
                    System.out.println("Player 1 Score: " + player1Score + ", Player 2 Score: " + player2Score);
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
            score.player2++;
            newPaddles();
            newBall();
            // System.out.println("Player 2: " + score.player2);
        }
        if (ball.x >= GAME_WIDTH - BALL_DIAMETER) {
            score.player1++;
            newPaddles();
            newBall();
            // System.out.println("Player 1: " + score.player1);
        }
    }

    public void checkGameOver() {
        if (score.player1 == WINNING_SCORE || score.player2 == WINNING_SCORE) {
            String winner;
            int winnerScore;
            if (score.player1 == WINNING_SCORE) {
                winner = "Player 1";
                winnerScore = score.player1;
            } else {
                winner = "Player 2";
                winnerScore = score.player2;
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
                saveScoresToDatabase(score.player1, score.player2);
                checkStoredScores();
                gameOver = false;
                score.player1 = 0;
                score.player2 = 0;
                newPaddles();
                newBall();
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

    // public void checkWinningConditions() {
    // if (score.player1 >= WINNING_SCORE || score.player1 >= WINNING_SCORE) {
    // String winner = (score.player1 >= WINNING_SCORE) ? "Player 1" : "Player 2";
    // System.out.println(winner + " wins!");
    // System.exit(0);
    // }
    // }

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
