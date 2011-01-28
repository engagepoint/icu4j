//##header J2SE15
/*********************************************************************
 * Copyright (C) 2000-2007, International Business Machines Corporation and
 * others. All Rights Reserved.
 *********************************************************************
 */
package com.ibm.icu.text;
import com.ibm.icu.util.*;
import com.ibm.icu.impl.Utility;

import java.io.InvalidObjectException;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Locale;

/**
 * A concrete {@link DateFormat} for {@link com.ibm.icu.util.ChineseCalendar}.
 * This class handles a <code>ChineseCalendar</code>-specific field,
 * <code>ChineseCalendar.IS_LEAP_MONTH</code>.  It also redefines the
 * handling of two fields, <code>ERA</code> and <code>YEAR</code>.  The
 * former is displayed numerically, instead of symbolically, since it is
 * the numeric cycle number in <code>ChineseCalendar</code>.  The latter is
 * numeric, as before, but has no special 2-digit Y2K behavior.
 *
 * <p>With regard to <code>ChineseCalendar.IS_LEAP_MONTH</code>, this
 * class handles parsing specially.  If no string symbol is found at all,
 * this is taken as equivalent to an <code>IS_LEAP_MONTH</code> value of
 * zero.  This allows formats to display a special string (e.g., "*") for
 * leap months, but no string for normal months.
 *
 * <p>Summary of field changes vs. {@link SimpleDateFormat}:<pre>
 * Symbol   Meaning                 Presentation        Example
 * ------   -------                 ------------        -------
 * G        cycle                   (Number)            78
 * y        year of cycle (1..60)   (Number)            17
 * l        is leap month           (Text)              4637
 * </pre>
 *
 * @see com.ibm.icu.util.ChineseCalendar
 * @see ChineseDateFormatSymbols
 * @author Alan Liu
 * @stable ICU 2.0
 */
public class ChineseDateFormat extends SimpleDateFormat {
    // Generated by serialver from JDK 1.4.1_01
    static final long serialVersionUID = -4610300753104099899L;
    
    // TODO Finish the constructors

    /**
     * Construct a ChineseDateFormat from a date format pattern and locale
     * @param pattern the pattern
     * @param locale the locale
     * @stable ICU 2.0
     */
   public ChineseDateFormat(String pattern, Locale locale) {
       this(pattern, ULocale.forLocale(locale));
    }

    /**
     * Construct a ChineseDateFormat from a date format pattern and locale
     * @param pattern the pattern
     * @param locale the locale
     * @stable ICU 3.2
     */
   public ChineseDateFormat(String pattern, ULocale locale) {
       super(pattern, new ChineseDateFormatSymbols(locale), 
               new ChineseCalendar(TimeZone.getDefault(), locale), locale, true);
    }

// NOTE: This API still exists; we just inherit it from SimpleDateFormat
// as of ICU 3.0
//  /**
//   * @stable ICU 2.0
//   */
//  protected String subFormat(char ch, int count, int beginOffset,
//                             FieldPosition pos, DateFormatSymbols formatData,
//                             Calendar cal)  {
//      switch (ch) {
//      case 'G': // 'G' - ERA
//          return zeroPaddingNumber(cal.get(Calendar.ERA), 1, 9);
//      case 'l': // 'l' - IS_LEAP_MONTH
//          {
//              ChineseDateFormatSymbols symbols =
//                  (ChineseDateFormatSymbols) formatData;
//              return symbols.getLeapMonth(cal.get(
//                             ChineseCalendar.IS_LEAP_MONTH));
//          }
//      default:
//          return super.subFormat(ch, count, beginOffset, pos, formatData, cal);
//      }
//  }    

    /**
     * {@inheritDoc}
     * @internal
     * @deprecated This API is ICU internal only.
     */
    protected void subFormat(StringBuffer buf,
                             char ch, int count, int beginOffset,
                             FieldPosition pos,
                             Calendar cal) {
        switch (ch) {
        case 'G': // 'G' - ERA
            zeroPaddingNumber(buf, cal.get(Calendar.ERA), 1, 9);
            break;
        case 'l': // 'l' - IS_LEAP_MONTH
            buf.append(((ChineseDateFormatSymbols) getSymbols()).
                       getLeapMonth(cal.get(ChineseCalendar.IS_LEAP_MONTH)));
            break;
        default:
            super.subFormat(buf, ch, count, beginOffset, pos, cal);
            break;
        }

        // TODO: add code to set FieldPosition for 'G' and 'l' fields. This
        // is a DESIGN FLAW -- subclasses shouldn't have to duplicate the
        // code that handles this at the end of SimpleDateFormat.subFormat.
        // The logic should be moved up into SimpleDateFormat.format.
    }

    /**
     * {@inheritDoc}
     * @stable ICU 2.0
     */
    protected int subParse(String text, int start, char ch, int count,
                           boolean obeyCount, boolean allowNegative, boolean[] ambiguousYear, Calendar cal) {
        if (ch != 'G' && ch != 'l' && ch != 'y') {
            return super.subParse(text, start, ch, count, obeyCount, allowNegative, ambiguousYear, cal);
        }

        // Skip whitespace
        start = Utility.skipWhitespace(text, start);

        ParsePosition pos = new ParsePosition(start);

        switch (ch) {
        case 'G': // 'G' - ERA
        case 'y': // 'y' - YEAR, but without the 2-digit Y2K adjustment
            {
                Number number = null;
                if (obeyCount) {
                    if ((start+count) > text.length()) {
                        return -start;
                    }
                    number = numberFormat.parse(text.substring(0, start+count), pos);
                } else {
                    number = numberFormat.parse(text, pos);
                }
                if (number == null) {
                    return -start;
                }
                int value = number.intValue();
                cal.set(ch == 'G' ? Calendar.ERA : Calendar.YEAR, value);
                return pos.getIndex();
            }
        case 'l': // 'l' - IS_LEAP_MONTH
            {
                ChineseDateFormatSymbols symbols =
                    (ChineseDateFormatSymbols) getSymbols();
                int result = matchString(text, start, ChineseCalendar.IS_LEAP_MONTH,
                                         symbols.isLeapMonth, cal);
                // Treat the absence of any matching string as setting
                // IS_LEAP_MONTH to false.
                if (result<0) {
                    cal.set(ChineseCalendar.IS_LEAP_MONTH, 0);
                    result = start;
                }
                return result;
            }
        default:
            ///CLOVER:OFF
            return 0; // This can never happen
            ///CLOVER:ON
        }
    }

//#if defined(FOUNDATION10) || defined(J2SE13)
//#else
    /**
     * {@inheritDoc}
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    protected DateFormat.Field patternCharToDateFormatField(char ch) {
        if (ch == 'l') {
            return ChineseDateFormat.Field.IS_LEAP_MONTH;
        }
        return super.patternCharToDateFormatField(ch);
    }

    /**
     * The instances of this inner class are used as attribute keys and values
     * in AttributedCharacterIterator that
     * ChineseDateFormat.formatToCharacterIterator() method returns.
     * <p>
     * There is no public constructor to this class, the only instances are the
     * constants defined here.
     * <p>
     * @stable ICU 3.8
     */
    public static class Field extends DateFormat.Field {

        private static final long serialVersionUID = -5102130532751400330L;

        /**
         * Constant identifying the leap month marker.
         * @stable ICU 3.8
         */
        public static final Field IS_LEAP_MONTH = new Field("is leap month", ChineseCalendar.IS_LEAP_MONTH);

        /**
         * Constructs a <code>ChineseDateFormat.Field</code> with the given name and
         * the <code>ChineseCalendar</code> field which this attribute represents.
         * Use -1 for <code>calendarField</code> if this field does not have a
         * corresponding <code>ChineseCalendar</code> field.
         * 
         * @param name          Name of the attribute
         * @param calendarField <code>Calendar</code> field constant
         * 
         * @stable ICU 3.8
         */
        protected Field(String name, int calendarField) {
            super(name, calendarField);
        }

        /**
         * Returns the <code>Field</code> constant that corresponds to the <code>
         * ChineseCalendar</code> field <code>calendarField</code>.  If there is no
         * corresponding <code>Field</code> is available, null is returned.
         * 
         * @param calendarField <code>ChineseCalendar</code> field constant
         * @return <code>Field</code> associated with the <code>calendarField</code>,
         * or null if no associated <code>Field</code> is available.
         * @throws IllegalArgumentException if <code>calendarField</code> is not
         * a valid <code>Calendar</code> field constant.
         * 
         * @stable ICU 3.8
         */
        public static DateFormat.Field ofCalendarField(int calendarField) {
            if (calendarField == ChineseCalendar.IS_LEAP_MONTH) {
                return IS_LEAP_MONTH;
            }
            return DateFormat.Field.ofCalendarField(calendarField);
        }

        /**
         * {@inheritDoc}
         * 
         * @stable ICU 3.8
         */
        protected Object readResolve() throws InvalidObjectException {
            if (this.getClass() != ChineseDateFormat.Field.class) {
                throw new InvalidObjectException("A subclass of ChineseDateFormat.Field must implement readResolve.");
            }
            if (this.getName().equals(IS_LEAP_MONTH.getName())) {
                return IS_LEAP_MONTH;
            } else {
                throw new InvalidObjectException("Unknown attribute name.");
            }
        }
    }
//#endif
}