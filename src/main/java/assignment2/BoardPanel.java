package assignment2;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class BoardPanel extends GridPane implements EventHandler<ActionEvent> {

    private final View view;
    private final Board board;
    private Cell[][] cells;
    private ThreeMusketeers model;
    private Cell selectedPiece;  // Add this line



    /**
     * Constructs a new GridPane that contains a Cell for each position in the board
     * <p>
     * Contains default alignment and styles which can be modified
     *
     * @param view
     * @param board
     */
    public BoardPanel(View view, Board board) {
        this.view = view;
        this.board = board;

        // Can modify styling
        this.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: #181a1b;");
        int size = 550;
        this.setPrefSize(size, size);
        this.setMinSize(size, size);
        this.setMaxSize(size, size);

        setupBoard();
        updateCells();
    }


    /**
     * Setup the BoardPanel with Cells
     */
    private void setupBoard() { // TODO
        cells = new Cell[5][5];

        // Load board configuration from file
        Board board = new Board(new File("C:\\Users\\jgiso\\Documents\\A2\\boards\\Starter.txt"));

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                Coordinate coordinate = new Coordinate(row, col);
                Cell cell = new Cell(coordinate);

                // Set Musketeer or Guard based on the piece in the loaded board
                Piece piece = board.getCell(coordinate).getPiece();
                if (piece instanceof Musketeer) {
                    cell.setPiece(new Musketeer());
                } else if (piece instanceof Guard) {
                    cell.setPiece(new Guard());
                } else {
                    // Handle invalid piece
                    System.err.println("Invalid piece in the loaded board");
                    return;
                }

                cell.setOnAction(this::handle); // Set the event handler for the cell
                cells[row][col] = cell;
                this.add(cell, col, row);
            }
        }
//            cells = new Cell[5][5];
//
//            // Load board configuration from file
//            Board board = new Board(new File("C:\\Users\\jgiso\\Documents\\A2\\boards\\Starter.txt"));
//
//            for (int row = 0; row < 5; row++) {
//                for (int col = 0; col < 5; col++) {
//                    Coordinate coordinate = new Coordinate(row, col);
//                    Cell cell = new Cell(coordinate);
//
//                    // Set Musketeer or Guard based on the piece in the loaded board
//                    Piece piece = board.getCell(coordinate).getPiece();
//                    if (piece instanceof Musketeer) {
//                        cell.setPiece(new Musketeer());
//                    } else if (piece instanceof Guard) {
//                        cell.setPiece(new Guard());
//                    } else {
//                        // Handle invalid piece
//                        System.err.println("Invalid piece in the loaded board");
//                        return;
//                    }
//
//                    cells[row][col] = cell;
//                    this.add(cell, col, row);
//                }
//            }
        }


    /**
     * Updates the BoardPanel to represent the board with the latest information
     * <p>
     * If it's a computer move: disable all cells and disable all game controls in view
     * <p>
     * If it's a human player turn and they are picking a piece to move:
     * - disable all cells
     * - enable cells containing valid pieces that the player can move
     * If it's a human player turn and they have picked a piece to move:
     * - disable all cells
     * - enable cells containing other valid pieces the player can move
     * - enable cells containing the possible destinations for the currently selected piece
     * <p>
     * If the game is over:
     * - update view.messageLabel with the winner ('MUSKETEER' or 'GUARD')
     * - disable all cells
     */
    protected void updateCells() { // TODO
        disableAllCells();

        List<Cell> possibleCells = board.getPossibleCells();
        for (Cell[] row : cells) {
            for (Cell cell : row) {
                if (!board.getPossibleCells().isEmpty()) {
                    cell.setDisable(false);
                }
            }
        }

        if (board.isGameOver()) {
            view.setMessageLabel(board.getWinner() + " WINS!");
            disableAllCells();
        }
    }



    public void disableAllCells() {
        for (Cell[] row : cells) {
            for (Cell cell : row) {
                cell.setDisable(true);
            }
        }
    }


    /**
     * Handles Cell clicks and updates the board accordingly
     * When a Cell gets clicked the following must be handled:
     * - If it's a valid piece that the player can move, select the piece and update the board
     * - If it's a destination for a selected piece to move, perform the move and update the board
     *
     * @param actionEvent
     */
    @Override
    public void handle(ActionEvent actionEvent) { // TODO
        if (actionEvent.getSource() instanceof Cell) {
            Cell clickedCell = (Cell) actionEvent.getSource();

            if (selectedPiece == null && clickedCell.hasPiece() && clickedCell.getPiece().getType() == Piece.Type.MUSKETEER) {
                // Case 1: No piece is selected, and the clicked cell has a piece
                // Select the piece
                selectedPiece = clickedCell;
                clickedCell.setAgentFromColor();
                List<Cell> possibleDestinations = board.getPossibleDestinations(selectedPiece);
                for (Cell destination : possibleDestinations) {
                    destination.setOptionsColor();
                }

            } else if (selectedPiece != null && selectedPiece.equals(clickedCell)) {
                // Case 2: A piece is selected, and the clicked cell is empty
                // Check if the move is valid
                selectedPiece.setDefaultColor();  // Unhighlight the deselected Musketeer
                selectedPiece = null;

                // Clear possible destination colors
                for (Cell[] row : cells) {
                    for (Cell cell : row) {
                        cell.setDefaultColor();
                    }
                }
            } else if (selectedPiece != null && isValidMove(selectedPiece, clickedCell)) {
                // Case 3: A Musketeer is selected, and the clicked cell is a valid destination
                // Perform the move
                Move move = new Move(selectedPiece, clickedCell);
                board.move(move);
                updateCells();
                selectedPiece.setDefaultColor();  // Unhighlight the previously selected Musketeer
                selectedPiece = null; // Reset the selected Musketeer

                // Clear possible destination colors
                for (Cell[] row : cells) {
                    for (Cell cell : row) {
                        cell.setDefaultColor();
                    }
                }
            }
        }
    }


    private boolean isValidMove(Cell fromCell, Cell toCell) {
        // Check if the move is valid based on your criteria
        // For example:
        Piece piece = fromCell.getPiece();
        return piece != null && piece.canMoveOnto(toCell);
    }
}



