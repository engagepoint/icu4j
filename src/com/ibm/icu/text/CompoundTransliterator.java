package com.ibm.text;

import java.util.Enumeration;
import java.util.Vector;

/**
 * A transliterator that is composed of two or more other
 * transliterator objects linked together.  For example, if one
 * transliterator transliterates from script A to script B, and
 * another transliterates from script B to script C, the two may be
 * combined to form a new transliterator from A to C.
 *
 * <p>Composed transliterators may not behave as expected.  For
 * example, inverses may not combine to form the identity
 * transliterator.  See the class documentation for {@link
 * Transliterator} for details.
 *
 * <p>If a non-<tt>null</tt> <tt>UnicodeFilter</tt> is applied to a
 * <tt>CompoundTransliterator</tt>, it has the effect of being
 * logically <b>and</b>ed with the filter of each transliterator in
 * the chain.
 *
 * <p>Copyright &copy; IBM Corporation 1999.  All rights reserved.
 *
 * @author Alan Liu
 * @version $RCSfile: CompoundTransliterator.java,v $ $Revision: 1.2 $ $Date: 2000/01/18 02:30:49 $
 */
public class CompoundTransliterator extends Transliterator {

    private static final boolean DEBUG = false;

    private Transliterator[] trans;

    private static final String COPYRIGHT =
        "\u00A9 IBM Corporation 1999. All rights reserved.";

    /**
     * Constructs a new compound transliterator given an array of
     * transliterators.  The array of transliterators may be of any
     * length, including zero or one, however, useful compound
     * transliterators have at least two components.
     * @param transliterators array of <code>Transliterator</code>
     * objects
     * @param filter the filter.  Any character for which
     * <tt>filter.isIn()</tt> returns <tt>false</tt> will not be
     * altered by this transliterator.  If <tt>filter</tt> is
     * <tt>null</tt> then no filtering is applied.
     */
    public CompoundTransliterator(Transliterator[] transliterators,
                                  UnicodeFilter filter) {
        super(joinIDs(transliterators), filter);
        trans = new Transliterator[transliterators.length];
        System.arraycopy(transliterators, 0, trans, 0, trans.length);
    }

    /**
     * Constructs a new compound transliterator given an array of
     * transliterators.  The array of transliterators may be of any
     * length, including zero or one, however, useful compound
     * transliterators have at least two components.
     * @param transliterators array of <code>Transliterator</code>
     * objects
     */
    public CompoundTransliterator(Transliterator[] transliterators) {
        this(transliterators, null);
    }
    
    /**
     * Splits an ID of the form "ID;ID;..." into a compound using each
     * of the IDs. 
     * @param ID of above form
     * @param forward if false, does the list in reverse order, and
     * takes the inverse of each ID.
     */
    public CompoundTransliterator(String ID, int direction,
                                  UnicodeFilter filter) {
        // changed MED
        // Later, add "rule1[filter];rule2...
        super(ID, filter);
        String[] list = split(ID, ';');
        trans = new Transliterator[list.length];
        for (int i = 0; i < list.length; ++i) {
            trans[i] = getInstance(list[direction==FORWARD ? i : (list.length-1-i)],
                                   direction);
        }
    }
    
    public CompoundTransliterator(String ID, int direction) {
        this(ID, direction, null);
    }
    
    public CompoundTransliterator(String ID) {
        this(ID, FORWARD, null);
    }

    /**
     * Return the IDs of the given list of transliterators, concatenated
     * with ';' delimiting them.  Equivalent to the perlish expression
     * join(';', map($_.getID(), transliterators).
     */
    private static String joinIDs(Transliterator[] transliterators) {
        StringBuffer id = new StringBuffer();
        for (int i=0; i<transliterators.length; ++i) {
            if (i > 0) {
                id.append(';');
            }
            id.append(transliterators[i].getID());
        }
        return id.toString();
    }

    /**
     * Splits a string, as in JavaScript
     */
    private static String[] split(String s, char divider) {
        // changed MED

	    // see how many there are
	    int count = 1;
	    for (int i = 0; i < s.length(); ++i) {
	        if (s.charAt(i) == divider) ++count;
	    }
	    
	    // make an array with them
	    String[] result = new String[count];
	    int last = 0;
	    int current = 0;
	    int i;
	    for (i = 0; i < s.length(); ++i) {
	        if (s.charAt(i) == divider) {
	            result[current++] = s.substring(last,i);
	            last = i+1;
	        }
	    }
	    result[current++] = s.substring(last,i);
	    return result;
	}
    

    /**
     * Returns the number of transliterators in this chain.
     * @return number of transliterators in this chain.
     */
    public int getCount() {
        return trans.length;
    }

    /**
     * Returns the transliterator at the given index in this chain.
     * @param index index into chain, from 0 to <code>getCount() - 1</code>
     * @return transliterator at the given index
     */
    public Transliterator getTransliterator(int index) {
        return trans[index];
    }

    /**
     * Transliterates a segment of a string.  <code>Transliterator</code> API.
     * @param text the string to be transliterated
     * @param start the beginning index, inclusive; <code>0 <= start
     * <= limit</code>.
     * @param limit the ending index, exclusive; <code>start <= limit
     * <= text.length()</code>.
     * @return the new limit index
     */
    public int transliterate(Replaceable text, int start, int limit) {
        for (int i=0; i<trans.length; ++i) {
            limit = trans[i].transliterate(text, start, limit);
        }
        return limit;
    }

    /**
     * Implements {@link Transliterator#handleKeyboardTransliterate}.
     */
    protected void handleKeyboardTransliterate(Replaceable text,
                                               int[] index) {
        /* Call each transliterator with the same start value and
         * initial cursor index, but with the limit index as modified
         * by preceding transliterators.  The cursor index must be
         * reset for each transliterator to give each a chance to
         * transliterate the text.  The initial cursor index is known
         * to still point to the same place after each transliterator
         * is called because each transliterator will not change the
         * text between start and the initial value of cursor.
         *
         * IMPORTANT: After the first transliterator, each subsequent
         * transliterator only gets to transliterate text committed by
         * preceding transliterators; that is, the cursor (output
         * value) of transliterator i becomes the limit (input value)
         * of transliterator i+1.  Finally, the overall limit is fixed
         * up before we return.
         *
         * Assumptions we make here:
         * (1) start <= cursor <= limit    ;cursor valid on entry
         * (2) cursor <= cursor' <= limit' ;cursor doesn't move back
         * (3) cursor <= limit'            ;text before cursor unchanged
         * - cursor' is the value of cursor after calling handleKT
         * - limit' is the value of limit after calling handleKT
         */

        /**
         * Example: 3 transliterators.  This example illustrates the
         * mechanics we need to implement.  S, C, and L are the start,
         * cursor, and limit.  gl is the globalLimit.
         *
         * 1. h-u, changes hex to Unicode
         *
         *    4  7  a  d  0      4  7  a
         *    abc/u0061/u    =>  abca/u    
         *    S  C       L       S   C L   gl=f->a
         *
         * 2. upup, changes "x" to "XX"
         *
         *    4  7  a       4  7  a
         *    abca/u    =>  abcAA/u    
         *    S  CL         S    C   
         *                       L    gl=a->b
         * 3. u-h, changes Unicode to hex
         *
         *    4  7  a        4  7  a  d  0  3
         *    abcAA/u    =>  abc/u0041/u0041/u    
         *    S  C L         S              C
         *                                  L   gl=b->15
         * 4. return
         *
         *    4  7  a  d  0  3
         *    abc/u0041/u0041/u    
         *    S C L
         */

        /**
         * One more wrinkle.  If there is a filter F for the compound
         * transliterator as a whole, then we need to modify every
         * non-null filter f in the chain to be f' = F & f.  Then,
         * when we're done, we restore the original filters.
         *
         * A possible future optimization is to change f to f' at
         * construction time, but then if anyone else is using the
         * transliterators in the chain outside of this context, they
         * will get unexpected results.
         */
        UnicodeFilter F = getFilter();
        UnicodeFilter[] f = null;
        if (F != null) {
            f = new UnicodeFilter[trans.length];
            for (int i=0; i<f.length; ++i) {
                f[i] = trans[i].getFilter();
                trans[i].setFilter(UnicodeFilterLogic.and(F, f[i]));
            }
        }

        try {
            int cursor = index[CURSOR];
            int limit = index[LIMIT];
            int globalLimit = limit;
            /* globalLimit is the overall limit.  We keep track of this
             * since we overwrite index[LIMIT] with the previous
             * index[CURSOR].  After each transliteration, we update
             * globalLimit for insertions or deletions that have happened.
             */

            for (int i=0; i<trans.length; ++i) {
                index[CURSOR] = cursor; // Reset cursor
                index[LIMIT] = limit;

                if (DEBUG) {
                    System.out.print(escape(i + ": \"" +
                        substring(text, index[START], index[CURSOR]) + '|' +
                        substring(text, index[CURSOR], index[LIMIT]) +
                        "\" -> \""));
                }

                trans[i].handleKeyboardTransliterate(text, index);

                if (DEBUG) {
                    System.out.println(escape(
                        substring(text, index[START], index[CURSOR]) + '|' +
                        substring(text, index[CURSOR], index[LIMIT]) +
                        '"'));
                }
            
                // Adjust overall limit for insertions/deletions
                globalLimit += index[LIMIT] - limit;
                limit = index[CURSOR]; // Move limit to end of committed text
            }
            // Cursor is good where it is -- where the last
            // transliterator left it.  Limit needs to be put back
            // where it was, modulo adjustments for deletions/insertions.
            index[LIMIT] = globalLimit;

        } finally {
            // Fixup the transliterator filters, if we had to modify them.
            if (f != null) {
                for (int i=0; i<f.length; ++i) {
                    trans[i].setFilter(f[i]);
                }
            }
        }
    }

    /**
     * Returns the length of the longest context required by this transliterator.
     * This is <em>preceding</em> context.
     * @return maximum number of preceding context characters this
     * transliterator needs to examine
     */
    protected int getMaximumContextLength() {
        int max = 0;
        for (int i=0; i<trans.length; ++i) {
            int len = trans[i].getMaximumContextLength();
            if (len > max) {
                max = len;
            }
        }
        return max;
    }

    /**
     * DEBUG
     * Returns a substring of a Replaceable.
     */
    private static final String substring(Replaceable str, int start, int limit) {
        StringBuffer buf = new StringBuffer();
        while (start < limit) {
            buf.append(str.charAt(start++));
        }
        return buf.toString();
    }

    /**
     * DEBUG
     * Escapes non-ASCII characters as Unicode.
     */
    private static final String escape(String s) {
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<s.length(); ++i) {
            char c = s.charAt(i);
            if (c >= ' ' && c <= 0x007F) {
                buf.append(c);
            } else {
                buf.append("\\u");
                if (c < 0x1000) {
                    buf.append('0');
                    if (c < 0x100) {
                        buf.append('0');
                        if (c < 0x10) {
                            buf.append('0');
                        }
                    }
                }
                buf.append(Integer.toHexString(c));
            }
        }
        return buf.toString();
    }
}
