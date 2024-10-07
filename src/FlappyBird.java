import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.sound.sampled.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Random;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardwidth = 360;
    int boardheight = 640;

    // Images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // Bird
    int birdX = boardwidth / 8;
    int birdY = boardheight / 2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    int pipeX = boardwidth;
    int pipeY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // Game Logic
    Bird Bird;
    int velocityX = -4;
    int velocityY = 0;
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipesTimer;
    boolean gameOver = false;
    double score = 0;

    // Sound
    Clip backgroundMusic;
    Clip gameOverSound;
    Clip flapsound;

    FlappyBird() {
        setPreferredSize(new Dimension(boardwidth, boardheight));
        setFocusable(true);
        addKeyListener(this);

        // Load Images
        backgroundImg = new ImageIcon(getClass().getResource("./images/flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./images/flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./images/toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./images/bottompipe.png")).getImage();

        // Load Sounds
        backgroundMusic = loadSound("background_music.wav");
        gameOverSound = loadSound("game_over_sound.wav");
        flapsound = loadSound("wing-flap.wav"); 

        // bird
        Bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        placePipesTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });

        placePipesTimer.start();

        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();

        // Play background music
        playSound(backgroundMusic, true);
    }

    public void placePipes() {
        int radomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardheight / 4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = radomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = radomPipeY + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(backgroundImg, 0, 0, boardwidth, boardheight, null);
    
        Graphics2D g2d = (Graphics2D) g;
    
        // Calculate rotation angle based on bird's velocityY
        double angle = Math.toRadians(Math.max(-45, Math.min(45, -velocityY * 2)));
    
        // Rotate the bird based on the angle and draw it
        g2d.translate(Bird.x + Bird.width / 2, Bird.y + Bird.height / 2); // Move to bird's center
        g2d.rotate(angle); // Rotate the bird
        g2d.drawImage(Bird.img, -Bird.width / 2, -Bird.height / 2, Bird.width, Bird.height, null); // Draw bird at the new position
        g2d.rotate(-angle); // Reset rotation
        g2d.translate(-(Bird.x + Bird.width / 2), -(Bird.y + Bird.height / 2)); // Reset translation
    
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }
    
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        g.drawString(String.valueOf((int) score), 10, 35);
    
        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("Game Over", boardwidth / 2 - 120, boardheight / 2);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            int new_heght = (int) (boardheight/1.5);
            g.drawString("Press spacebar to continue", boardwidth / 2 - 130, new_heght);

        }
    }
    

    public void move() {
        velocityY += gravity;
        Bird.y += velocityY;
        Bird.y = Math.max(Bird.y, 0);

        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            if (!pipe.passed && Bird.x > pipe.x + pipe.width) {
                pipe.passed = true;
                score += 0.5;
            }

            if (collision(Bird, pipe)) {
                gameOver = true;
                playGameOver();
            }
        }

        if (Bird.y > boardheight) {
            gameOver = true;
            playGameOver();
        }
    }

    public boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    public Clip loadSound(String filepath) {
        try {
            File soundFile = new File(filepath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            return clip;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void playSound(Clip clip, boolean loop) {
        if (clip != null) {
            clip.setFramePosition(0); // rewind to the beginning
            if (loop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                clip.start();
            }
        }
    }

    public void stopSound(Clip clip) {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    public void playGameOver() {
        stopSound(backgroundMusic);
        playSound(gameOverSound, false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            placePipesTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -10;
            playSound(flapsound, false); //play the flapSound
            if (gameOver) {
                Bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                score = 0;
                gameOver = false;
                gameLoop.start();
                placePipesTimer.start();
                playSound(backgroundMusic, true); // restart background music

            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
