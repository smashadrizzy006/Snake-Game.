import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;

public class EnhancedSnakeGame extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;

    private final int SCALE = 20;
    private final int WIDTH = 30;
    private final int HEIGHT = 20;
    private final int INITIAL_DELAY = 200;
    private final int MIN_DELAY = 50;

    private int[] x = new int[WIDTH * HEIGHT];
    private int[] y = new int[WIDTH * HEIGHT];
    private int dots;
    private int apple_x;
    private int apple_y;
    private int score;
    private int level;
    private int delay;
    private boolean inGame = true;
    private Timer timer;

    private boolean leftDirection = false;
    private boolean rightDirection = true;
    private boolean upDirection = false;
    private boolean downDirection = false;

    private List<Color> wallColors;
    private Color snakeColor = Color.green;
    private Color appleColor = Color.red;

    private BufferedImage appleImage;

    public EnhancedSnakeGame() {
        loadAppleImage();
        initGame();
    }

    private void loadAppleImage() {
        try {
            appleImage = ImageIO.read(new File("apple.png")); // Replace with your image path
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initGame() {
        setBackground(Color.black);
        setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        setFocusable(true);
        setWallColors();

        addKeyListener(new TAdapter());
        startGame();
    }

    private void setWallColors() {
        wallColors = new ArrayList<>();
        wallColors.add(Color.white);
        wallColors.add(Color.blue);
        wallColors.add(Color.orange);
        wallColors.add(Color.magenta);
    }

    private void startGame() {
        dots = 3;
        score = 0;
        level = 1;
        delay = INITIAL_DELAY;

        for (int i = 0; i < dots; i++) {
            x[i] = 60 - i * SCALE;
            y[i] = 60;
        }

        locateApple();

        inGame = true;
        timer = new Timer(delay, this);
        timer.start();
    }

    private void locateApple() {
        int r = (int) (Math.random() * WIDTH);
        apple_x = r * SCALE;

        r = (int) (Math.random() * HEIGHT);
        apple_y = r * SCALE;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }

    private void doDrawing(Graphics g) {
        if (inGame) {
            drawWalls(g);
            drawApple(g);
            drawSnake(g);
        } else {
            gameOver(g);
        }

        Toolkit.getDefaultToolkit().sync();
    }

    private void drawWalls(Graphics g) {
        g.setColor(wallColors.get(level - 1));
        g.fillRect(0, 0, WIDTH * SCALE, SCALE);  // Top wall
        g.fillRect(0, HEIGHT * SCALE - SCALE, WIDTH * SCALE, SCALE);  // Bottom wall
        g.fillRect(0, 0, SCALE, HEIGHT * SCALE);  // Left wall
        g.fillRect(WIDTH * SCALE - SCALE, 0, SCALE, HEIGHT * SCALE);  // Right wall
    }

    private void drawSnake(Graphics g) {
        for (int i = 0; i < dots; i++) {
            if (i == 0) {
                g.setColor(snakeColor);
                g.fillRect(x[i], y[i], SCALE, SCALE);
            } else {
                g.setColor(new Color(45, 180, 0));
                g.fillRect(x[i], y[i], SCALE, SCALE);
            }
        }
    }

    private void drawApple(Graphics g) {
        g.drawImage(appleImage, apple_x, apple_y, SCALE, SCALE, this);
    }

    private void gameOver(Graphics g) {
        String message = "Game Over";
        Font font = new Font("Helvetica", Font.BOLD, 24);
        FontMetrics metrics = getFontMetrics(font);

        g.setColor(Color.white);
        g.setFont(font);
        g.drawString(message, (WIDTH * SCALE - metrics.stringWidth(message)) / 2, HEIGHT * SCALE / 2);
        g.drawString("Score: " + score, (WIDTH * SCALE - metrics.stringWidth("Score: " + score)) / 2, HEIGHT * SCALE / 2 + 30);
    }

    private void checkApple() {
        if (x[0] == apple_x && y[0] == apple_y) {
            dots++;
            score += 10;
            locateApple();

            if (score % 50 == 0) {
                level++;
                decreaseDelay();
                timer.setDelay(delay);
                playLevelUpSound();
            }
        }
    }

    private void decreaseDelay() {
        if (delay > MIN_DELAY) {
            delay -= 10;
        }
    }

    private void playLevelUpSound() {
        try {
            File soundFile = new File("levelup.wav");  // Replace with your sound file path
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void move() {
        for (int i = dots; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        if (leftDirection) {
            x[0] -= SCALE;
        }
        if (rightDirection) {
            x[0] += SCALE;
        }
        if (upDirection) {
            y[0] -= SCALE;
        }
        if (downDirection) {
            y[0] += SCALE;
        }
    }

    private void checkCollision() {
        for (int i = dots; i > 0; i--) {
            if (i > 4 && x[0] == x[i] && y[0] == y[i]) {
                inGame = false;
            }
        }

        if (y[0] >= HEIGHT * SCALE || y[0] < 0 || x[0] >= WIDTH * SCALE || x[0] < 0) {
            inGame = false;
        }

        if (!inGame) {
            timer.stop();
            playGameOverSound();
        }
    }

    private void playGameOverSound() {
        try {
            File soundFile = new File("gameover.wav");  // Replace with your sound file path
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (inGame) {
            checkApple();
            checkCollision();
            move();
        }

        repaint();
    }

    private class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if ((key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) && (!rightDirection)) {
                leftDirection = true;
                upDirection = false;
                downDirection = false;
            }
            if ((key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) && (!leftDirection)) {
                rightDirection = true;
                upDirection = false;
                downDirection = false;
            }
            if ((key == KeyEvent.VK_UP || key == KeyEvent.VK_W) && (!downDirection)) {
                upDirection = true;
                rightDirection = false;
                leftDirection = false;
            }
            if ((key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) && (!upDirection)) {
                downDirection = true;
                rightDirection = false;
                leftDirection = false;
            }
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            JFrame frame = new JFrame("Enhanced Snake Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.add(new EnhancedSnakeGame(), BorderLayout.CENTER);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
