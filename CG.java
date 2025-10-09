import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CG extends JFrame {
    private static final int SIZE = 8;
    private final JButton[][] squares = new JButton[SIZE][SIZE];
    private String[][] board = new String[SIZE][SIZE];

    // Game State
    private Point selectedPiece = null;
    private boolean whiteTurn = true;
    private boolean vsComputer = false;
    private String difficulty = "Easy"; // Easy, Medium, Hard

    // State for special moves
    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteRookAMoved = false;
    private boolean whiteRookHMoved = false;
    private boolean blackRookAMoved = false;
    private boolean blackRookHMoved = false;
    private Point enPassantTarget = null; // The square a pawn can move to for en passant

    private final Color lightColor = new Color(240, 217, 181);
    private final Color darkColor = new Color(181, 136, 99);
    private final Color selectedColor = new Color(137, 196, 244);
    private final Color validMoveColor = new Color(100, 150, 100, 150); // Semi-transparent green

    public CG() {
        // Mode selection dialogs
        if (!setupGameMode()) {
            System.exit(0);
        }

        setTitle("Java Chess Game");
        setSize(800, 800);
        setLayout(new GridLayout(SIZE, SIZE));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initBoard();
        drawBoard();
        updateTitle();

        setVisible(true);
    }

    private boolean setupGameMode() {
        String[] modes = {"Player vs Player", "Player vs Computer"};
        String mode = (String) JOptionPane.showInputDialog(
                this, "Choose game mode:", "Game Mode",
                JOptionPane.QUESTION_MESSAGE, null, modes, modes[0]);

        if (mode == null) return false; // User closed the dialog

        if (mode.equals("Player vs Computer")) {
            vsComputer = true;
            String[] levels = {"Easy", "Medium"}; // Hard is a placeholder
            difficulty = (String) JOptionPane.showInputDialog(
                    this, "Choose difficulty:", "Difficulty",
                    JOptionPane.QUESTION_MESSAGE, null, levels, levels[0]);
            if (difficulty == null) return false; // User closed the dialog
        }
        return true;
    }

    private void initBoard() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                board[r][c] = "";
            }
        }
        String[] backRank = {"r", "n", "b", "q", "k", "b", "n", "r"};
        for (int c = 0; c < SIZE; c++) {
            board[0][c] = "b" + backRank[c];
            board[1][c] = "bp";
            board[6][c] = "wp";
            board[7][c] = "w" + backRank[c];
        }
    }

    private void drawBoard() {
        Font font = new Font("SansSerif", Font.PLAIN, 48);
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                JButton btn = new JButton();
                btn.setFont(font);
                btn.setFocusPainted(false);
                btn.setText(getPieceUnicode(board[r][c]));

                int row = r, col = c;
                btn.addActionListener(e -> onSquareClick(row, col));
                squares[r][c] = btn;
                add(btn);
            }
        }
        refreshBoardColors();
    }

    private String getPieceUnicode(String piece) {
        if (piece.isEmpty()) return "";
        switch (piece) {
            case "wp": return "♙"; case "bp": return "♟";
            case "wr": return "♖"; case "br": return "♜";
            case "wn": return "♘"; case "bn": return "♞";
            case "wb": return "♗"; case "bb": return "♝";
            case "wq": return "♕"; case "bq": return "♛";
            case "wk": return "♔"; case "bk": return "♚";
            default: return "?";
        }
    }

    private void onSquareClick(int row, int col) {
        if (selectedPiece == null) {
            // Nothing selected, try to select a piece
            if (!board[row][col].isEmpty() && isCurrentTurn(board[row][col].charAt(0))) {
                selectedPiece = new Point(row, col);
                highlightValidMoves(row, col);
            }
        } else {
            // A piece is already selected, try to move it
            int r1 = selectedPiece.x;
            int c1 = selectedPiece.y;

            // If clicking another of own pieces, switch selection
            if (!board[row][col].isEmpty() && isCurrentTurn(board[row][col].charAt(0))) {
                selectedPiece = new Point(row, col);
                refreshBoardColors();
                highlightValidMoves(row, col);
                return;
            }

            if (isValidMove(r1, c1, row, col)) {
                movePiece(r1, c1, row, col);
                selectedPiece = null;
                refreshBoardColors();
                switchTurn();
            } else {
                // Invalid move, deselect
                selectedPiece = null;
                refreshBoardColors();
            }
        }
    }

    private boolean isCurrentTurn(char pieceColor) {
        return (whiteTurn && pieceColor == 'w') || (!whiteTurn && pieceColor == 'b');
    }

    private void switchTurn() {
        whiteTurn = !whiteTurn;
        updateTitle();
        checkGameState();

        if (vsComputer && !whiteTurn) {
            // Add a small delay for a better user experience
            Timer timer = new Timer(500, e -> {
                makeComputerMove();
                whiteTurn = !whiteTurn;
                updateTitle();
                checkGameState();
            });
            timer.setRepeats(false);
            timer.start();
        }
    }
    
    private void updateTitle() {
        String turn = whiteTurn ? "White's Turn" : "Black's Turn";
        if (isKingInCheck(whiteTurn)) {
            turn += " (Check!)";
        }
        setTitle("Java Chess Game - " + turn);
    }

    private void movePiece(int r1, int c1, int r2, int c2) {
        String piece = board[r1][c1];
        char pieceType = piece.charAt(1);

        // Reset en passant target
        enPassantTarget = null;

        // Set new en passant target if a pawn moves two squares
        if (pieceType == 'p' && Math.abs(r1 - r2) == 2) {
            enPassantTarget = new Point((r1 + r2) / 2, c1);
        }

        // Handle en passant capture
        if (pieceType == 'p' && c1 != c2 && board[r2][c2].isEmpty()) {
            int capturedPawnRow = whiteTurn ? r2 + 1 : r2 - 1;
            board[capturedPawnRow][c2] = "";
            squares[capturedPawnRow][c2].setText("");
        }

        // Handle castling move
        if (pieceType == 'k' && Math.abs(c1 - c2) == 2) {
            if (c2 > c1) { // Kingside
                board[r1][c1 + 1] = board[r1][c2 + 1];
                board[r1][c2 + 1] = "";
                squares[r1][c1 + 1].setText(getPieceUnicode(board[r1][c1 + 1]));
                squares[r1][c2 + 1].setText("");
            } else { // Queenside
                board[r1][c1 - 1] = board[r1][c2 - 2];
                board[r1][c2 - 2] = "";
                squares[r1][c1 - 1].setText(getPieceUnicode(board[r1][c1 - 1]));
                squares[r1][c2 - 2].setText("");
            }
        }

        // Update moved flags for castling
        if (piece.equals("wk")) whiteKingMoved = true;
        if (piece.equals("bk")) blackKingMoved = true;
        if (piece.equals("wr")) {
            if (c1 == 0) whiteRookAMoved = true;
            if (c1 == 7) whiteRookHMoved = true;
        }
        if (piece.equals("br")) {
            if (c1 == 0) blackRookAMoved = true;
            if (c1 == 7) blackRookHMoved = true;
        }

        // Standard move
        board[r2][c2] = piece;
        board[r1][c1] = "";
        squares[r2][c2].setText(getPieceUnicode(piece));
        squares[r1][c1].setText("");
        
        // Pawn Promotion
        if (pieceType == 'p' && (r2 == 0 || r2 == 7)) {
            promotePawn(r2, c2);
        }
    }

    private void promotePawn(int r, int c) {
        String[] options = {"Queen", "Rook", "Bishop", "Knight"};
        String choice = (String) JOptionPane.showInputDialog(
                this, "Promote pawn to:", "Pawn Promotion",
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (choice == null) choice = "Queen"; // Default to Queen if dialog is closed
        
        char pieceChar;
        switch (choice) {
            case "Rook": pieceChar = 'r'; break;
            case "Knight": pieceChar = 'n'; break;
            case "Bishop": pieceChar = 'b'; break;
            default: pieceChar = 'q'; break;
        }
        String newPiece = (whiteTurn ? "w" : "b") + pieceChar;
        board[r][c] = newPiece;
        squares[r][c].setText(getPieceUnicode(newPiece));
    }


    // --- Move Validation ---

    private boolean isValidMove(int r1, int c1, int r2, int c2) {
        List<Point> validMoves = generateValidMovesForPiece(r1, c1);
        return validMoves.contains(new Point(r2, c2));
    }

    private List<Point> generateValidMovesForPiece(int r, int c) {
        List<Point> moves = new ArrayList<>();
        String piece = board[r][c];
        if (piece.isEmpty()) return moves;

        char pieceColor = piece.charAt(0);
        char pieceType = piece.charAt(1);

        switch (pieceType) {
            case 'p': moves.addAll(getPawnMoves(r, c, pieceColor)); break;
            case 'r': moves.addAll(getRookMoves(r, c)); break;
            case 'n': moves.addAll(getKnightMoves(r, c)); break;
            case 'b': moves.addAll(getBishopMoves(r, c)); break;
            case 'q': moves.addAll(getQueenMoves(r, c)); break;
            case 'k': moves.addAll(getKingMoves(r, c, pieceColor)); break;
        }

        // Filter out moves that would leave the king in check
        List<Point> legalMoves = new ArrayList<>();
        for (Point move : moves) {
            if (isMoveLegal(r, c, move.x, move.y)) {
                legalMoves.add(move);
            }
        }
        return legalMoves;
    }
    
    private boolean isMoveLegal(int r1, int c1, int r2, int c2) {
        String piece = board[r1][c1];
        String capturedPiece = board[r2][c2];

        // Simulate move
        board[r2][c2] = piece;
        board[r1][c1] = "";

        boolean kingInCheck = isKingInCheck(whiteTurn);

        // Undo move
        board[r1][c1] = piece;
        board[r2][c2] = capturedPiece;

        return !kingInCheck;
    }

    private List<Point> getPawnMoves(int r, int c, char color) {
        List<Point> moves = new ArrayList<>();
        int dir = (color == 'w') ? -1 : 1;
        int startRow = (color == 'w') ? 6 : 1;

        // 1. Forward one square
        if (r + dir >= 0 && r + dir < SIZE && board[r + dir][c].isEmpty()) {
            moves.add(new Point(r + dir, c));
            // 2. Forward two squares from start
            if (r == startRow && board[r + 2 * dir][c].isEmpty()) {
                moves.add(new Point(r + 2 * dir, c));
            }
        }
        // 3. Diagonal captures
        for (int dc = -1; dc <= 1; dc += 2) {
            if (c + dc >= 0 && c + dc < SIZE) {
                // Regular capture
                if (!board[r + dir][c + dc].isEmpty() && board[r + dir][c + dc].charAt(0) != color) {
                    moves.add(new Point(r + dir, c + dc));
                }
                // En passant
                if (enPassantTarget != null && enPassantTarget.x == r + dir && enPassantTarget.y == c + dc) {
                    moves.add(new Point(r + dir, c + dc));
                }
            }
        }
        return moves;
    }

    private List<Point> getRookMoves(int r, int c) {
        List<Point> moves = new ArrayList<>();
        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};
        addSlidingMoves(moves, r, c, dr, dc);
        return moves;
    }

    private List<Point> getBishopMoves(int r, int c) {
        List<Point> moves = new ArrayList<>();
        int[] dr = {-1, -1, 1, 1};
        int[] dc = {-1, 1, -1, 1};
        addSlidingMoves(moves, r, c, dr, dc);
        return moves;
    }

    private List<Point> getQueenMoves(int r, int c) {
        List<Point> moves = getRookMoves(r, c);
        moves.addAll(getBishopMoves(r, c));
        return moves;
    }

    private List<Point> getKnightMoves(int r, int c) {
        List<Point> moves = new ArrayList<>();
        int[] dr = {-2, -2, -1, -1, 1, 1, 2, 2};
        int[] dc = {-1, 1, -2, 2, -2, 2, -1, 1};
        char color = board[r][c].charAt(0);

        for (int i = 0; i < 8; i++) {
            int nr = r + dr[i];
            int nc = c + dc[i];
            if (nr >= 0 && nr < SIZE && nc >= 0 && nc < SIZE) {
                if (board[nr][nc].isEmpty() || board[nr][nc].charAt(0) != color) {
                    moves.add(new Point(nr, nc));
                }
            }
        }
        return moves;
    }

    private List<Point> getKingMoves(int r, int c, char color) {
        List<Point> moves = new ArrayList<>();
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = r + dr;
                int nc = c + dc;
                if (nr >= 0 && nr < SIZE && nc >= 0 && nc < SIZE) {
                    if (board[nr][nc].isEmpty() || board[nr][nc].charAt(0) != color) {
                        moves.add(new Point(nr, nc));
                    }
                }
            }
        }
        
        // Castling
        if (!isKingInCheck(color == 'w')) {
             // Kingside
            if ((color == 'w' ? !whiteKingMoved && !whiteRookHMoved : !blackKingMoved && !blackRookHMoved) &&
                board[r][c + 1].isEmpty() && board[r][c + 2].isEmpty() &&
                !isSquareAttacked(r, c + 1, color == 'b') && !isSquareAttacked(r, c + 2, color == 'b')) {
                moves.add(new Point(r, c + 2));
            }
            // Queenside
            if ((color == 'w' ? !whiteKingMoved && !whiteRookAMoved : !blackKingMoved && !blackRookAMoved) &&
                board[r][c - 1].isEmpty() && board[r][c - 2].isEmpty() && board[r][c - 3].isEmpty() &&
                !isSquareAttacked(r, c - 1, color == 'b') && !isSquareAttacked(r, c - 2, color == 'b')) {
                moves.add(new Point(r, c - 2));
            }
        }

        return moves;
    }

    private void addSlidingMoves(List<Point> moves, int r, int c, int[] dr, int[] dc) {
        char color = board[r][c].charAt(0);
        for (int i = 0; i < dr.length; i++) {
            for (int k = 1; k < SIZE; k++) {
                int nr = r + k * dr[i];
                int nc = c + k * dc[i];
                if (nr < 0 || nr >= SIZE || nc < 0 || nc >= SIZE) break;
                if (board[nr][nc].isEmpty()) {
                    moves.add(new Point(nr, nc));
                } else {
                    if (board[nr][nc].charAt(0) != color) {
                        moves.add(new Point(nr, nc));
                    }
                    break;
                }
            }
        }
    }


    // --- Check and Game State Logic ---

    private Point findKing(boolean isWhite) {
        char kingColor = isWhite ? 'w' : 'b';
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c].equals(kingColor + "k")) {
                    return new Point(r, c);
                }
            }
        }
        return null; // Should not happen
    }

    private boolean isKingInCheck(boolean isWhite) {
        Point kingPos = findKing(isWhite);
        if (kingPos == null) return true; // King is captured, game over
        return isSquareAttacked(kingPos.x, kingPos.y, !isWhite);
    }
    
    private boolean isSquareAttacked(int r, int c, boolean byWhite) {
        char opponentColor = byWhite ? 'w' : 'b';
        // Check for attacks from all opponent pieces
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (!board[i][j].isEmpty() && board[i][j].charAt(0) == opponentColor) {
                    List<Point> moves = getPieceMovesIgnoringCheck(i, j); // Get raw moves
                    for (Point move : moves) {
                        if (move.x == r && move.y == c) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    // A version of move generation that doesn't check for self-check, for use in isSquareAttacked
    private List<Point> getPieceMovesIgnoringCheck(int r, int c) {
        List<Point> moves = new ArrayList<>();
        String piece = board[r][c];
        char pieceColor = piece.charAt(0);
        char pieceType = piece.charAt(1);
        switch (pieceType) {
            case 'p': // Pawn attacks are special
                int dir = (pieceColor == 'w') ? -1 : 1;
                if(c > 0) moves.add(new Point(r + dir, c - 1));
                if(c < 7) moves.add(new Point(r + dir, c + 1));
                break;
            case 'r': moves.addAll(getRookMoves(r, c)); break;
            case 'n': moves.addAll(getKnightMoves(r, c)); break;
            case 'b': moves.addAll(getBishopMoves(r, c)); break;
            case 'q': moves.addAll(getQueenMoves(r, c)); break;
            case 'k': moves.addAll(getKingMoves(r, c, pieceColor)); break;
        }
        return moves;
    }

    private List<Point> generateAllValidMoves(boolean isWhite) {
        List<Point> allMoves = new ArrayList<>();
        char color = isWhite ? 'w' : 'b';
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (!board[r][c].isEmpty() && board[r][c].charAt(0) == color) {
                    // We need to store the start point as well
                    List<Point> pieceMoves = generateValidMovesForPiece(r, c);
                    for (Point move : pieceMoves) {
                        allMoves.add(new Point(r, c)); // Store start pos
                        allMoves.add(move); // Store end pos
                    }
                }
            }
        }
        return allMoves;
    }
    
    private void checkGameState() {
        boolean currentPlayerHasMoves = !generateAllValidMoves(whiteTurn).isEmpty();
        if (!currentPlayerHasMoves) {
            if (isKingInCheck(whiteTurn)) {
                endGame(whiteTurn ? "Black wins by Checkmate!" : "White wins by Checkmate!");
            } else {
                endGame("Stalemate! It's a draw.");
            }
        }
    }

    private void endGame(String message) {
        JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        // Disable all buttons
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                squares[r][c].setEnabled(false);
            }
        }
    }


    // --- AI ---

    private void makeComputerMove() {
        List<Point> allMoves = generateAllValidMoves(false); // Black's moves
        if (allMoves.isEmpty()) return;

        Point start = null, end = null;

        if (difficulty.equals("Easy")) {
            Random rand = new Random();
            int moveIndex = rand.nextInt(allMoves.size() / 2) * 2;
            start = allMoves.get(moveIndex);
            end = allMoves.get(moveIndex + 1);
        } else if (difficulty.equals("Medium")) {
            // Minimax with depth 1 (choose best immediate outcome)
            int bestScore = Integer.MIN_VALUE;
            for (int i = 0; i < allMoves.size(); i += 2) {
                Point currentStart = allMoves.get(i);
                Point currentEnd = allMoves.get(i + 1);

                // Simulate the move
                String captured = board[currentEnd.x][currentEnd.y];
                board[currentEnd.x][currentEnd.y] = board[currentStart.x][currentStart.y];
                board[currentStart.x][currentStart.y] = "";

                int score = -evaluateBoard(); // Evaluate from opponent's (now white's) perspective

                // Undo the move
                board[currentStart.x][currentStart.y] = board[currentEnd.x][currentEnd.y];
                board[currentEnd.x][currentEnd.y] = captured;

                if (score > bestScore) {
                    bestScore = score;
                    start = currentStart;
                    end = currentEnd;
                }
            }
        }

        if (start != null && end != null) {
            movePiece(start.x, start.y, end.x, end.y);
        }
    }

    private int getPieceValue(char pieceType) {
        switch (pieceType) {
            case 'p': return 10;
            case 'n': return 30;
            case 'b': return 30;
            case 'r': return 50;
            case 'q': return 90;
            case 'k': return 900;
            default: return 0;
        }
    }

    private int evaluateBoard() {
        int score = 0;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (!board[r][c].isEmpty()) {
                    char color = board[r][c].charAt(0);
                    char type = board[r][c].charAt(1);
                    int value = getPieceValue(type);
                    if (color == 'w') {
                        score += value;
                    } else {
                        score -= value;
                    }
                }
            }
        }
        return score;
    }

    // --- UI Helpers ---

    private void refreshBoardColors() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                squares[r][c].setBackground((r + c) % 2 == 0 ? lightColor : darkColor);
            }
        }
    }

    private void highlightValidMoves(int r, int c) {
        refreshBoardColors();
        squares[r][c].setBackground(selectedColor);
        List<Point> moves = generateValidMovesForPiece(r, c);
        for (Point move : moves) {
            squares[move.x][move.y].setBackground(validMoveColor);
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessGame::new);
    }
}