package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

// Main class of the game extending JFrame to use Swing components
public class ShapeMatcherGame extends JFrame
{
    private Shape currentShape; // Holds the currently displayed shape
    private String currentShapeType; // String description of the current shape
    private JLabel statusLabel; // Label to display messages like "Correct!" or "Try again!"
    private JLabel scoreLabel; // Label to display the current score
    private JLabel timerLabel; // Label to display the countdown timer
    private JLabel livesLabel; // Label to display the number of remaining lives
    private GamePanel gamePanel; // Custom panel for drawing shapes
    private Timer timer; // Timer for countdown in each round
    private int score = 0; // Variable to keep track of the player's score
    private int lives; // Variable to keep track of the player's remaining lives
    private int timeLeft; // Variable to keep track of the time left for answering

    // Constructor to set up the game environment
    public ShapeMatcherGame()
    {
        selectDifficulty(); // Prompts the user to select the difficulty level of the game

        this.setTitle("Shape Matching Game"); // Sets the window title
        this.setSize(800, 600); // Sets the window size
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Ensures the program exits when the window is closed
        this.setLayout(new BorderLayout()); // Uses BorderLayout for arranging panels

        gamePanel = new GamePanel();
        this.add(gamePanel, BorderLayout.CENTER); // Adds the panel for drawing shapes to the center

        JPanel buttonPanel = new JPanel(); // Panel for holding shape buttons
        String[] shapes = {"Rectangle", "Circle", "Square", "Trapezoid"};
        for (String shape : shapes)
        {
            JButton button = new JButton(shape);
            button.addActionListener(this::shapeButtonClicked); // Adds action listener for button clicks
            buttonPanel.add(button); // Adds each button to the panel
        }
        this.add(buttonPanel, BorderLayout.SOUTH); // Adds the button panel to the bottom of the layout

        JPanel infoPanel = new JPanel(new GridLayout(1, 4)); // Panel for displaying game information
        statusLabel = new JLabel("Choose the correct shape.", SwingConstants.CENTER);
        scoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        timerLabel = new JLabel("Time left: " + timeLeft + "s", SwingConstants.CENTER);
        livesLabel = new JLabel("Lives: " + lives, SwingConstants.CENTER);

        // Adds labels to the info panel
        infoPanel.add(statusLabel);
        infoPanel.add(scoreLabel);
        infoPanel.add(timerLabel);
        infoPanel.add(livesLabel);

        this.add(infoPanel, BorderLayout.NORTH); // Adds the info panel to the top of the layout

        setupTimer(); // Initializes and starts the game timer
        this.setVisible(true); // Makes the window visible
    }

    // Method for selecting the game difficulty through a dialog box
    private void selectDifficulty()
    {
        String[] options = {"Easy", "Normal", "Hard"};
        int response = JOptionPane.showOptionDialog(null, "Select Difficulty", "Difficulty Selection",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);
        switch (response)
        {
            case 0: // Easy difficulty
                timeLeft = 10; // More time per question
                lives = 5; // More lives for a forgiving gameplay experience
                break;
            case 2: // Hard difficulty
                timeLeft = 3; // Less time per question for more challenge
                lives = 2; // Fewer lives for increased difficulty
                break;
            case 1: // Normal difficulty
            default:
                timeLeft = 5; // Standard time per question
                lives = 3; // Standard number of lives
                break;
        }
    }

    // Method to handle shape button clicks to identify the shape
    private void shapeButtonClicked(ActionEvent e)
    {
        if (!timer.isRunning()) return; // Ignore clicks if the game is over

        String selectedShape = ((JButton) e.getSource()).getText();
        if (selectedShape.equals(currentShapeType))
        {
            score++;
            scoreLabel.setText("Score: " + score);
            statusLabel.setText("Correct! It's a " + currentShapeType + "!");
            resetTimer();
            gamePanel.drawRandomShape();
        }

        else
        {
            lives--;
            livesLabel.setText("Lives: " + lives);
            statusLabel.setText("Incorrect, try again!");
            if (lives <= 0)
            {
                gameOver("No lives left! Game Over!");
            }
        }
    }

    // Sets up the game timer with a 1-second delay between ticks
    private void setupTimer()
    {
        timer = new Timer(1000, e -> {
            timeLeft--;
            timerLabel.setText("Time left: " + timeLeft + "s");
            if (timeLeft <= 0)
            {
                timer.stop();
                gameOver("Time's up! Game Over!");
            }
        });
        timer.start();
    }

    // Resets the timer for a new question
    private void resetTimer()
    {
        timeLeft = 5; // Reset the timer based on the selected difficulty
        timer.restart();
    }

    // Ends the game and disables further interaction
    private void gameOver(String message)
    {
        statusLabel.setText(message);
        timer.stop();
        disableComponents(this.getContentPane()); // Disables all components to prevent further interaction
    }

    // Recursively disables all components in a container
    private void disableComponents(Container container)
    {
        for (Component component : container.getComponents())
        {
            component.setEnabled(false);
            if (component instanceof Container)
            {
                disableComponents((Container) component);
            }
        }
    }

    // Inner class for the panel where shapes are drawn
    class GamePanel extends JPanel
    {
        private Color shapeColor; // Color of the shape to be drawn

        public GamePanel()
        {
            this.setPreferredSize(new Dimension(600, 400)); // Sets the preferred size for the panel
            drawRandomShape(); // Draws the initial shape
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g); // Calls the superclass method to handle basic painting features
            Graphics2D g2 = (Graphics2D) g; // Casts the Graphics object to Graphics2D for more advanced features
            g2.setColor(shapeColor); // Sets the color for drawing the shape
            g2.fill(currentShape); // Fills the shape with the set color
        }

        // Randomly selects a shape and its color, then requests a repaint
        public void drawRandomShape()
        {
            Random rand = new Random(); // Creates a random generator
            int shapeType = rand.nextInt(4); // Generates a random integer to decide the shape type
            int x = 250; // Base x coordinate for the shape
            int y = 150; // Base y coordinate for the shape
            int size = 100; // Base size for the shape
            shapeColor = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)); // Random color for the shape

            switch (shapeType)
            {
                case 0: // Rectangle
                    currentShape = new Rectangle2D.Float(x, y, size * 2, size); // Creates a rectangle
                    currentShapeType = "Rectangle"; // Sets the shape type as rectangle
                    break;
                case 1: // Circle
                    currentShape = new Ellipse2D.Float(x, y, size, size); // Creates a circle
                    currentShapeType = "Circle"; // Sets the shape type as circle
                    break;
                case 2: // Square
                    currentShape = new Rectangle2D.Float(x, y, size, size); // Creates a square
                    currentShapeType = "Square"; // Sets the shape type as square
                    break;
                case 3: // Trapezoid
                    Polygon trapezoid = new Polygon(); // Creates a polygon to represent a trapezoid
                    trapezoid.addPoint(x, y); // Adds the top left point
                    trapezoid.addPoint(x + 200, y); // Adds the top right point (wider for trapezoid shape)
                    trapezoid.addPoint(x + 150, y + size); // Adds the bottom right point
                    trapezoid.addPoint(x + 50, y + size); // Adds the bottom left point
                    currentShape = trapezoid; // Sets the currentShape to the trapezoid
                    currentShapeType = "Trapezoid"; // Sets the shape type as trapezoid
                    break;
            }

            repaint(); // Calls repaint to redraw the panel with the new shape
        }
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run() {
                new ShapeMatcherGame(); // Starts the game by creating an instance of ShapeMatcherGame
            }
        });
    }
}
