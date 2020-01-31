package tablut;

import java.util.Formatter;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.ArrayList;
import java.util.HashMap;
import static tablut.Move.ROOK_MOVES;
import static tablut.Piece.*;
import static tablut.Square.*;


/** The state of a Tablut Game.
 *  @author Ryan Chen
 */
class Board {

    /** The initial limit of the board. */
    static final int INITIAL_LIMIT = 10000;

    /** The number of squares on a side of the board. */
    static final int SIZE = 9;

    /** The throne (or castle) square and its four surrounding squares.. */
    static final Square THRONE = sq(4, 4),
        NTHRONE = sq(4, 5),
        STHRONE = sq(4, 3),
        WTHRONE = sq(3, 4),
        ETHRONE = sq(5, 4);

    /** The position of the castle. */
    static final Square[] SURROUNDING_THRONE = {NTHRONE,
        STHRONE, WTHRONE, ETHRONE};

    /** Initial positions of attackers. */
    static final Square[] INITIAL_ATTACKERS = {
        sq(0, 3), sq(0, 4), sq(0, 5), sq(1, 4),
        sq(8, 3), sq(8, 4), sq(8, 5), sq(7, 4),
        sq(3, 0), sq(4, 0), sq(5, 0), sq(4, 1),
        sq(3, 8), sq(4, 8), sq(5, 8), sq(4, 7)
    };

    /** Initial positions of defenders of the king. */
    static final Square[] INITIAL_DEFENDERS = {
        NTHRONE, ETHRONE, STHRONE, WTHRONE,
        sq(4, 6), sq(4, 2), sq(2, 4), sq(6, 4)
    };


    /** Initializes a game board with SIZE squares on a side in the
     *  initial position. */
    Board() {
        init();
    }


    /** Initializes a copy of MODEL. */
    Board(Board model) {
        copy(model);
    }

    /** Copies MODEL into me. */
    void copy(Board model) {
        if (model == this) {
            return;
        }
        init();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                Square place = sq(i, j);
                Piece piece = model.get(place);
                put(piece, place);
            }
        }
        this.undoStack.addAll(model.undoStack);
        this.boardPositions.addAll(model.boardPositions);
        this._repeated = model._repeated;
        this._moveCount = model._moveCount;
        this._turn = model._turn;
        _winner = model._winner;
        _lim = model._lim;
        this.updateKingPosition(model._kingPosition);

    }

    /** Clears the board to the initial position. */
    void init() {
        updateKingPosition(THRONE);
        _winner = null;
        _turn = BLACK;
        _moveCount = 0;
        _repeated = false;
        _lim = INITIAL_LIMIT;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                Square place = sq(i, j);
                put(EMPTY, place);
            }
        }
        for (Square j : INITIAL_DEFENDERS) {
            put(WHITE, j);
        }
        for (Square i : INITIAL_ATTACKERS) {
            put(BLACK, i);
        }
        put(KING, THRONE);
    }

    /** Set the move limit to LIM.  It is an error if 2*LIM <= moveCount().
     * @param n is the move limit */
    void setMoveLimit(int n) {
        if (2 * n <= moveCount()) {
            throw new IllegalArgumentException("Move limit is too great");
        }
        _lim = n;
    }

    /** Checks the move limit. */
    void checkMoveLimit() {
        if (2 * _lim <= moveCount()) {
            throw new IllegalArgumentException("Move limit is too great");
        }
    }

    /** Return a Piece representing whose move it is (WHITE or BLACK). */
    Piece turn() {
        return _turn;
    }

    /** Return the winner in the current position, or null if there is no winner
     *  yet. */
    Piece winner() {
        return _winner;
    }

    /** Returns true iff this is a win due to a repeated position. */
    boolean repeatedPosition() {
        return _repeated;
    }

    /** Record current position and set winner() next mover if the current
     *  position is a repeat. */
    private void checkRepeated() {
        if (boardPositions.contains(this.encodedBoard())) {
            _repeated = true;
            if (_turn == WHITE) {
                setWinner(WHITE);
            } else if (_turn == BLACK) {
                setWinner(BLACK);
            }
        }
        boardPositions.add(this.encodedBoard());
    }

    /** Record current position and set winner() next mover if the current
     *  position is a repeat.
     *  @param side is the side being evaluated.*/
    private void checkWinner(Piece side) {
        if (_winner == BLACK || _winner == WHITE) {
            return;
        }
        if (kingPosition().isEdge()) {
            setWinner(WHITE);
        } else if (!hasMove(side)) {
            setWinner(side.opponent());
        }
    }

    /** Return the number of moves since the initial position that have not been
     *  undone. */
    final int moveCount() {
        return _moveCount;
    }

    /** Updates the King's position.
     * @param to is the position where the king is now */
    void updateKingPosition(Square to) {
        _kingPosition = to;
    }


    /** Return location of the king. */
    Square kingPosition() {
        return _kingPosition;
    }

    /** Return the contents the square at S. */
    final Piece get(Square s) {
        return get(s.col(), s.row());
    }

    /** Return the contents of the square at (COL, ROW), where
     *  0 <= COL, ROW <= 9. */
    final Piece get(int col, int row) {
        return tablet[col][row];
    }


    /** Return the contents of the square at COL ROW. */
    final Piece get(char col, char row) {
        return get(col - 'a', row - '1');
    }

    /** Set square S to P. */
    final void put(Piece p, Square s) {
        tablet[s.col()][s.row()] = p;
    }

    /** Set square S to P and record for undoing. */
    final void revPut(Piece p, Square s) {
        HashMap<Piece, Square> undo = new HashMap<>();
        undo.put(get(s), s);
        put(p, s);
        undoStack.push(undo);
    }

    /** Insert null in the stack. */
    final void revPut() {
        undoStack.push(null);
    }

    /** Set square COL ROW to P. */
    final void put(Piece p, char col, char row) {
        put(p, sq(col - 'a', row - '1'));
    }

    /** Return true iff FROM - TO is an unblocked rook move on the current
     *  board.  For this to be true, FROM-TO must be a rook move and the
     *  squares along it, other than FROM, must be empty. */
    boolean isUnblockedMove(Square from, Square to) {
        if (get(from) != KING && to == THRONE) {
            return false;
        }
        if (from.isRookMove(to)) {
            int direction = from.direction(to);
            int index = from.index();
            for (Square sq : ROOK_SQUARES[index][direction]) {
                if (get(sq) != EMPTY) {
                    return false;
                }
                if (sq == to) {
                    return true;
                }
            }
        }
        return false;
    }


    /** Return true iff FROM is a valid starting square for a move. */
    boolean isLegal(Square from) {
        return get(from).side() == _turn;
    }

    /** Return true iff MOVE is a legal move in the current
     *  position. */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to());
    }

    /** Return true iff FROM-TO is a valid move. */
    boolean isLegal(Square from, Square to) {
        return (isLegal(from) && isUnblockedMove(from, to));
    }

    /** Move FROM-TO, assuming this is a legal move. */
    void makeMove(Square from, Square to) {
        assert isLegal(from, to);
        checkMoveLimit();
        Piece side = get(from).side();
        if (get(from) == KING) {
            updateKingPosition(to);
        }
        this.revPut();
        this.revPut(get(from), to);
        this.revPut(EMPTY, from);
        checkCapture(to);
        _moveCount++;
        boardPositions.add(this.encodedBoard());
        switchTurns();
        checkRepeated();
        checkWinner(side);
    }

    /** Check squares for pieces that are eligible to be captured.
     * @param to is the square being checked */
    void checkCapture(Square to) {
        boolean isThroneHostile = false; Square sq0 = to;
        ArrayList<Square> squares = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (ROOK_SQUARES[to.index()][i].size() >= 2) {
                squares.add(ROOK_SQUARES[to.index()][i].get(1));
            }
        }
        switch (get(to).side()) {
        case BLACK:
            if (kingPosition() != THRONE) {
                isThroneHostile = true;
            } else if (kingPosition() == THRONE) {
                int count = 0;
                for (Square s : SURROUNDING_THRONE) {
                    if (get(s).side() == BLACK) {
                        count++;
                    }
                }
                if (count >= 3) {
                    isThroneHostile = true;
                }
            }
            boolean kingInCastle = kingInCastle();
            for (Square sq2 : squares) {
                Square sq1 = sq0.between(sq2);
                if ((get(sq1) == KING) && kingInCastle) {
                    Square[] temp = {sq0.diag1(sq2), sq0.diag2(sq2), sq2, sq0};
                    int check = 0;
                    for (Square sq : temp) {
                        if (get(sq).side() == BLACK
                                || (sq == THRONE && isThroneHostile)) {
                            check++;
                        }
                    }
                    if (check == 4) {
                        capture(sq0, sq2);
                    }
                } else {
                    if ((get(sq0).side() == get(sq2).side()
                            || (sq2 == THRONE && isThroneHostile))
                            && (get(sq0).opponent() == get(sq1).side())) {
                        capture(sq0, sq2);
                    }
                }
            }
            break;
        case WHITE:
            for (Square sq2 : squares) {
                Square sq1 = sq0.between(sq2);
                if (get(sq0).side() == get(sq2).side() || (sq2 == THRONE)) {
                    if (get(sq0).opponent() == get(sq1).side()) {
                        capture(sq0, sq2);
                    }
                }
            }
            break;
        default:
        }
    }

    /** Returns true if King is in the castle. */
    boolean kingInCastle() {
        return kingPosition() == THRONE || kingPosition() == NTHRONE
                || kingPosition() == STHRONE || kingPosition() == ETHRONE
                || kingPosition() == WTHRONE;
    }

    /** Switches turns. */
    void switchTurns() {
        if (_turn == BLACK) {
            _turn = WHITE;
        } else {
            _turn = BLACK;
        }
    }

    /** Move according to MOVE, assuming it is a legal move. */
    void makeMove(Move move) {
        makeMove(move.from(), move.to());
    }

    /** Capture the piece between SQ0 and SQ2, assuming a piece just moved to
     *  SQ0 and the necessary conditions are satisfied. */
    private void capture(Square sq0, Square sq2) {
        Square sq1 = sq0.between(sq2);
        if (get(sq1) == KING) {
            setWinner(BLACK);
        }
        revPut(EMPTY, sq1);
    }

    /** Undo one move. Has no effect on the initial board. */
    void undo() {
        if (_moveCount > 0) {
            undoPosition();
            while (undoStack.peek() != null) {
                HashMap<Piece, Square> undo = undoStack.pop();
                Piece p = null;
                if (undo.containsKey(BLACK)) {
                    p = BLACK;
                } else if (undo.containsKey(WHITE)) {
                    p = WHITE;
                } else if (undo.containsKey(EMPTY)) {
                    p = EMPTY;
                } else if (undo.containsKey(KING)) {
                    p = KING;
                }
                Square sq = undo.get(p);
                if (p == KING) {
                    updateKingPosition(sq);
                }
                put(p, sq);
            }
            if (undoStack.peek() == null) {
                undoStack.pop();
            }
            setWinner(null);
            _repeated = false;
            _moveCount--;
            switchTurns();
        }
    }

    /** Remove record of current position in the set of positions encountered,
     *  unless it is a repeated position or we are at the first move. */
    private void undoPosition() {
        if (_repeated) {
            return;
        }
        boardPositions.remove(this.encodedBoard());
    }

    /** Clear the undo stack and board-position counts. Does not modify the
     *  current position or win status. */
    void clearUndo() {
        undoStack.clear();
        boardPositions.clear();
    }

    /** Return a new mutable list of all legal moves on the current board for
     *  SIDE (ignoring whose turn it is at the moment). */
    List<Move> legalMoves(Piece side) {
        List<Move> legalMoves = new ArrayList<>();
        for (Square sq: pieceLocations(side)) {
            for (int dir = 0; dir < 4; dir++) {
                for (Move move : ROOK_MOVES[sq.index()][dir]) {
                    if (isUnblockedMove(move.from(), move.to())) {
                        legalMoves.add(move);
                    }
                }
            }
        }
        return legalMoves;
    }

    /** Return the total number of legal moves. */
    int totalLegalMoves() {
        return legalMoves(_turn).size();
    }

    /** Return true iff SIDE has a legal move. */
    boolean hasMove(Piece side) {
        if (legalMoves(side).isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /** Return a text representation of this Board.  If COORDINATES, then row
     *  and column designations are included along the left and bottom sides.
     */
    String toString(boolean coordinates) {
        Formatter out = new Formatter();
        for (int r = SIZE - 1; r >= 0; r -= 1) {
            if (coordinates) {
                out.format("%2d", r + 1);
            } else {
                out.format("  ");
            }
            for (int c = 0; c < SIZE; c += 1) {
                out.format(" %s", get(c, r));
            }
            out.format("%n");
        }
        if (coordinates) {
            out.format("  ");
            for (char c = 'a'; c <= 'i'; c += 1) {
                out.format(" %c", c);
            }
            out.format("%n");
        }
        return out.toString();
    }

    /** Return the locations of all pieces on SIDE. */
    HashSet<Square> pieceLocations(Piece side) {
        assert side != EMPTY;
        HashSet<Square> locations = new HashSet<>();
        for (Square sq : SQUARE_LIST) {
            if (this.get(sq).side() == side.side()) {
                locations.add(sq);
            }
        }
        return locations;
    }

    /** Return how many routes the king has to edge. */
    final int kingToEdge() {
        int counter = 0;
        if (isLegal(_kingPosition, sq(_kingPosition.col(), 0))) {
            counter++;
        }
        if (isLegal(_kingPosition, sq(_kingPosition.col(), 8))) {
            counter++;
        }
        if (isLegal(_kingPosition, sq(0, _kingPosition.row()))) {
            counter++;
        }
        if (isLegal(_kingPosition, sq(8, _kingPosition.row()))) {
            counter++;
        }
        return counter;
    }
    /** Return number of black pieces. */
    public int getNumBlackPieces() {
        return pieceLocations(BLACK).size();
    }
    /** Return number of black pieces. */
    public int getNumWhitePieces() {
        return pieceLocations(WHITE).size();
    }

    /** Sets the winner of the game.
     * @param winner is the winner.*/
    public void setWinner(Piece winner) {
        this._winner = winner;
    }

    /** Return the contents of _board in the order of SQUARE_LIST as a sequence
     *  of characters: the toString values of the current turn and Pieces. */
    String encodedBoard() {
        char[] result = new char[Square.SQUARE_LIST.size() + 1];
        result[0] = turn().toString().charAt(0);
        for (Square sq : SQUARE_LIST) {
            result[sq.index() + 1] = get(sq).toString().charAt(0);
        }
        return new String(result);
    }
    /** Return the number of defenders of the king. */
    int numDefenders() {
        int count = 0;
        for (int d = 0; d < 4; d++) {
            for (Square sq : ROOK_SQUARES[this.kingPosition().index()][d]) {
                if (!isUnblockedMove(kingPosition(), sq)) {
                    if (get(sq).side() == WHITE) {
                        count++;
                    }
                    break;
                }
            }
        }
        return count;
    }

    /** The tablet representation of the board. */
    private Piece[][] tablet = new Piece[SIZE][SIZE];
    /** The undo stack. */
    private Stack<HashMap<Piece, Square>> undoStack = new Stack<>();
    /** The positions of the board. */
    private HashSet<String> boardPositions = new HashSet<>();
    /** Piece whose turn it is (WHITE or BLACK). */
    private Piece _turn;
    /** Cached value of winner on this board, or EMPTY if it has not been
     *  computed. */
    private Piece _winner;
    /** Number of (still undone) moves since initial position. */
    private int _moveCount;
    /** True when current board is a repeated position (ending the game). */
    private boolean _repeated;
    /** The move limit of the game. */
    private int _lim;
    /** Keeps Track of the King Position. */
    private Square _kingPosition;

}
