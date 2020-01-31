package tablut;

import org.junit.Test;
import static org.junit.Assert.*;
import static tablut.Piece.BLACK;

import ucb.junit.textui;

/** The suite of all JUnit tests for the enigma package.
 *  @author Ryan Chen
 */
public class UnitTest {



    /** Run the JUnit tests in this package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /** A dummy test as a placeholder for real ones. */
    @Test
    public void boardTest() {
        Board board = new Board();
        assertEquals(board.turn(), BLACK);
    }

    @Test
    public void undoTest() {
        Board board = new Board();
        board.init();
        board.makeMove(Move.mv("d1-c"));
        board.makeMove(Move.mv("e4-c"));
        board.makeMove(Move.mv("f1-i"));
        board.undo();
        assertEquals(board.get(8, 0), Piece.EMPTY);
        assertEquals(board.get(5, 0), Piece.BLACK);
    }

}


