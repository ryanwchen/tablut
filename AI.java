package tablut;

import static java.lang.Math.*;
import static tablut.Board.*;
import static tablut.Piece.*;


/** A Player that automatically generates moves.
 *  @author Ryan Chen
 */
class AI extends Player {

    /** A position-score magnitude indicating a win (for white if positive,
     *  black if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A position-score magnitude indicating a forced win in a subsequent
     *  move.  This differs from WINNING_VALUE to avoid putting off wins. */
    private static final int WILL_WIN_VALUE = Integer.MAX_VALUE  - 40;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;
    /** A magnitude greater than a normal value. */
    private static final int NEG_INFTY = Integer.MIN_VALUE;
    /** 1st level move limit. */
    private static final int FIRSTLEVEL = 6;
    /** 2nd level move limit. */
    private static final int SECONDLEVEL = 20;
    /** The weight of the black pieces. */
    private static final int BLACKPIECESWEIGHT = 20;
    /** The weight. */
    private static final int WEIGHT = 20;

    /** A new AI with no piece or controller (intended to produce
     *  a template). */
    AI() {
        this(null, null);
    }

    /** A new AI playing PIECE under control of CONTROLLER. */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }



    @Override
    String myMove() {
        Move move = findMove();
        _controller.reportMove(move);
        return move.toString();
    }

    @Override
    boolean isManual() {
        return false;
    }


    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(board());
        _lastFoundMove = null;
        int sense = 0;
        if (myPiece() == BLACK) {
            sense = -1;
        } else if (myPiece() == WHITE) {
            sense = 1;
        }
        maxDepth = maxDepth(b);
        findMove(b, maxDepth, true, sense, NEG_INFTY, INFTY);
        return _lastFoundMove;
    }

    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;
    /** The maximum depth. */
    private int maxDepth;

    /** Helper function that returns the score of a board.
     * @param board is the given board
     * @param depth is the current depth.*/
    private int simpleScore(Board board, int depth) {
        if (board.winner() == WHITE) {
            if (depth == maxDepth - 1) {
                return WINNING_VALUE;
            }
            return WILL_WIN_VALUE + depth;
        } else if (board.winner() == BLACK) {
            if (depth == maxDepth - 1) {
                return -1 * WINNING_VALUE;
            }
            return -1 * WILL_WIN_VALUE - depth;
        } else {
            return staticScore(board);
        }
    }

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _lastMoveFound. */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        if (depth == 0 || board.winner() != null) {
            return simpleScore(board, depth);
        }
        if (sense == 1) {
            Move bestSoFarMove = null;
            int bestSoFarInt = NEG_INFTY;
            for (Move mv : board.legalMoves(WHITE)) {
                board.makeMove(mv);
                int moveVal = findMove(board, depth - 1,
                        false, -1, alpha, beta);
                board.undo();
                if (moveVal >= bestSoFarInt) {
                    bestSoFarMove = mv;
                    bestSoFarInt = moveVal;
                    alpha = max(alpha, moveVal);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            if (saveMove) {
                _lastFoundMove = bestSoFarMove;
            }
            return bestSoFarInt;
        } else if (sense == -1) {
            Move bestSoFarMove1 = null;
            int bestSoFarInt1 = INFTY;
            for (Move mv : board.legalMoves(BLACK)) {
                board.makeMove(mv);
                int moveVal = findMove(board, depth - 1, false, 1, alpha, beta);
                board.undo();
                if (moveVal <= bestSoFarInt1) {
                    bestSoFarMove1 = mv;
                    bestSoFarInt1 = moveVal;
                    beta = min(beta, moveVal);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            if (saveMove) {
                _lastFoundMove = bestSoFarMove1;
            }
            return bestSoFarInt1;
        }
        return 0;
    }

    /** Return a heuristically determined maximum search depth
     *  based on characteristics of BOARD. */
    private static int maxDepth(Board board) {
        if (board.moveCount() < WEIGHT) {
            return 4;
        }
        return 5;
    }

    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
        return 0;
    }
}

