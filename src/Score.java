import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

public class Score extends Rectangle {
    static int PANEL_WIDTH;
    static int PANEL_HEIGHT;
    int player1Score;
    int player2Score;

    Score(int PANEL_WIDTH, int PANEL_HEIGHT) {
        Score.PANEL_WIDTH = PANEL_WIDTH;
        Score.PANEL_HEIGHT = PANEL_HEIGHT;
    }

    public void draw(Graphics g) {
        g.setColor(Color.red);
        g.setFont(new Font("Consolas", Font.PLAIN, 60));
        g.drawLine(PANEL_WIDTH / 2, 0, PANEL_WIDTH / 2, PANEL_HEIGHT);
        g.drawString(String.valueOf(player1Score / 10) + String.valueOf(player1Score % 10),
                (PANEL_WIDTH / 2) - 85, 50);
        g.drawString(String.valueOf(player2Score / 10) + String.valueOf(player2Score % 10),
                (PANEL_WIDTH / 2) + 20, 50);
    }
}
