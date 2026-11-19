
// this part of the code has been modified
// added more imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

// everything that isn't in the view class is in model?

class Tile {
    private char symbol;
    private boolean flipped;
    private boolean matched;
    private boolean seen;

    public Tile(char symbol) {
        this.symbol = symbol;
        this.flipped = false;
        this.matched = false;
        this.seen = false;
    }

    public char getSymbol() {
        return symbol;
    }

    public boolean isFlipped() {
        return flipped;
    }

    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    // Track seen status of each card
    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}

class BoardModel {
    private List<Tile> tiles;
    private int matchesFound;
    private int flipsRemaining;
    private int playerScore;
    private Timer timer;
    private int secondsElapsed;
    private int gameDuration;
    private boolean firstTilePreviouslySeen;

    public BoardModel() {
        tiles = new ArrayList<>();
        matchesFound = 0;
        flipsRemaining = 2; // Set the number of allowed flips per turn
        playerScore = 0;
        secondsElapsed = 0;
        gameDuration = 120; // Set the game duration in seconds INCREASED FOR TESTING
        firstTilePreviouslySeen = false;
    }

    // Get player score
    public int getPlayerScore() {
        return playerScore;
    }

    public void initializeTiles(int pairs) {
        for (char symbol = 'A'; symbol < 'A' + pairs; symbol++) {
            tiles.add(new Tile(symbol));
            tiles.add(new Tile(symbol));
        }
        Collections.shuffle(tiles);
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    public void play() {
        Scanner scanner = new Scanner(System.in);
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                secondsElapsed++;
                if (secondsElapsed >= gameDuration) {
                    System.out.println("Time's up! Game over.");
                    System.out.println("Your final score: " + playerScore); // added for testing
                    endGame();
                }
            }
        }, 1000, 1000);

        // this section of code has been modified and moved to controller
        while (matchesFound < tiles.size() / 2) {

            TextView.displayBoard(this);

            if (GameController.control(this, scanner)) {
                continue;
            } else {
                return;
            }

        }

        System.out.println("Congratulations! You found all the matches.");
        System.out.println("Your final score: " + playerScore);
        System.out.println("Time elapsed: " + secondsElapsed + " seconds");
        scanner.close();
        endGame();
    }

    public void quit() {
        System.out.println("Quitting the game. Goodbye!");
        endGame();
        timer.cancel();
    }

    public void restart() {
        System.out.println("Restarting the game...");
        timer.cancel();
        restartGame();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                secondsElapsed++;
                if (secondsElapsed >= gameDuration) {
                    System.out.println("Time's up! Game over.");
                    endGame();
                }
            }
        }, 1000, 1000);
    }

    public void flipTile(Tile tile) {
        if (tile.isMatched()) {
            return;
        }

        // Check if this tile was ALREADY seen before this flip
        boolean wasAlreadySeen = tile.isSeen();
        if (flipsRemaining == 2) {
            firstTilePreviouslySeen = wasAlreadySeen;
        }

        tile.setFlipped(true);
        // seen status
        tile.setSeen(true);
        flipsRemaining--;
        if (checkForMatch(tile)) {
            System.out.println("Match found! (+10 points!)");
            matchesFound++;
            playerScore += 10; // points earned only if card match

            for (Tile t : tiles) {
                if (t.isFlipped() &&
                        t.getSymbol() == tile.getSymbol()) {
                    t.setMatched(true);
                }
            }
            flipsRemaining = 2;

        } else if (flipsRemaining == 0) {
            // Deduct points ONLY if seen cards are not correctly matched
            int penalty = 0;
            if (firstTilePreviouslySeen) {
                penalty += 2;
            }
            if (wasAlreadySeen) {
                penalty += 2;
            }
            playerScore -= penalty;

            if (penalty > 0) {
                System.out.println("No match! Missed seen card(s): -" + penalty + " points.");
            } else {
                System.out.println("No match. Out of flips. Next turn.");
            }

            // Shows both flipped cards for a second so it doesnt
            // reset immediately, so you can see both letters.
            TextView.displayBoard(this);
            try {
                Thread.sleep(1500); // Pause 1.5 seconds so you can see the 2nd card
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            resetFlippedTiles();
            flipsRemaining = 2; // Reset the flips remaining for the next turn
            firstTilePreviouslySeen = false;
        } else {
            System.out.println("No match. Try again.");
        }
    }

    private boolean checkForMatch(Tile tile) {
        int count = 0;
        for (Tile t : tiles) {
            if (t.isFlipped() && !t.isMatched() && t.getSymbol() == tile.getSymbol()) {
                count++;
            }
        }
        return count == 2;
    }

    // private void displayBoard()
    // this got exactly 1-1 moved to viewer class

    private void resetFlippedTiles() {
        for (Tile tile : tiles) {
            if (!tile.isMatched()) {
                tile.setFlipped(false);
            }
        }
    }

    private void restartGame() {
        tiles.clear();
        matchesFound = 0;
        flipsRemaining = 2;
        playerScore = 0;
        secondsElapsed = 0;
        firstTilePreviouslySeen = false;
        initializeTiles(6); // Change the number of pairs as per your preference
    }

    private void endGame() {
        timer.cancel();
    }
}

// this is the view secion of the architecture
// this section of code has been modified
class TextView {
    public static void displayBoard(BoardModel model) {
        System.out.println("\n------- Memory Game -------");
        // Display the score and the board
        System.out.println("Score: " + model.getPlayerScore());
        System.out.print("   ");
        for (int i = 0; i < model.getTiles().size(); i++) {
            System.out.print(i + "\t");
        }
        System.out.println();
        for (int i = 0; i < model.getTiles().size(); i++) {
            if (i % 6 == 0) {
                System.out.println();
                System.out.print(i / 6 + "  ");
            }
            Tile tile = model.getTiles().get(i);
            if (tile.isFlipped()) {
                System.out.print(tile.getSymbol() + "\t");
            } else {
                System.out.print("[" + i + "]\t");
            }
        }
        System.out.println("\n---------------------------");
        System.out.println("Enter the tile number to flip, 'q' to quit, or 'r' to restart: ");
    }
}

// this is the controller for the code
// this section of code has been modified
class GameController {
    public static boolean control(BoardModel model, Scanner scanner) {
        String input = scanner.nextLine();
        if (input.equalsIgnoreCase("q")) {
            model.quit();
            return false;
        } else if (input.equalsIgnoreCase("r")) {
            model.restart();
            return true;
        }
        int tileIndex;
        try {
            tileIndex = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a tile number.");
            return true;
        }
        if (tileIndex < 0 || tileIndex >= model.getTiles().size()) {
            System.out.println("Invalid tile number. Please enter a valid tile number.");
            return true;
        }
        Tile tile = model.getTiles().get(tileIndex);
        if (tile.isFlipped()) {
            System.out.println("Tile already flipped. Try again.");
        } else {
            model.flipTile(tile);
        }
        return true;
    }
}

public class Main {
    public static void main(String[] args) {
        BoardModel game = new BoardModel();
        game.initializeTiles(6); // Change the number of pairs as per your preference
        game.play();
    }
}
