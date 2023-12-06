package assignment2;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Board {
    public int size = 5;
    // 2D Array of Cells for representation of the game board
    public final Cell[][] board = new Cell[size][size];

    private Piece.Type turn;
    private Piece.Type winner;

    /**
     * Create a Board with the current player turn set.
     */
    public Board() {
        this.loadBoard(new File("Boards/Starter.txt"));
    }

    public Board(EventHandler<MouseEvent> cellClickHandler) {
        this.loadBoard(new File("Boards/Starter.txt"));
        setCellClickHandler(cellClickHandler);
    }

    public void setCellClickHandler(EventHandler<MouseEvent> cellClickHandler) {
        for (Cell[] row : board) {
            for (Cell cell : row) {
                cell.setOnMouseClicked(cellClickHandler);
            }
        }
    }

//    public void handleCellClick(MouseEvent actionEvent) {
//        Cell clickedCell = (Cell) actionEvent.getSource();
//        // Perform actions based on the clicked cell, e.g., make a move or select a piece.
//        // You may want to implement the logic for handling cell clicks here.
//    }

    /**
     * Create a Board with the current player turn set and a specified board.
     *
     * @param boardFile The board file to load (e.g. "Boards/Starter.txt")
     */
    public Board(File boardFile) {
        this.loadBoard(boardFile);
    }

    /**
     * Creates a Board copy of the given board.
     * @param board Board to copy
     */
    public Board(Board board) {
        this.size = board.size;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                this.board[row][col] = new Cell(board.board[row][col]);
            }
        }
        this.turn = board.turn;
        this.winner = board.winner;
    }

    /**
     * @return the Piece.Type (Musketeer or Guard) of the current turn
     */
    public Piece.Type getTurn() {
        return turn;
    }

    public void setTurn(Piece.Type turn) {
        this.turn = turn;
    }

    public Cell getCell(Coordinate coordinate) {
        return this.board[coordinate.row][coordinate.col];
    }

    public Piece.Type getWinner() {
        return winner;
    }

    /**
     * Gets all the Musketeer cells on the board.
     * @return List of cells
     */
    public List<Cell> getMusketeerCells() {
        List<Cell> cells = new ArrayList<>();
        for (Cell[] row : board) {
            for (Cell cellOfRow : row) {
                if (cellOfRow.getPiece() != null && cellOfRow.getPiece().getType() == Piece.Type.MUSKETEER) {
                    cells.add(cellOfRow);
                }
            }
        }
        return cells;
    }

    /**
     * Gets all the Guard cells on the board.
     * @return List of cells
     */
    public List<Cell> getGuardCells() {
        var cells = new ArrayList<Cell>();
        for (Cell[] row : board) {
            for (Cell cellOfRow : row) {
                if (cellOfRow.getPiece() != null && cellOfRow.getPiece().getType() == Piece.Type.GUARD)
                    cells.add(cellOfRow);
            }
        }
        return cells;
    }

    /**
     * Executes the given move on the board.
     * @param move a valid move
     */
    public void move(Move move) {
        int rowFrom = move.fromCell.getCoordinate().row;
        int colFrom = move.fromCell.getCoordinate().col;
        int rowTo = move.toCell.getCoordinate().row;
        int colTo = move.toCell.getCoordinate().col;
        board[rowTo][colTo].setPiece(move.fromCell.getPiece());
        board[rowFrom][colFrom].setPiece(null);
        if (turn == Piece.Type.MUSKETEER) {
            turn = Piece.Type.GUARD;
        } else {
            turn = Piece.Type.MUSKETEER;
        }
    }

    /**
     * Undo the move given.
     * @param move Copy of a move that was done and needs to be undone. The move copy has the correct piece info in the
     *             from and to cell fields.
     */
    public void undoMove(Move move) {
        int rowFrom = move.fromCell.getCoordinate().row;
        int colFrom = move.fromCell.getCoordinate().col;
        int rowTo = move.toCell.getCoordinate().row;
        int colTo = move.toCell.getCoordinate().col;
        board[rowFrom][colFrom].setPiece(move.fromCell.getPiece());
        //board[rowTo][colTo].setPiece(move.toCell == null ? null : move.toCell.getPiece());
        board[rowTo][colTo].setPiece(move.toCell.getPiece());

        if (turn == Piece.Type.MUSKETEER) {
            turn = Piece.Type.GUARD;
        } else {
            turn = Piece.Type.MUSKETEER;
        }
    }

    /**
     * Checks if the given move is valid.
     *
     * @param move a move
     * @return True, if the move is valid, false otherwise
     */
    public Boolean isValidMove(Move move) {
        boolean isValidCordinate = false;
        if (move.toCell.getCoordinate().row > size ||
                move.toCell.getCoordinate().col > size ||
                move.toCell.getCoordinate().row < 0 ||
                move.toCell.getCoordinate().col < 0)
            return false;

        if (move.fromCell.getCoordinate().row == move.toCell.getCoordinate().row &&
                move.fromCell.getCoordinate().col == move.toCell.getCoordinate().col + 1) {
            isValidCordinate = true;
        } else if
        (move.fromCell.getCoordinate().row == move.toCell.getCoordinate().row &&
                        move.fromCell.getCoordinate().col == move.toCell.getCoordinate().col - 1) {
            isValidCordinate = true;
        } else if
        (move.fromCell.getCoordinate().row == move.toCell.getCoordinate().row + 1 &&
                        move.fromCell.getCoordinate().col == move.toCell.getCoordinate().col) {
            isValidCordinate = true;
        } else if (move.fromCell.getCoordinate().row == move.toCell.getCoordinate().row - 1 &&
                move.fromCell.getCoordinate().col == move.toCell.getCoordinate().col) {
            isValidCordinate = true;
        }

        if (isValidCordinate) {
            if (move.fromCell.getPiece().getType() == Piece.Type.MUSKETEER
                    && move.toCell.getPiece() != null
                    && move.toCell.getPiece().getType() == Piece.Type.GUARD) {
                return true;
            } else if (move.fromCell.getPiece().getType() == Piece.Type.GUARD
                    && move.toCell.getPiece() == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get all the possible cells that have pieces that can be moved this turn.
     *
     * @return Cells that can be moved from the given cells
     */
    public List<Cell> getPossibleCells() {
        var possibleCells = new ArrayList<Cell>();
        if (turn == Piece.Type.MUSKETEER) {
            for (Cell muskCell : getMusketeerCells()) {
                if (getPossibleDestinations(muskCell).size() >= 1) {
                    possibleCells.add(muskCell);
                }
            }
        } else {
            for (Cell guardCell : getGuardCells()) {
                if (getPossibleDestinations(guardCell).size() >= 1) {
                    possibleCells.add(guardCell);
                }
            }
        }
        return possibleCells;
    }

    /**
     * Get all the possible cell destinations that is possible to move to from the fromCell.
     * @param fromCell The cell that has the piece that is going to be moved
     * @return List of cells that are possible to get to
     */
    public List<Cell> getPossibleDestinations(Cell fromCell) {
        var possibleCells = new ArrayList<Cell>();
        if ((fromCell.getCoordinate().col + 1) < size) {
            var toCell1 = board[fromCell.getCoordinate().row][fromCell.getCoordinate().col + 1];
            if (isValidMove(new Move(fromCell, toCell1))) {
                possibleCells.add(toCell1);
            }
        }
        if ((fromCell.getCoordinate().col - 1) >= 0) {
            var toCell2 = board[fromCell.getCoordinate().row][fromCell.getCoordinate().col - 1];
            if (isValidMove(new Move(fromCell, toCell2))) {
                possibleCells.add(toCell2);
            }
        }
        if ((fromCell.getCoordinate().row + 1) < size) {
            var toCell3 = board[fromCell.getCoordinate().row + 1][fromCell.getCoordinate().col];
            if (isValidMove(new Move(fromCell, toCell3))) {
                possibleCells.add(toCell3);
            }
        }
        if ((fromCell.getCoordinate().row - 1) >= 0) {
            var toCell4 = board[fromCell.getCoordinate().row - 1][fromCell.getCoordinate().col];
            if (isValidMove(new Move(fromCell, toCell4))) {
                possibleCells.add(toCell4);
            }
        }
        return possibleCells;
    }

    /**
     * Get all the possible moves that can be made this turn.
     * @return List of moves that can be made this turn
     */
    public List<Move> getPossibleMoves() {
        var possibleStart = getPossibleCells();
        var possibleMoves = new ArrayList<Move>();
        for (Cell c : possibleStart) {
            for (Cell d : getPossibleDestinations(c)) {
                Move m = new Move(c, d);
                possibleMoves.add(m);
            }
        }

        return possibleMoves;
    }

    /**
     * Checks if the game is over and sets the winner if there is one.
     * @return True, if the game is over, false otherwise.
     */
    public boolean isGameOver() {
        List<Move> possibleMoves = getPossibleMoves();
        if (possibleMoves.size() == 0 && turn == Piece.Type.GUARD) {
            winner = Piece.Type.MUSKETEER;
            return true;
        }
        if (possibleMoves.size() == 0 && turn == Piece.Type.MUSKETEER) {
            winner = Piece.Type.GUARD;
            return true;
        } else
            return false;
    }

    /**
     * Saves the current board state to the board's directory.
     *
     * @param file File to save the board to
     */
    public void saveBoard(File file) {
        try {
            file.createNewFile();
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            writer.write(turn.getType() + "\n");
            for (Cell[] row : board) {
                StringBuilder line = new StringBuilder();
                for (Cell cell : row) {
                    if (cell.getPiece() != null) {
                        line.append(cell.getPiece().getSymbol());
                    } else {
                        line.append("_");
                    }
                    line.append(" ");
                }
                writer.write(line.toString().strip() + "\n");
            }
            writer.close();
            System.out.printf("Saved board to %s.\n", file.getPath());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.printf("Failed to save board to %s.\n", file.getPath());
        }
    }

    @Override
    public String toString() {
        StringBuilder boardStr = new StringBuilder("  | A B C D E\n");
        boardStr.append("--+----------\n");
        for (int i = 0; i < size; i++) {
            boardStr.append(i + 1).append(" | ");
            for (int j = 0; j < size; j++) {
                Cell cell = board[i][j];
                boardStr.append(cell).append(" ");
            }
            boardStr.append("\n");
        }
        return boardStr.toString();
    }


    /**
     * Loads a board file from a file path.
     *
     * @param file The board file to load (e.g. "boards/Starter.txt")
     */
    protected void loadBoard(File file) {
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.err.printf("File at %s not found.", file.getPath());
            System.exit(1);
        }

        turn = Piece.Type.valueOf(scanner.nextLine().toUpperCase());

        int row = 0, col = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] pieces = line.trim().split(" ");
            for (String piece: pieces) {
                Cell cell = new Cell(new Coordinate(row, col));
                switch (piece) {
                    case "O" -> cell.setPiece(new Guard());
                    case "X" -> cell.setPiece(new Musketeer());
                    default -> cell.setPiece(null);
                }
                this.board[row][col] = cell;
                col += 1;
            }
            col = 0;
            row += 1;
        }
        scanner.close();
        System.out.printf("Loaded board from %s.\n", file.getPath());
    }
}
