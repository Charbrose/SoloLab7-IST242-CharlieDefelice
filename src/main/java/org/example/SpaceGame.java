package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;


public class SpaceGame extends JFrame implements KeyListener
{
    //game constants
    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;
    private static final int PLAYER_WIDTH = 50;
    private static final int PLAYER_HEIGHT = 50;
    private static final int OBSTACLE_WIDTH = 20;
    private static final int OBSTACLE_HEIGHT = 20;
    private static final int PROJECTILE_WIDTH = 5;
    private static final int PROJECTILE_HEIGHT = 10;
    private static final int PLAYER_SPEED = 5;
    private static final int OBSTACLE_SPEED = 3;
    private static final int PROJECTILE_SPEED = 10;
    private static final int POWER_UP_SIZE = 10;

    //game variables
    private int score = 0;
    private int lives = 3;
    private double obstacleSpawnProbability = 0.02;  //normal mode spawn rate

    //swing components
    private JPanel gamePanel;
    private JLabel scoreLabel;
    private JLabel livesLabel;
    private JLabel countdownLabel;
    private Timer gameTimer;
    private Timer countdownTimer;
    private boolean isGameOver;
    private int countdown = 5; // Countdown from 5 seconds
    private int playerX, playerY;
    private int projectileX, projectileY;
    private boolean isProjectileVisible;
    private boolean isFiring;
    private boolean shieldActive = false;
    private List<Point> obstacles;
    private List<Point> powerUps;
    private Random random;
    private Image spaceshipImage;
    private Image obstacleImage;
    private Image shieldImage;

    public SpaceGame()
    {
        //sets up the frame
        setTitle("Space Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        //loads images for the game
        spaceshipImage = new ImageIcon("src/main/resources/spaceship.png").getImage();
        shieldImage = new ImageIcon("src/main/resources/shield.png").getImage();
        obstacleImage = new ImageIcon("src/main/resources/obstacles.png").getImage();

        //sets up the main game panel where everything will be drawn
        gamePanel = new JPanel()
        {
            @Override
            protected void paintComponent(Graphics g)
            {
                super.paintComponent(g);
                draw(g);
            }
        };

        //sets up UI to display score and lives
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setForeground(Color.BLUE);  //sets score color to blue
        scoreLabel.setBounds(10, 10, 100, 20);  //positions the score label
        gamePanel.add(scoreLabel);  //adds the score to the game panel

        livesLabel = new JLabel("Lives: " + lives);
        livesLabel.setForeground(Color.RED);  //sets lives color to red
        livesLabel.setBounds(10, 30, 100, 20);  //positions the lives label
        gamePanel.add(livesLabel);  //adds the lives label to the game panel

        countdownLabel = new JLabel("Starting in: 5", SwingConstants.CENTER);
        countdownLabel.setFont(new Font("Arial", Font.BOLD, 24));
        countdownLabel.setForeground(Color.WHITE);
        countdownLabel.setBounds(WIDTH / 2 - 100, HEIGHT / 2 - 50, 200, 50);
        gamePanel.add(countdownLabel); //adds the countdown label to the game panel
        countdownLabel.setVisible(true);

        add(gamePanel); //adds the game panel to the JFrame
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(this);

        //game variables and player position
        playerX = WIDTH / 2 - PLAYER_WIDTH / 2;  //positions the player in the middle
        playerY = HEIGHT - PLAYER_HEIGHT - 20;  //positions the player near the bottom
        projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;  //centers the projectiles coming from the player
        projectileY = playerY - PROJECTILE_HEIGHT;  //makes the projectile just above the player
        isProjectileVisible = false;
        isGameOver = false;
        isFiring = false;
        obstacles = new ArrayList<>();  //list of obstacles
        powerUps = new ArrayList<>();  //list of power ups
        random = new Random();  //random number generator for misc. game elements

        chooseDifficulty();  //allows player to choose the game difficulty
        startCountdown();  //starts the countdown before the game begins
    }

    //prompts user to choose the difficulty of the game
    private void chooseDifficulty()
    {
        String[] options = {"Normal", "Challenge"};
        int response = JOptionPane.showOptionDialog(this, "Choose the difficulty level:", "Difficulty Selection",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (response == 1)
        {
            obstacleSpawnProbability = 0.05;  //increased spawn rate for challenge mode
        }
    }

    //starts a countdown before the game begins
    private void startCountdown()
    {
        countdownTimer = new Timer(1000, e -> {
            countdown--;
            countdownLabel.setText("Starting in: " + countdown); //updates the label showing the countdown
            if (countdown <= 0) //checks if the countdown has reached zero
            {
                countdownLabel.setVisible(false);
                ((Timer) e.getSource()).stop();
                startGame(); //starts the game after the countdown finishes
            }
        });
        countdownTimer.start();
    }

    //starts the game by setting up a timer to refresh game state and graphics
    private void startGame()
    {
        gameTimer = new Timer(20, e -> {
            if (!isGameOver) //checks if the game is still running
            {
                update();
                gamePanel.repaint();
            }
        });
        gameTimer.start();
    }

    //draws all the game elements on the screen
    private void draw(Graphics g)
    {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        //draws the stars randomly on the background
        int stars = 50;
        for (int i = 0; i < stars; i++)
        {
            int starX = random.nextInt(WIDTH);
            int starY = random.nextInt(HEIGHT);
            Color starColor = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat());
            g.setColor(starColor);
            g.fillOval(starX, starY, 2, 2);
        }

        //draws the player's spaceship with or without the shield
        if (shieldActive)
        {
            g.drawImage(shieldImage, playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT, this);
        }

        else
        {
            g.drawImage(spaceshipImage, playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT, this);
        }

        //draws the projectile if it's visible
        if (isProjectileVisible)
        {
            g.setColor(Color.GREEN);
            g.fillRect(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        }

        //draws all obstacles on the screen
        for (Point obstacle : obstacles)
        {
            g.drawImage(obstacleImage, obstacle.x, obstacle.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT, this);
        }

        //draws the power ups as yellow ovals
        g.setColor(Color.YELLOW);
        for (Point powerUp : powerUps)
        {
            g.fillOval(powerUp.x, powerUp.y, POWER_UP_SIZE, POWER_UP_SIZE);
        }

        //displays the game over message
        if (isGameOver)
        {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Game Over!", WIDTH / 2 - 80, HEIGHT / 2);
        }
    }

    //updates game state: moving obstacles and projectiles, checking collisions, and spawning other game elements
    private void update()
    {
        for (int i = 0; i < obstacles.size(); i++)
        {
            Point obstacle = obstacles.get(i);
            obstacle.y += OBSTACLE_SPEED;
            if (obstacle.y > HEIGHT)
            {
                obstacles.remove(i);
                i--;
            }
        }

        //handles projectile movements and collisions with obstacles
        if (isProjectileVisible)
        {
            projectileY -= PROJECTILE_SPEED;
            if (projectileY < 0)
            {
                isProjectileVisible = false;  //hides projectile if it moves off the screen
            }

            else
            {
                Rectangle projectileRect = new Rectangle(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
                for (int i = 0; i < obstacles.size(); i++)
                {
                    Point obs = obstacles.get(i);
                    Rectangle obstacleRect = new Rectangle(obs.x, obs.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
                    if (projectileRect.intersects(obstacleRect))
                    {
                        obstacles.remove(i);
                        isProjectileVisible = false;
                        score += 10;  //gives score when hitting an obstacle
                        scoreLabel.setText("Score: " + score);
                        playCollisionSound();  //plays collision sound
                        break;
                    }
                }
            }
        }

        //moves power ups downward and then remove them if they move off the screen
        for (int i = 0; i < powerUps.size(); i++)
        {
            Point powerUp = powerUps.get(i);
            powerUp.y += OBSTACLE_SPEED;
            if (powerUp.y > HEIGHT)
            {
                powerUps.remove(i);
                i--;
            }
        }

        //randomly spawns new obstacles and power ups
        if (Math.random() < 0.02)
        {
            int x = random.nextInt(WIDTH - OBSTACLE_WIDTH);
            obstacles.add(new Point(x, 0));
            if (Math.random() < 0.3)
            {
                powerUps.add(new Point(x, 0));
            }
        }

        Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
        for (int i = 0; i < powerUps.size(); i++)
        {
            Point p = powerUps.get(i);
            Rectangle powerUpRect = new Rectangle(p.x, p.y, POWER_UP_SIZE, POWER_UP_SIZE);
            if (playerRect.intersects(powerUpRect))
            {
                lives += 1; //gives lives when collecting a power up
                livesLabel.setText("Lives: " + lives);
                powerUps.remove(i);
                i--;
            }
        }

        //handles collisions between the player and obstacles
        for (int i = 0; i < obstacles.size(); i++)
        {
            Point obs = obstacles.get(i);
            Rectangle obstacleRect = new Rectangle(obs.x, obs.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
            if (playerRect.intersects(obstacleRect) && !shieldActive)
            {
                lives--;
                livesLabel.setText("Lives: " + lives);
                obstacles.remove(i);
                playCollisionSound();
                break;
            }
        }

        if (lives <= 0)  //ends the game if lives run out
        {
            isGameOver = true;
        }
    }

    //handles the keyboard inputs for controlling the player
    @Override
    public void keyPressed(KeyEvent e)
    {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_LEFT && playerX > 0)
        {
            playerX -= PLAYER_SPEED; //moves player to the left
        }

        else if (keyCode == KeyEvent.VK_RIGHT && playerX < WIDTH - PLAYER_WIDTH)
        {
            playerX += PLAYER_SPEED; //moves player to the right
        }

        else if (keyCode == KeyEvent.VK_SPACE && !isFiring)
        {
            isFiring = true;
            projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
            projectileY = playerY;
            isProjectileVisible = true;
            playFireSound();
            new Thread(() -> {
                try
                {
                    Thread.sleep(500); // Delay to manage firing rate
                    isFiring = false;
                }

                catch (InterruptedException ex)
                {
                    ex.printStackTrace();
                }
            }).start();
        }


        else if (keyCode == KeyEvent.VK_S) // Press 'S' to activate the shield
        {
            activateShield();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    //this method handles all audio playback
    private void playSound(String filePath)
    {
        try
        {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        }

        catch (UnsupportedAudioFileException | IOException | LineUnavailableException e)
        {
            e.printStackTrace();
        }
    }

    //plays fire sound
    private void playFireSound()
    {
        playSound("src/main/resources/fire.wav");
    }

    //plays collision sound
    private void playCollisionSound()
    {
        playSound("src/main/resources/collision.wav");
    }


    //turns on a shield that stops damage
    private void activateShield()
    {
        shieldActive = true;
        Timer shieldTimer = new Timer(5000, e -> {     //sets a timer to deactivate the shield after 5 seconds
            shieldActive = false; //deactivates the shield
            ((Timer) e.getSource()).stop();
        });
        shieldTimer.start();
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> new SpaceGame().setVisible(true));
    }
}
