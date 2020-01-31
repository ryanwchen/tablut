package tablut;

import org.junit.Test;
import static org.junit.Assert.*;
import ucb.junit.textui;
import java.util.List;

/** Junit tests for our Tablut Board class.
 *  @author Vivant Sakore
 */
public class TablutTests {

    /** Run the JUnit tests in this package. */
    public static void main(String[] ignored) {
        textui.runClasses(TablutTests.class);
    }

    /**
     * Tests legalMoves for white pieces to make sure it
     * returns all legal Moves.
     * This method needs to be finished and may need to be changed
     * based on your implementation.
     */
    @Test
    public void testLegalWhiteMoves() {
        Board b = new Board();
        List<Move> movesList = b.legalMoves(Piece.WHITE);

        assertEquals(56, movesList.size());

        assertFalse(movesList.contains(Move.mv("e7-8")));
        assertFalse(movesList.contains(Move.mv("e8-f")));

        assertTrue(movesList.contains(Move.mv("e6-f")));
        assertTrue(movesList.contains(Move.mv("f5-8")));

    }

    /**
     * Tests legalMoves for black pieces to make sure it
     * returns all legal Moves.
     * This method needs to be finished and may need to be changed
     * based on your implementation.
     */
    @Test
    public void testLegalBlackMoves() {
        Board b = new Board();

        List<Move> movesList = b.legalMoves(Piece.BLACK);

        assertEquals(80, movesList.size());

        assertFalse(movesList.contains(Move.mv("e8-7")));
        assertFalse(movesList.contains(Move.mv("e7-8")));

        assertTrue(movesList.contains(Move.mv("f9-i")));
        assertTrue(movesList.contains(Move.mv("h5-1")));

    }
}
