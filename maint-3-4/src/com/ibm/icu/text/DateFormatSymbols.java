/*
 *******************************************************************************
 * Copyright (C) 1996-2006, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package com.ibm.icu.text;

import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.CalendarData;
import com.ibm.icu.impl.TextTrieMap;
import com.ibm.icu.impl.Utility;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.UResourceBundle;
import com.ibm.icu.impl.ZoneMeta;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.impl.SoftCache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * <code>DateFormatSymbols</code> is a public class for encapsulating
 * localizable date-time formatting data, such as the names of the
 * months, the names of the days of the week, and the time zone data.
 * <code>DateFormat</code> and <code>SimpleDateFormat</code> both use
 * <code>DateFormatSymbols</code> to encapsulate this information.
 *
 * <p>
 * Typically you shouldn't use <code>DateFormatSymbols</code> directly.
 * Rather, you are encouraged to create a date-time formatter with the
 * <code>DateFormat</code> class's factory methods: <code>getTimeInstance</code>,
 * <code>getDateInstance</code>, or <code>getDateTimeInstance</code>.
 * These methods automatically create a <code>DateFormatSymbols</code> for
 * the formatter so that you don't have to. After the
 * formatter is created, you may modify its format pattern using the
 * <code>setPattern</code> method. For more information about
 * creating formatters using <code>DateFormat</code>'s factory methods,
 * see {@link DateFormat}.
 *
 * <p>
 * If you decide to create a date-time formatter with a specific
 * format pattern for a specific locale, you can do so with:
 * <blockquote>
 * <pre>
 * new SimpleDateFormat(aPattern, new DateFormatSymbols(aLocale)).
 * </pre>
 * </blockquote>
 *
 * <p>
 * <code>DateFormatSymbols</code> objects are clonable. When you obtain
 * a <code>DateFormatSymbols</code> object, feel free to modify the
 * date-time formatting data. For instance, you can replace the localized
 * date-time format pattern characters with the ones that you feel easy
 * to remember. Or you can change the representative cities
 * to your favorite ones.
 *
 * <p>
 * New <code>DateFormatSymbols</code> subclasses may be added to support
 * <code>SimpleDateFormat</code> for date-time formatting for additional locales.

 * @see          DateFormat
 * @see          SimpleDateFormat
 * @see          com.ibm.icu.util.SimpleTimeZone
 * @author       Chen-Lieh Huang
 * @stable ICU 2.0
 */
public class DateFormatSymbols implements Serializable, Cloneable {

    // TODO make sure local pattern char string is 18 characters long,
    // that is, that it encompasses the new 'u' char for
    // EXTENDED_YEAR.  Two options: 1. Make sure resource data is
    // correct; 2. Make code add in 'u' at end if len == 17.

    // Constants for context
    /**
     * Constant for context.
     * @draft ICU 3.6
     * @provisional This API might change or be removed in a future release.
     */
    public static final int FORMAT = 0;

    /**
     * Constant for context.
     * @draft ICU 3.6
     * @provisional This API might change or be removed in a future release.
     */
    public static final int STANDALONE = 1;

    /**
     * Constant for context.
     * @internal revisit for ICU 3.6
     * @provisional This API might change or be removed in a future release.
     */
    public static final int DT_CONTEXT_COUNT = 2;

    // Constants for width

    /**
     * Constant for width.
     * @draft ICU 3.6
     * @provisional This API might change or be removed in a future release.
     */
    public static final int ABBREVIATED = 0;

    /**
     * Constant for width.
     * @draft ICU 3.6
     * @provisional This API might change or be removed in a future release.
     */
    public static final int WIDE = 1;

    /**
     * Constant for width.
     * @draft ICU 3.6
     * @provisional This API might change or be removed in a future release.
     */
    public static final int NARROW = 2;

    /**
     * Constant for width.
     * @internal revisit for ICU 3.6
     * @provisional This API might change or be removed in a future release.
     */
    public static final int DT_WIDTH_COUNT = 3;

    /**
     * Construct a DateFormatSymbols object by loading format data from
     * resources for the default locale.
     *
     * @throws  java.util.MissingResourceException
     *          if the resources for the default locale cannot be
     *          found or cannot be loaded.
     * @stable ICU 2.0
     */
    public DateFormatSymbols()
    {
        initializeData(ULocale.getDefault(), ""); // TODO: type?
    }
    
    /**
     * Construct a DateFormatSymbols object by loading format data from
     * resources for the given locale.
     *
     * @throws  java.util.MissingResourceException
     *          if the resources for the specified locale cannot be
     *          found or cannot be loaded.
     * @stable ICU 2.0
     */
    public DateFormatSymbols(Locale locale)
    {
        initializeData(ULocale.forLocale(locale), ""); // TODO: type?
    }

    /**
     * Construct a DateFormatSymbols object by loading format data from
     * resources for the given ulocale.
     *
     * @throws  java.util.MissingResourceException
     *          if the resources for the specified locale cannot be
     *          found or cannot be loaded.
     * @draft ICU 3.2
     * @provisional This API might change or be removed in a future release.
     */
    public DateFormatSymbols(ULocale locale)
    {
        initializeData(locale, ""); // TODO: type?
    }

    /**
     * Era strings. For example: "AD" and "BC".  An array of 2 strings,
     * indexed by <code>Calendar.BC</code> and <code>Calendar.AD</code>.
     * @serial
     */
    String eras[] = null;

    /**
     * Era name strings. For example: "Anno Domini" and "Before Christ".  An array of 2 strings,
     * indexed by <code>Calendar.BC</code> and <code>Calendar.AD</code>.
     * @serial
     */
    String eraNames[] = null;

    /**
     * Month strings. For example: "January", "February", etc.  An array
     * of 13 strings (some calendars have 13 months), indexed by
     * <code>Calendar.JANUARY</code>, <code>Calendar.FEBRUARY</code>, etc.
     * @serial
     */
    String months[] = null;

    /**
     * Short month strings. For example: "Jan", "Feb", etc.  An array of
     * 13 strings (some calendars have 13 months), indexed by
     * <code>Calendar.JANUARY</code>, <code>Calendar.FEBRUARY</code>, etc.

     * @serial
     */
    String shortMonths[] = null;

    /**
     * Narrow month strings. For example: "J", "F", etc.  An array of
     * 13 strings (some calendars have 13 months), indexed by
     * <code>Calendar.JANUARY</code>, <code>Calendar.FEBRUARY</code>, etc.

     * @serial
     */
    String narrowMonths[] = null;

    /**
     * Standalone month strings. For example: "January", "February", etc.  An array
     * of 13 strings (some calendars have 13 months), indexed by
     * <code>Calendar.JANUARY</code>, <code>Calendar.FEBRUARY</code>, etc.
     * @serial
     */
    String standaloneMonths[] = null;

    /**
     * Standalone short month strings. For example: "Jan", "Feb", etc.  An array of
     * 13 strings (some calendars have 13 months), indexed by
     * <code>Calendar.JANUARY</code>, <code>Calendar.FEBRUARY</code>, etc.

     * @serial
     */
    String standaloneShortMonths[] = null;

    /**
     * Standalone narrow month strings. For example: "J", "F", etc.  An array of
     * 13 strings (some calendars have 13 months), indexed by
     * <code>Calendar.JANUARY</code>, <code>Calendar.FEBRUARY</code>, etc.

     * @serial
     */
    String standaloneNarrowMonths[] = null;

    /**
     * Weekday strings. For example: "Sunday", "Monday", etc.  An array
     * of 8 strings, indexed by <code>Calendar.SUNDAY</code>,
     * <code>Calendar.MONDAY</code>, etc.
     * The element <code>weekdays[0]</code> is ignored.
     * @serial
     */
    String weekdays[] = null;

    /**
     * Short weekday strings. For example: "Sun", "Mon", etc.  An array
     * of 8 strings, indexed by <code>Calendar.SUNDAY</code>,
     * <code>Calendar.MONDAY</code>, etc.
     * The element <code>shortWeekdays[0]</code> is ignored.
     * @serial
     */
    String shortWeekdays[] = null;

    /**
     * Narrow weekday strings. For example: "S", "M", etc.  An array
     * of 8 strings, indexed by <code>Calendar.SUNDAY</code>,
     * <code>Calendar.MONDAY</code>, etc.
     * The element <code>narrowWeekdays[0]</code> is ignored.
     * @serial
     */
    String narrowWeekdays[] = null;

    /**
     * Standalone weekday strings. For example: "Sunday", "Monday", etc.  An array
     * of 8 strings, indexed by <code>Calendar.SUNDAY</code>,
     * <code>Calendar.MONDAY</code>, etc.
     * The element <code>standaloneWeekdays[0]</code> is ignored.
     * @serial
     */
    String standaloneWeekdays[] = null;

    /**
     * Standalone short weekday strings. For example: "Sun", "Mon", etc.  An array
     * of 8 strings, indexed by <code>Calendar.SUNDAY</code>,
     * <code>Calendar.MONDAY</code>, etc.
     * The element <code>standaloneShortWeekdays[0]</code> is ignored.
     * @serial
     */
    String standaloneShortWeekdays[] = null;

    /**
     * Standalone narrow weekday strings. For example: "S", "M", etc.  An array
     * of 8 strings, indexed by <code>Calendar.SUNDAY</code>,
     * <code>Calendar.MONDAY</code>, etc.
     * The element <code>standaloneNarrowWeekdays[0]</code> is ignored.
     * @serial
     */
    String standaloneNarrowWeekdays[] = null;

    /**
     * AM and PM strings. For example: "AM" and "PM".  An array of
     * 2 strings, indexed by <code>Calendar.AM</code> and
     * <code>Calendar.PM</code>.
     * @serial
     */
    String ampms[] = null;

    /**
     * Localized names of time zones in this locale.  This is a
     * two-dimensional array of strings of size <em>n</em> by <em>m</em>,
     * where <em>m</em> is at least 5 and up to 8.  Each of the <em>n</em> rows is an
     * entry containing the localized names for a single <code>TimeZone</code>.
     * Each such row contains (with <code>i</code> ranging from
     * 0..<em>n</em>-1):
     * <ul>
     * <li><code>zoneStrings[i][0]</code> - time zone ID</li>
     * <li><code>zoneStrings[i][1]</code> - long name of zone in standard
     * time</li>
     * <li><code>zoneStrings[i][2]</code> - short name of zone in
     * standard time</li>
     * <li><code>zoneStrings[i][3]</code> - long name of zone in daylight
     * savings time</li>
     * <li><code>zoneStrings[i][4]</code> - short name of zone in daylight
     * savings time</li>
     * <li>The remainder varies depending on whether there is data 
     * on city name or generic time.  The city name, if available, comes
     * first.  The long and short generic times, if available, come next,
     * in that order. The length of the array (m) can be examined to 
     * determine which optional information is available.</li>
     * </ul>
     * The zone ID is <em>not</em> localized; it corresponds to the ID
     * value associated with a system time zone object.  All other entries
     * are localized names.  If a zone does not implement daylight savings
     * time, the daylight savings time names are ignored.
     * @see com.ibm.icu.util.TimeZone
     * @serial
     */
     private String zoneStrings[][] = null;

    /**
     * Unlocalized date-time pattern characters. For example: 'y', 'd', etc.
     * All locales use the same unlocalized pattern characters.
     */
    static final String  patternChars = "GyMdkHmsSEDFwWahKzYeugAZvcL";

    /**
     * Localized date-time pattern characters. For example, a locale may
     * wish to use 'u' rather than 'y' to represent years in its date format
     * pattern strings.
     * This string must be exactly 18 characters long, with the index of
     * the characters described by <code>DateFormat.ERA_FIELD</code>,
     * <code>DateFormat.YEAR_FIELD</code>, etc.  Thus, if the string were
     * "Xz...", then localized patterns would use 'X' for era and 'z' for year.
     * @serial
     */
    String  localPatternChars = null;

    /* use serialVersionUID from JDK 1.1.4 for interoperability */
    private static final long serialVersionUID = -5987973545549424702L;

    /**
     * Gets era strings. For example: "AD" and "BC".
     * @return the era strings.
     * @stable ICU 2.0
     */
    public String[] getEras() {
        return duplicate(eras);
    }

    /**
     * Sets era strings. For example: "AD" and "BC".
     * @param newEras the new era strings.
     * @stable ICU 2.0
     */
    public void setEras(String[] newEras) {
        eras = duplicate(newEras);
    }

    /**
     * Gets era name strings. For example: "Anno Domini" and "Before Christ".
     * @return the era strings.
     * @draft ICU 3.4
     * @provisional This API might change or be removed in a future release.
     */
    public String[] getEraNames() {
        return duplicate(eraNames);
    }

    /**
     * Sets era name strings. For example: "Anno Domini" and "Before Christ".
     * @param newEraNames the new era strings.
     * @internal revisit for ICU 3.6
     */
    public void setEraNames(String[] newEraNames) {
        eraNames = duplicate(newEraNames);
    }
    /**
     * Gets month strings. For example: "January", "February", etc.
     * @return the month strings.
     * @stable ICU 2.0
     */
    public String[] getMonths() {
        return duplicate(months);
    }

    /**
     * Gets month strings. For example: "January", "February", etc.
     * @param context    The month context, FORMAT or STANDALONE.
     * @param width      The width or the returned month string,
     *                   either WIDE, ABBREVIATED, or NARROW.
     * @return the month strings.
     * @draft ICU 3.4
     * @provisional This API might change or be removed in a future release.
     */
    public String[] getMonths(int context, int width) {
        String [] returnValue = null;
        switch (context) {
           case FORMAT :
              switch(width) {
                 case WIDE :
                    returnValue = months;
                    break;
                 case ABBREVIATED :
                    returnValue = shortMonths;
                    break;
                 case NARROW :
                    returnValue = narrowMonths;
                    break;
              }
              break;
           case STANDALONE :
              switch(width) {
                 case WIDE :
                    returnValue = standaloneMonths;
                    break;
                 case ABBREVIATED :
                    returnValue = standaloneShortMonths;
                    break;
                 case NARROW :
                    returnValue = standaloneNarrowMonths;
                    break;
              }
              break;
        }
        return duplicate(returnValue);
    }

    /**
     * Sets month strings. For example: "January", "February", etc.
     * @param newMonths the new month strings.
     * @stable ICU 2.0
     */
    public void setMonths(String[] newMonths) {
        months = duplicate(newMonths);
    }

    /**
     * Sets month strings. For example: "January", "February", etc.
     * @param newMonths the new month strings.
     * @param context    The formatting context, FORMAT or STANDALONE.
     * @param width      The width of the month string,
     *                   either WIDE, ABBREVIATED, or NARROW.
     * @internal revisit for ICU 3.6
     * @provisional This API might change or be removed in a future release.
     */
    public void setMonths(String[] newMonths, int context, int width) {
        switch (context) {
           case FORMAT :
              switch(width) {
                 case WIDE :
                    months = duplicate(newMonths);
                    break;
                 case ABBREVIATED :
                    shortMonths = duplicate(newMonths);
                    break;
                 case NARROW :
                    narrowMonths = duplicate(newMonths);
                    break;
              }
              break;
           case STANDALONE :
              switch(width) {
                 case WIDE :
                    standaloneMonths = duplicate(newMonths);
                    break;
                 case ABBREVIATED :
                    standaloneShortMonths = duplicate(newMonths);
                    break;
                 case NARROW :
                    standaloneNarrowMonths = duplicate(newMonths);
                    break;
              }
              break;
        }
    }
    
    /**
     * Gets short month strings. For example: "Jan", "Feb", etc.
     * @return the short month strings.
     * @stable ICU 2.0
     */
    public String[] getShortMonths() {
        return duplicate(shortMonths);
    }

    /**
     * Sets short month strings. For example: "Jan", "Feb", etc.
     * @param newShortMonths the new short month strings.
     * @stable ICU 2.0
     */
    public void setShortMonths(String[] newShortMonths) {
        shortMonths = duplicate(newShortMonths);
    }

    /**
     * Gets weekday strings. For example: "Sunday", "Monday", etc.
     * @return the weekday strings. Use <code>Calendar.SUNDAY</code>,
     * <code>Calendar.MONDAY</code>, etc. to index the result array.
     * @stable ICU 2.0
     */
    public String[] getWeekdays() {
        return duplicate(weekdays);
    }

    /**
     * Gets weekday strings. For example: "Sunday", "Monday", etc.
     * @return the weekday strings. Use <code>Calendar.SUNDAY</code>,
     * <code>Calendar.MONDAY</code>, etc. to index the result array.
     * @param context    Formatting context, either FORMAT or STANDALONE.
     * @param width      Width of strings to be returned, either
     *                   WIDE, ABBREVIATED, or NARROW
     * @draft ICU 3.4
     * @provisional This API might change or be removed in a future release.
     */
    public String[] getWeekdays(int context, int width) {
        String [] returnValue = null;
        switch (context) {
           case FORMAT :
              switch(width) {
                 case WIDE :
                    returnValue = weekdays;
                    break;
                 case ABBREVIATED :
                    returnValue = shortWeekdays;
                    break;
                 case NARROW :
                    returnValue = narrowWeekdays;
                    break;
              }
              break;
           case STANDALONE :
              switch(width) {
                 case WIDE :
                    returnValue = standaloneWeekdays;
                    break;
                 case ABBREVIATED :
                    returnValue = standaloneShortWeekdays;
                    break;
                 case NARROW :
                    returnValue = standaloneNarrowWeekdays;
                    break;
              }
              break;
        }
        return duplicate(returnValue);
    }

    /**
     * Sets weekday strings. For example: "Sunday", "Monday", etc.
     * @param newWeekdays The new weekday strings.
     * @param context     The formatting context, FORMAT or STANDALONE.
     * @param width       The width of the strings,
     *                    either WIDE, ABBREVIATED, or NARROW.
     * @internal revisit for ICU 3.6
     * @provisional This API might change or be removed in a future release.
     */
    public void setWeekdays(String[] newWeekdays, int context, int width) {
        switch (context) {
           case FORMAT :
              switch(width) {
                 case WIDE :
                    weekdays = duplicate(newWeekdays);
                    break;
                 case ABBREVIATED :
                    shortWeekdays = duplicate(newWeekdays);
                    break;
                 case NARROW :
                    narrowWeekdays = duplicate(newWeekdays);
                    break;
              }
              break;
           case STANDALONE :
              switch(width) {
                 case WIDE :
                    standaloneWeekdays = duplicate(newWeekdays);
                    break;
                 case ABBREVIATED :
                    standaloneShortWeekdays = duplicate(newWeekdays);
                    break;
                 case NARROW :
                    standaloneNarrowWeekdays = duplicate(newWeekdays);
                    break;
              }
              break;
        }
    }

    /**
     * Sets weekday strings. For example: "Sunday", "Monday", etc.
     * @param newWeekdays the new weekday strings. The array should
     * be indexed by <code>Calendar.SUNDAY</code>,
     * <code>Calendar.MONDAY</code>, etc.
     * @stable ICU 2.0
     */
    public void setWeekdays(String[] newWeekdays) {
        weekdays = duplicate(newWeekdays);
    }

    /**
     * Gets short weekday strings. For example: "Sun", "Mon", etc.
     * @return the short weekday strings. Use <code>Calendar.SUNDAY</code>,
     * <code>Calendar.MONDAY</code>, etc. to index the result array.
     * @stable ICU 2.0
     */
    public String[] getShortWeekdays() {
        return duplicate(shortWeekdays);
    }

    /**
     * Sets short weekday strings. For example: "Sun", "Mon", etc.
     * @param newShortWeekdays the new short weekday strings. The array should
     * be indexed by <code>Calendar.SUNDAY</code>,
     * <code>Calendar.MONDAY</code>, etc.
     * @stable ICU 2.0
     */
    public void setShortWeekdays(String[] newShortWeekdays) {
        shortWeekdays = duplicate(newShortWeekdays);
    }

    /**
     * Gets ampm strings. For example: "AM" and "PM".
     * @return the weekday strings.
     * @stable ICU 2.0
     */
    public String[] getAmPmStrings() {
        return duplicate(ampms);
    }

    /**
     * Sets ampm strings. For example: "AM" and "PM".
     * @param newAmpms the new ampm strings.
     * @stable ICU 2.0
     */
    public void setAmPmStrings(String[] newAmpms) {
        ampms = duplicate(newAmpms);
    }

    /**
     * Gets timezone strings.
     * @return the timezone strings.
     * @stable ICU 2.0
     */
    public String[][] getZoneStrings() {
        String[][] strings = zoneStrings;
        if(strings == null){
            // get the default zone strings
            ZoneItemInfo zii = getDefaultZoneItemInfo();
            strings = zii.tzStrings;
        }
        return duplicate(strings);
    }

    /**
     * Sets timezone strings.
     * @param newZoneStrings the new timezone strings.
     * @stable ICU 2.0
     */
    public void setZoneStrings(String[][] newZoneStrings) {
        zoneStrings = duplicate(newZoneStrings);
        // need to update local zone item info
        localZoneItemInfo = null;
    }

    /**
     * Gets localized date-time pattern characters. For example: 'u', 't', etc.
     * @return the localized date-time pattern characters.
     * @stable ICU 2.0
     */
    public String getLocalPatternChars() {
        return new String(localPatternChars);
    }

    /**
     * Sets localized date-time pattern characters. For example: 'u', 't', etc.
     * @param newLocalPatternChars the new localized date-time
     * pattern characters.
     * @stable ICU 2.0
     */
    public void setLocalPatternChars(String newLocalPatternChars) {
        localPatternChars = newLocalPatternChars;
    }

    /**
     * Overrides Cloneable
     * @stable ICU 2.0
     */
    public Object clone()
    {
        try {
            DateFormatSymbols other = (DateFormatSymbols)super.clone();
            copyMembers(this, other);
            return other;
        } catch (CloneNotSupportedException e) {
            ///CLOVER:OFF
            throw new IllegalStateException();
            ///CLOVER:ON
        }
    }

    /**
     * Override hashCode.
     * Generates a hash code for the DateFormatSymbols object.
     * @stable ICU 2.0
     */
    public int hashCode() {
        int hashcode = 0;
        hashcode ^= requestedLocale.toString().hashCode();
        String[][] tzStrings = zoneStrings;
        if (tzStrings == null){
            ZoneItemInfo zii = getDefaultZoneItemInfo();
            tzStrings = zii.tzStrings;
        }
        for(int i = 0; i < tzStrings.length; i++) {
        	for (int j = 0; j < tzStrings[i].length; j++) {
        		if (tzStrings[i][j] != null) {
            		hashcode ^= tzStrings[i][j].hashCode();
        		}
        	}
        }
        return hashcode;
    }

    /**
     * Override equals
     * @stable ICU 2.0
     */
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DateFormatSymbols that = (DateFormatSymbols) obj;
        return (Utility.arrayEquals(eras, that.eras)
                && Utility.arrayEquals(eraNames, that.eraNames)
                && Utility.arrayEquals(months, that.months)
                && Utility.arrayEquals(shortMonths, that.shortMonths)
                && Utility.arrayEquals(narrowMonths, that.narrowMonths)
                && Utility.arrayEquals(standaloneMonths, that.standaloneMonths)
                && Utility.arrayEquals(standaloneShortMonths, that.standaloneShortMonths)
                && Utility.arrayEquals(standaloneNarrowMonths, that.standaloneNarrowMonths)
                && Utility.arrayEquals(weekdays, that.weekdays)
                && Utility.arrayEquals(shortWeekdays, that.shortWeekdays)
                && Utility.arrayEquals(narrowWeekdays, that.narrowWeekdays)
                && Utility.arrayEquals(standaloneWeekdays, that.standaloneWeekdays)
                && Utility.arrayEquals(standaloneShortWeekdays, that.standaloneShortWeekdays)
                && Utility.arrayEquals(standaloneNarrowWeekdays, that.standaloneNarrowWeekdays)
                && Utility.arrayEquals(ampms, that.ampms)
                && arrayOfArrayEquals(zoneStrings, that.zoneStrings)
                // getDiplayName maps deprecated country and language codes to the current ones
                // too bad there is no way to get the current codes!
                // I thought canolicalize() would map the codes but .. alas! it doesn't.
                && requestedLocale.getDisplayName().equals(that.requestedLocale.getDisplayName())
                && Utility.arrayEquals(localPatternChars,
                                       that.localPatternChars));
    }

    // =======================privates===============================

    /**
     * Useful constant for defining timezone offsets.
     */
    static final int millisPerHour = 60*60*1000;
    /**
     * 
     * @param desiredLocale
     * @param type
     * @draft ICU 3.0
     * @provisional This API might change or be removed in a future release.
     */
    protected void initializeData(ULocale desiredLocale, String type)
    {
        CalendarData calData = new CalendarData(desiredLocale, type);
        initializeData(desiredLocale, calData);
    }

    /**
     * 
     * @param desiredLocale
     * @param calData
     * @draft ICU 3.0
     * @provisional This API might change or be removed in a future release.
     */
    protected void initializeData(ULocale desiredLocale, CalendarData calData)
    {

        // FIXME: cache only ResourceBundle. Hence every time, will do
        // getObject(). This won't be necessary if the Resource itself
        // is cached.
        eras = calData.getEras("abbreviated");

        try {
           eraNames = calData.getEras("wide");
        }
        catch (MissingResourceException e) {
           eraNames = calData.getEras("abbreviated");
        }

        months = calData.getStringArray("monthNames", "wide");
        shortMonths = calData.getStringArray("monthNames", "abbreviated");

        try {
           narrowMonths = calData.getStringArray("monthNames", "narrow");
        } 
        catch (MissingResourceException e) {
           narrowMonths = calData.getStringArray("monthNames", "abbreviated");
        }

        try {
           standaloneMonths = calData.getStringArray("monthNames", "stand-alone", "wide");
        } 
        catch (MissingResourceException e) {
           standaloneMonths = calData.getStringArray("monthNames", "format", "wide");
        }

        try {
           standaloneShortMonths = calData.getStringArray("monthNames", "stand-alone", "abbreviated");
        } 
        catch (MissingResourceException e) {
           standaloneShortMonths = calData.getStringArray("monthNames", "format", "abbreviated");
        }

        try {
           standaloneNarrowMonths = calData.getStringArray("monthNames", "stand-alone", "narrow");
        } 
        catch (MissingResourceException e) {
           try {
              standaloneNarrowMonths = calData.getStringArray("monthNames", "format", "narrow");
           }
           catch (MissingResourceException e1) {
              standaloneNarrowMonths = calData.getStringArray("monthNames", "format", "abbreviated");
           }
        }

        String[] lWeekdays = calData.getStringArray("dayNames", "wide");
        weekdays = new String[8];
        weekdays[0] = "";  // 1-based
        System.arraycopy(lWeekdays, 0, weekdays, 1, lWeekdays.length);

        String[] sWeekdays = calData.getStringArray("dayNames", "abbreviated");
        shortWeekdays = new String[8];
        shortWeekdays[0] = "";  // 1-based
        System.arraycopy(sWeekdays, 0, shortWeekdays, 1, sWeekdays.length);

        String [] nWeekdays = null;
        try {
           nWeekdays = calData.getStringArray("dayNames", "narrow");
        }
        catch (MissingResourceException e) {
           nWeekdays = calData.getStringArray("dayNames", "abbreviated");
        }
        narrowWeekdays = new String[8];
        narrowWeekdays[0] = "";  // 1-based
        System.arraycopy(nWeekdays, 0, narrowWeekdays, 1, nWeekdays.length);

        String [] saWeekdays = null;
        try {
           saWeekdays = calData.getStringArray("dayNames", "stand-alone", "wide");
        }
        catch (MissingResourceException e) {
           saWeekdays = calData.getStringArray("dayNames", "format", "wide");
        }
        standaloneWeekdays = new String[8];
        standaloneWeekdays[0] = "";  // 1-based
        System.arraycopy(saWeekdays, 0, standaloneWeekdays, 1, saWeekdays.length);

        String [] ssWeekdays = null;
        try {
           ssWeekdays = calData.getStringArray("dayNames", "stand-alone", "abbreviated");
        }
        catch (MissingResourceException e) {
           ssWeekdays = calData.getStringArray("dayNames", "format", "abbreviated");
        }
        standaloneShortWeekdays = new String[8];
        standaloneShortWeekdays[0] = "";  // 1-based
        System.arraycopy(ssWeekdays, 0, standaloneShortWeekdays, 1, ssWeekdays.length);

        String [] snWeekdays = null;
        try {
           snWeekdays = calData.getStringArray("dayNames", "stand-alone", "narrow");
        }
        catch (MissingResourceException e) {
           try {
              snWeekdays = calData.getStringArray("dayNames", "format", "narrow");
           }
           catch (MissingResourceException e1) {
              snWeekdays = calData.getStringArray("dayNames", "format", "abbreviated");
           }
        }
        standaloneNarrowWeekdays = new String[8];
        standaloneNarrowWeekdays[0] = "";  // 1-based
        System.arraycopy(snWeekdays, 0, standaloneNarrowWeekdays, 1, snWeekdays.length);

        ampms = calData.getStringArray("AmPmMarkers");

/*  THE FOLLOWING DOESN'T WORK; A COUNTRY LOCALE WITH ONE ZONE BLOCKS THE LANGUAGE LOCALE
        // These really do use rb and not calData
        ICUResourceBundle rb = (ICUResourceBundle)UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, desiredLocale);
        // hack around class cast problem
        // zoneStrings = (String[][])rb.getObject("zoneStrings");
        ICUResourceBundle zoneObject = rb.get("zoneStrings");
        zoneStrings = new String[zoneObject.getSize()][];
        for(int i =0; i< zoneObject.getSize(); i++){
            ICUResourceBundle zoneArr = zoneObject.get(i);
            String[] strings = new String[zoneArr.getSize()];
            for(int j=0; j<zoneArr.getSize(); j++){
                strings[j]=zoneArr.get(j).getString();
            }
            zoneStrings[i] = strings;
        }
*/        
        requestedLocale = desiredLocale;
     
        ICUResourceBundle rb = (ICUResourceBundle)UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, desiredLocale);
        localPatternChars = rb.getString("localPatternChars");

        // TODO: obtain correct actual/valid locale later
        ULocale uloc = rb.getULocale();
        setLocale(uloc, uloc);
    }

    private static final boolean arrayOfArrayEquals(Object[][] aa1, Object[][]aa2) {
        if (aa1 == aa2) { // both are null
            return true;
        }
        if (aa1 == null || aa2 == null) { // one is null and the other is not
            return false;
        }
        if (aa1.length != aa2.length) {
            return false;
        }
        boolean equal = true;
        for (int i = 0; i < aa1.length; i++) {
            equal = Utility.arrayEquals(aa1[i], aa2[i]);
            if (!equal) {
                break;
            }
        }
        return equal;
    }

    /**
     * Package private: used by SimpleDateFormat.
     * Gets the string for the specified time zone.
     * @param zid The time zone ID
     * @param type The type of zone string
     * @return The zone string, or null if not available.
     */
    String getZoneString(String zid, int type) {
        // Try local zone item info first
        String zoneString = getZoneString(getLocalZoneItemInfo(), zid, type);
        if (zoneString == null) {
            // Fallback to the default info
            zoneString = getZoneString(getDefaultZoneItemInfo(), zid, type);
        }
        return zoneString;
    }

    /**
     * Gets the zone string from the specified zone item info
     */
    private String getZoneString(ZoneItemInfo zinfo, String zid, int type) {
        if (zinfo == null) {
            return null;
        }
        String[] names = (String[])zinfo.tzidMap.get(zid);
        if (names != null) {
            // get name for the type
            int index = -1;
            switch (type) {
            case TIMEZONE_LONG_STANDARD:
                index = 1;
                break;
            case TIMEZONE_SHORT_STANDARD:
                index = 2;
                break;
            case TIMEZONE_LONG_DAYLIGHT:
                index = 3;
                break;
            case TIMEZONE_SHORT_DAYLIGHT:
                index = 4;
                break;
            case TIMEZONE_EXEMPLAR_CITY:
                if (names.length == 6 || names.length == 8) {
                    index = 5;
                }
                break;
            case TIMEZONE_LONG_GENERIC:
                if (names.length == 8) {
                    index = 6;
                } else {
                    index = 5;
                }
                break;
            case TIMEZONE_SHORT_GENERIC:
                if (names.length == 8) {
                    index = 7;
                } else {
                    index = 6;
                }
            }
            if (index < names.length) {
                return names[index];
            }
        }
        return null;
    }

    class ZoneItem{
        String value;
        int type;
        String zid;
    }

    /**
     * Package private: used by SimpleDateformat
     * Gets the ZoneItem instance which has zone strings
     * which matches the specified text.
     * @param text The text which contains a zone string
     * @param start The start position of zone string in the text
     * @return A ZonItem instance for the longest matching zone
     * string.
     */
    ZoneItem findZoneIDTypeValue(String text, int start){
        ZoneItem item = null;
        int textLength = text.length() - start;
        if (lastZoneItem != null && textLength == lastZoneItem.value.length()) {
            if (text.regionMatches(true, start, lastZoneItem.value, 0, textLength)) {
                item = new ZoneItem();
                item.type = lastZoneItem.type;
                item.value = lastZoneItem.value;
                item.zid = lastZoneItem.zid;
                return item;
            }
        }

        ZoneItemInfo zinfo = getLocalZoneItemInfo();
        if (zinfo != null) {
            // look up the zone string in localZoneItemInfo first
            item = (ZoneItem)zinfo.tzStringMap.get(text, start);
        }

        // look up the zone string in default ZoneItemInfo for the locale
        zinfo = getDefaultZoneItemInfo();
        ZoneItem itemForLocale = (ZoneItem)zinfo.tzStringMap.get(text, start);
        if (itemForLocale != null) {
            // we want to use longer match
            if (item == null || itemForLocale.value.length() > item.value.length()) {
                item = itemForLocale;
            }
        }

        if (item != null && textLength == item.value.length()) {
            // clone the last match for next time
            // only when the substring completely matches
            // with the value resolved
            lastZoneItem = new ZoneItem();
            lastZoneItem.type = item.type;
            lastZoneItem.value = item.value;
            lastZoneItem.zid = item.zid;
        }
        return item;
    }

    /**
     * A class holds zone strings and searchable indice
     */
    private class ZoneItemInfo {
        String[][] tzStrings;
        HashMap tzidMap;
        TextTrieMap tzStringMap;
    }

    /**
     * A cache for ZoneItemInfo objects, shared by class instances.
     */
    private static SoftCache zoneItemInfoCache = new SoftCache();

    /**
     * A ZoneItemInfo instance which holds custom timezone strings
     */
    private transient ZoneItemInfo localZoneItemInfo;

    /**
     * Single entry cache for findZoneTypeValue()
     */
    private transient ZoneItem lastZoneItem;

    /**
     * Gets the ZoneItemInfo instance for the locale used by this object.
     * If it does not exist, create new one and register in the static cache.
     */
    private ZoneItemInfo getDefaultZoneItemInfo() {
        ZoneItemInfo zii = (ZoneItemInfo)zoneItemInfoCache.get(requestedLocale);
        if (zii != null) {
            return zii;
        }
        zii = getZoneItemInfo(getDefaultZoneStrings(requestedLocale));
        // Add fallback display names
        String[] zoneIDs = TimeZone.getAvailableIDs();
        for (int i = 0; i < zoneIDs.length; i++) {
            Object o = zii.tzidMap.get(zoneIDs[i]);
            if (o != null) {
                // Already has names
                continue;
            }
            String value = ZoneMeta.displayFallback(zoneIDs[i], null, requestedLocale);
            if (value != null) {
                String[] strings = new String[8];
                strings[5] = value;
                zii.tzidMap.put(zoneIDs[i], strings);
                
                ZoneItem item = new ZoneItem();
                item.zid = zoneIDs[i];
                item.value = value;
                item.type = TIMEZONE_EXEMPLAR_CITY;
                zii.tzStringMap.put(item.value, item);
            }
        }
        zoneItemInfoCache.put(requestedLocale, zii);
        return zii;
    }

    /**
     * Gets the array of zone strings for the specified locale.
     */
    private static String[][] getDefaultZoneStrings(ULocale locale) {
        ArrayList tmpList = new ArrayList();
        HashSet tmpSet = new HashSet();
        for (ULocale tempLocale = locale; tempLocale != null; tempLocale = tempLocale.getFallback()) {
            ICUResourceBundle bundle = (ICUResourceBundle)UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, tempLocale);
            ICUResourceBundle zoneStringsBundle = bundle.getWithFallback("zoneStrings");
            for(int i = 0; i < zoneStringsBundle.getSize(); i++){
                ICUResourceBundle zoneTable = zoneStringsBundle.get(i);
                String key = Utility.replaceAll(zoneTable.getKey(), ":", "/");
                // hack for the root zone strings
                if(key.length() == 0|| zoneTable.getType() != ICUResourceBundle.TABLE){
                    continue;
                }
                if (tmpSet.contains(key)) {
                    // only add if we don't have already
                    continue;
                }
                String[] strings = new String[8];
                strings[0] = key;
                try {
                    strings[1] = zoneTable.getStringWithFallback(LONG_STANDARD);
                } catch (MissingResourceException ex) {
                    // throw away the exception   
                }
                try {
                    strings[2] = zoneTable.getStringWithFallback(SHORT_STANDARD);
                } catch (MissingResourceException ex) {
                    // throw away the exception   
                }
                try {
                    strings[3] = zoneTable.getStringWithFallback(LONG_DAYLIGHT);
                } catch (MissingResourceException ex) {
                    // throw away the exception   
                }
                try {
                    strings[4] = zoneTable.getStringWithFallback(SHORT_DAYLIGHT);
                } catch (MissingResourceException ex) {
                    //  throw away the exception    
                }
                try {
                    String city = zoneTable.getStringWithFallback(EXEMPLAR_CITY);
                    strings[5] = ZoneMeta.displayFallback(key, city, tempLocale);
                } catch (MissingResourceException ex) {
                    // throw away the exception   
                }
                try {
                    strings[6] = zoneTable.getStringWithFallback(LONG_GENERIC);
                } catch (MissingResourceException ex) {
                    // throw away the exception                 
                }
                try {
                    strings[7] = zoneTable.getStringWithFallback(SHORT_GENERIC);
                } catch (MissingResourceException ex) {
                    // throw away the exception   
                }
                tmpList.add(strings);
            }
        }
        String[][] array = new String[tmpList.size()][8];
        tmpList.toArray(array);
        return array;
    }

    /**
     * Gets the array of zone strings for the custom zone strings
     */
    private ZoneItemInfo getLocalZoneItemInfo() {
        if (localZoneItemInfo == null && zoneStrings != null) {
            localZoneItemInfo = getZoneItemInfo(zoneStrings);
        }
        return localZoneItemInfo;
    }

    /**
     * Creates a new ZoneItemInfo instance from the array of time zone
     * strings.
     */
    private ZoneItemInfo getZoneItemInfo(String[][] strings) {
        ZoneItemInfo zii = new ZoneItemInfo();
        zii.tzStrings = strings;
        zii.tzidMap = new HashMap();
        zii.tzStringMap = new TextTrieMap(true);
        for (int i = 0; i < strings.length; i++) {
            String zid = strings[i][0];
            if (zid != null && zid.length() > 0) {
                zii.tzidMap.put(zid, strings[i]);
                int nameCount = strings[i].length < 8 ? strings[i].length : 8;
                for (int j = 1; j < nameCount; j++) {
                    if (strings[i][j] != null) {
                        // map zoneStrings array index to timezone name type
                        int type = -1;
                        switch (j) {
                        case 1:
                            type = TIMEZONE_LONG_STANDARD;
                            break;
                        case 2:
                            type = TIMEZONE_SHORT_STANDARD;
                            break;
                        case 3:
                            type = TIMEZONE_LONG_DAYLIGHT;
                            break;
                        case 4:
                            type = TIMEZONE_SHORT_DAYLIGHT;
                            break;
                        case 5:
                            if (nameCount == 6 || nameCount == 8) {
                                type = TIMEZONE_EXEMPLAR_CITY;
                            } else {
                                type = TIMEZONE_LONG_GENERIC;
                            }
                            break;
                        case 6:
                            if (nameCount == 8) {
                                type = TIMEZONE_LONG_GENERIC;
                            } else {
                                type = TIMEZONE_SHORT_GENERIC;
                            }
                            break;
                        case 7:
                            type = TIMEZONE_SHORT_GENERIC;
                            break;
                        default:
                            // never occur
                            continue;
                        }
                        ZoneItem item = new ZoneItem();
                        item.zid = zid;
                        item.value = strings[i][j];
                        item.type = type;
                        zii.tzStringMap.put(strings[i][j], item);
                    }
                }
            }
        }
        return zii;
    }

    /**
     * save the input locale
     */
    private ULocale requestedLocale; 
 
    /**
     * The translation type of the translated zone strings
     * @internal ICU 3.6
     */
     private static final String   SHORT_GENERIC  = "sg",
                                   SHORT_STANDARD = "ss",
                                   SHORT_DAYLIGHT = "sd",
                                   LONG_GENERIC   = "lg",
                                   LONG_STANDARD  = "ls",
                                   LONG_DAYLIGHT  = "ld",
                                   EXEMPLAR_CITY  = "ec";
    /**
     * The translation type of the translated zone strings
     * @internal ICU 3.6
     */
     static final int   TIMEZONE_SHORT_GENERIC  = 0,
                        TIMEZONE_SHORT_STANDARD = 1,
                        TIMEZONE_SHORT_DAYLIGHT = 2,
                        TIMEZONE_LONG_GENERIC   = 3,
                        TIMEZONE_LONG_STANDARD  = 4,
                        TIMEZONE_LONG_DAYLIGHT  = 5,
                        TIMEZONE_EXEMPLAR_CITY  = 6,
                        TIMEZONE_COUNT          = 7;

     /**
     * Package private: used by SimpleDateFormat
     * Gets the index for the given time zone ID to obtain the timezone
     * strings for formatting. The time zone ID is just for programmatic
     * lookup. NOT LOCALIZED!!!
     * @param ID the given time zone ID.
     * @return the index of the given time zone ID.  Returns -1 if
     * the given time zone ID can't be located in the DateFormatSymbols object.
     * @see com.ibm.icu.util.SimpleTimeZone
     */
    final int getZoneIndex(String ID) {
        int result = _getZoneIndex(ID);
        if (result >= 0) {
            return result;
        }
        // Do a search through the equivalency group for the given ID
        int n = ZoneMeta.countEquivalentIDs(ID);
        if (n > 1) {
            for (int i=0; i<n; ++i) {
                String equivID = ZoneMeta.getEquivalentID(ID, i);
                if (!equivID.equals(ID)) {
                    int equivResult = _getZoneIndex(equivID);
                    if (equivResult >= 0) {
                        return equivResult;
                    }
                }
            }
        }
        return -1;
    }
    
    /**
     * Lookup the given ID.  Do NOT do an equivalency search.
     */
    private int _getZoneIndex(String ID)
    {
        for (int index=0; index<zoneStrings.length; index++) {
            if (ID.equalsIgnoreCase(zoneStrings[index][0])) return index;
        }
        return -1;
    }

    /**
     * Clones an array of Strings.
     * @param srcArray the source array to be cloned.
     * @param count the number of elements in the given source array.
     * @return a cloned array.
     */
    private final String[] duplicate(String[] srcArray)
    {
        return (String[])srcArray.clone();
    }

    private final String[][] duplicate(String[][] srcArray)
    {
        String[][] aCopy = new String[srcArray.length][];
        for (int i = 0; i < srcArray.length; ++i)
            aCopy[i] = duplicate(srcArray[i]);
        return aCopy;
    }

    /**
     * Clones all the data members from the source DateFormatSymbols to
     * the target DateFormatSymbols. This is only for subclasses.
     * @param src the source DateFormatSymbols.
     * @param dst the target DateFormatSymbols.
     */
    private final void copyMembers(DateFormatSymbols src, DateFormatSymbols dst)
    {
        dst.eras = duplicate(src.eras);
        dst.eraNames = duplicate(src.eraNames);
        dst.months = duplicate(src.months);
        dst.shortMonths = duplicate(src.shortMonths);
        dst.narrowMonths = duplicate(src.narrowMonths);
        dst.standaloneMonths = duplicate(src.standaloneMonths);
        dst.standaloneShortMonths = duplicate(src.standaloneShortMonths);
        dst.standaloneNarrowMonths = duplicate(src.standaloneNarrowMonths);
        dst.weekdays = duplicate(src.weekdays);
        dst.shortWeekdays = duplicate(src.shortWeekdays);
        dst.narrowWeekdays = duplicate(src.narrowWeekdays);
        dst.standaloneWeekdays = duplicate(src.standaloneWeekdays);
        dst.standaloneShortWeekdays = duplicate(src.standaloneShortWeekdays);
        dst.standaloneNarrowWeekdays = duplicate(src.standaloneNarrowWeekdays);
        dst.ampms = duplicate(src.ampms);
        if (src.zoneStrings != null) {
            dst.zoneStrings = duplicate(src.zoneStrings);
        }
        dst.requestedLocale = new ULocale(src.requestedLocale.toString());
        dst.localPatternChars = new String (src.localPatternChars);
    }

    /**
     * Compares the equality of the two arrays of String.
     * @param current this String array.
     * @param other that String array.
    private final boolean equals(String[] current, String[] other)
    {
        int count = current.length;

        for (int i = 0; i < count; ++i)
            if (!current[i].equals(other[i]))
                return false;
        return true;
    }
     */

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Get the {@link DateFormatSymbols} object that should be used to format a
     * calendar system's dates in the given locale.
     * <p>
     * <b>Subclassing:</b><br>
     * When creating a new Calendar subclass, you must create the
     * {@link ResourceBundle ResourceBundle}
     * containing its {@link DateFormatSymbols DateFormatSymbols} in a specific place.
     * The resource bundle name is based on the calendar's fully-specified
     * class name, with ".resources" inserted at the end of the package name
     * (just before the class name) and "Symbols" appended to the end.
     * For example, the bundle corresponding to "com.ibm.icu.util.HebrewCalendar"
     * is "com.ibm.icu.impl.data.HebrewCalendarSymbols".
     * <p>
     * Within the ResourceBundle, this method searches for five keys:
     * <ul>
     * <li><b>DayNames</b> -
     *      An array of strings corresponding to each possible
     *      value of the <code>DAY_OF_WEEK</code> field.  Even though
     *      <code>DAY_OF_WEEK</code> starts with <code>SUNDAY</code> = 1,
     *      This array is 0-based; the name for Sunday goes in the
     *      first position, at index 0.  If this key is not found
     *      in the bundle, the day names are inherited from the
     *      default <code>DateFormatSymbols</code> for the requested locale.
     *
     * <li><b>DayAbbreviations</b> -
     *      An array of abbreviated day names corresponding
     *      to the values in the "DayNames" array.  If this key
     *      is not found in the resource bundle, the "DayNames"
     *      values are used instead.  If neither key is found,
     *      the day abbreviations are inherited from the default
     *      <code>DateFormatSymbols</code> for the locale.
     *
     * <li><b>MonthNames</b> -
     *      An array of strings corresponding to each possible
     *      value of the <code>MONTH</code> field.  If this key is not found
     *      in the bundle, the month names are inherited from the
     *      default <code>DateFormatSymbols</code> for the requested locale.
     *
     * <li><b>MonthAbbreviations</b> -
     *      An array of abbreviated day names corresponding
     *      to the values in the "MonthNames" array.  If this key
     *      is not found in the resource bundle, the "MonthNames"
     *      values are used instead.  If neither key is found,
     *      the day abbreviations are inherited from the default
     *      <code>DateFormatSymbols</code> for the locale.
     *
     * <li><b>Eras</b> -
     *      An array of strings corresponding to each possible
     *      value of the <code>ERA</code> field.  If this key is not found
     *      in the bundle, the era names are inherited from the
     *      default <code>DateFormatSymbols</code> for the requested locale.
     * </ul>
     * <p>
     * @param cal       The calendar system whose date format symbols are desired.
     * @param locale    The locale whose symbols are desired.
     *
     * @see DateFormatSymbols#DateFormatSymbols(java.util.Locale)
     * @stable ICU 2.0
     */
    public DateFormatSymbols(Calendar cal, Locale locale) {
        initializeData(ULocale.forLocale(locale), cal.getType());
    }

    /**
     * Get the {@link DateFormatSymbols} object that should be used to format a
     * calendar system's dates in the given locale.
     * <p>
     * <b>Subclassing:</b><br>
     * When creating a new Calendar subclass, you must create the
     * {@link ResourceBundle ResourceBundle}
     * containing its {@link DateFormatSymbols DateFormatSymbols} in a specific place.
     * The resource bundle name is based on the calendar's fully-specified
     * class name, with ".resources" inserted at the end of the package name
     * (just before the class name) and "Symbols" appended to the end.
     * For example, the bundle corresponding to "com.ibm.icu.util.HebrewCalendar"
     * is "com.ibm.icu.impl.data.HebrewCalendarSymbols".
     * <p>
     * Within the ResourceBundle, this method searches for five keys:
     * <ul>
     * <li><b>DayNames</b> -
     *      An array of strings corresponding to each possible
     *      value of the <code>DAY_OF_WEEK</code> field.  Even though
     *      <code>DAY_OF_WEEK</code> starts with <code>SUNDAY</code> = 1,
     *      This array is 0-based; the name for Sunday goes in the
     *      first position, at index 0.  If this key is not found
     *      in the bundle, the day names are inherited from the
     *      default <code>DateFormatSymbols</code> for the requested locale.
     *
     * <li><b>DayAbbreviations</b> -
     *      An array of abbreviated day names corresponding
     *      to the values in the "DayNames" array.  If this key
     *      is not found in the resource bundle, the "DayNames"
     *      values are used instead.  If neither key is found,
     *      the day abbreviations are inherited from the default
     *      <code>DateFormatSymbols</code> for the locale.
     *
     * <li><b>MonthNames</b> -
     *      An array of strings corresponding to each possible
     *      value of the <code>MONTH</code> field.  If this key is not found
     *      in the bundle, the month names are inherited from the
     *      default <code>DateFormatSymbols</code> for the requested locale.
     *
     * <li><b>MonthAbbreviations</b> -
     *      An array of abbreviated day names corresponding
     *      to the values in the "MonthNames" array.  If this key
     *      is not found in the resource bundle, the "MonthNames"
     *      values are used instead.  If neither key is found,
     *      the day abbreviations are inherited from the default
     *      <code>DateFormatSymbols</code> for the locale.
     *
     * <li><b>Eras</b> -
     *      An array of strings corresponding to each possible
     *      value of the <code>ERA</code> field.  If this key is not found
     *      in the bundle, the era names are inherited from the
     *      default <code>DateFormatSymbols</code> for the requested locale.
     * </ul>
     * <p>
     * @param cal       The calendar system whose date format symbols are desired.
     * @param locale    The ulocale whose symbols are desired.
     *
     * @see DateFormatSymbols#DateFormatSymbols(java.util.Locale)
     * @draft ICU 3.2
     * @provisional This API might change or be removed in a future release.
     */
    public DateFormatSymbols(Calendar cal, ULocale locale) {
        initializeData(locale, cal.getType());
    }

    /**
     * Variant of DateFormatSymbols(Calendar, Locale) that takes the Calendar class
     * instead of a Calandar instance.
     * @see #DateFormatSymbols(Calendar, Locale)
     * @stable ICU 2.2
     */
    public DateFormatSymbols(Class calendarClass, Locale locale) {
        this(calendarClass, ULocale.forLocale(locale));
    }

    /**
     * Variant of DateFormatSymbols(Calendar, ULocale) that takes the Calendar class
     * instead of a Calandar instance.
     * @see #DateFormatSymbols(Calendar, Locale)
     * @draft ICU 3.2
     * @provisional This API might change or be removed in a future release.
     */
    public DateFormatSymbols(Class calendarClass, ULocale locale) {
        String fullName = calendarClass.getName();
        int lastDot = fullName.lastIndexOf('.');
        String className = fullName.substring(lastDot+1);
        String calType = Utility.replaceAll(className, "Calendar", "").toLowerCase();
        
        initializeData(locale, calType);
    }

    /**
     * Fetch a custom calendar's DateFormatSymbols out of the given resource
     * bundle.  Symbols that are not overridden are inherited from the
     * default DateFormatSymbols for the locale.
     * @see DateFormatSymbols#DateFormatSymbols
     * @stable ICU 2.0
     */
    public DateFormatSymbols(ResourceBundle bundle, Locale locale) {
        this(bundle, ULocale.forLocale(locale));
    }

    /**
     * Fetch a custom calendar's DateFormatSymbols out of the given resource
     * bundle.  Symbols that are not overridden are inherited from the
     * default DateFormatSymbols for the locale.
     * @see DateFormatSymbols#DateFormatSymbols
     * @draft ICU 3.2
     * @provisional This API might change or be removed in a future release.
     */
    public DateFormatSymbols(ResourceBundle bundle, ULocale locale) {
        initializeData(locale, 
            new CalendarData((ICUResourceBundle)bundle, null));
    }

    /**
     * Find the ResourceBundle containing the date format information for
     * a specified calendar subclass in a given locale.
     * <p>
     * The resource bundle name is based on the calendar's fully-specified
     * class name, with ".resources" inserted at the end of the package name
     * (just before the class name) and "Symbols" appended to the end.
     * For example, the bundle corresponding to "com.ibm.icu.util.HebrewCalendar"
     * is "com.ibm.icu.impl.data.HebrewCalendarSymbols".
     * @stable ICU 2.0
     */
    static public ResourceBundle getDateFormatBundle(Class calendarClass, Locale locale)
        throws MissingResourceException {
        return getDateFormatBundle(calendarClass, ULocale.forLocale(locale));
    }
        
    /**
     * Find the ResourceBundle containing the date format information for
     * a specified calendar subclass in a given locale.
     * <p>
     * The resource bundle name is based on the calendar's fully-specified
     * class name, with ".resources" inserted at the end of the package name
     * (just before the class name) and "Symbols" appended to the end.
     * For example, the bundle corresponding to "com.ibm.icu.util.HebrewCalendar"
     * is "com.ibm.icu.impl.data.HebrewCalendarSymbols".
     * @draft ICU 3.2
     * @provisional This API might change or be removed in a future release.
     */
    static public ResourceBundle getDateFormatBundle(Class calendarClass, ULocale locale)
        throws MissingResourceException {
        
        // Find the calendar's class name, which we're going to use to construct the
        // resource bundle name.
        String fullName = calendarClass.getName();
        int lastDot = fullName.lastIndexOf('.');
        String className = fullName.substring(lastDot+1);

        String bundleName = className + "Symbols";

        UResourceBundle result = null;
        try {
            result = UResourceBundle.getBundleInstance(bundleName, locale);
        }
        catch (MissingResourceException e) {
            ///CLOVER:OFF
            // coverage requires test without data, so skip
            //if (!(cal instanceof GregorianCalendar)) {
            if (!(GregorianCalendar.class.isAssignableFrom(calendarClass))) {
                // Ok for symbols to be missing for a Gregorian calendar, but
                // not for any other type.
                throw e;
            }
            ///CLOVER:ON
        }
        return result;
    }

    /**
     * Variant of getDateFormatBundle(java.lang.Class, java.util.Locale) that takes
     * a Calendar instance instead of a Calendar class.
     * @see #getDateFormatBundle(java.lang.Class, java.util.Locale)
     * @stable ICU 2.2
     */
    public static ResourceBundle getDateFormatBundle(Calendar cal, Locale locale)
        throws MissingResourceException {
        return getDateFormatBundle(cal.getClass(), ULocale.forLocale(locale));
    }
    
    /**
     * Variant of getDateFormatBundle(java.lang.Class, java.util.Locale) that takes
     * a Calendar instance instead of a Calendar class.
     * @see #getDateFormatBundle(java.lang.Class, java.util.Locale)
     * @draft ICU 3.2
     * @provisional This API might change or be removed in a future release.
     */
    public static ResourceBundle getDateFormatBundle(Calendar cal, ULocale locale)
        throws MissingResourceException {
        return getDateFormatBundle(cal.getClass(), locale);
    }
    
    // -------- BEGIN ULocale boilerplate --------

    /**
     * Return the locale that was used to create this object, or null.
     * This may may differ from the locale requested at the time of
     * this object's creation.  For example, if an object is created
     * for locale <tt>en_US_CALIFORNIA</tt>, the actual data may be
     * drawn from <tt>en</tt> (the <i>actual</i> locale), and
     * <tt>en_US</tt> may be the most specific locale that exists (the
     * <i>valid</i> locale).
     *
     * <p>Note: This method will be implemented in ICU 3.0; ICU 2.8
     * contains a partial preview implementation.  The * <i>actual</i>
     * locale is returned correctly, but the <i>valid</i> locale is
     * not, in most cases.
     * @param type type of information requested, either {@link
     * com.ibm.icu.util.ULocale#VALID_LOCALE} or {@link
     * com.ibm.icu.util.ULocale#ACTUAL_LOCALE}.
     * @return the information specified by <i>type</i>, or null if
     * this object was not constructed from locale data.
     * @see com.ibm.icu.util.ULocale
     * @see com.ibm.icu.util.ULocale#VALID_LOCALE
     * @see com.ibm.icu.util.ULocale#ACTUAL_LOCALE
     * @draft ICU 2.8 (retain)
     * @provisional This API might change or be removed in a future release.
     */
    public final ULocale getLocale(ULocale.Type type) {
        return type == ULocale.ACTUAL_LOCALE ?
            this.actualLocale : this.validLocale;
    }

    /**
     * Set information about the locales that were used to create this
     * object.  If the object was not constructed from locale data,
     * both arguments should be set to null.  Otherwise, neither
     * should be null.  The actual locale must be at the same level or
     * less specific than the valid locale.  This method is intended
     * for use by factories or other entities that create objects of
     * this class.
     * @param valid the most specific locale containing any resource
     * data, or null
     * @param actual the locale containing data used to construct this
     * object, or null
     * @see com.ibm.icu.util.ULocale
     * @see com.ibm.icu.util.ULocale#VALID_LOCALE
     * @see com.ibm.icu.util.ULocale#ACTUAL_LOCALE
     * @internal
     */
    final void setLocale(ULocale valid, ULocale actual) {
        // Change the following to an assertion later
        if ((valid == null) != (actual == null)) {
            ///CLOVER:OFF
            throw new IllegalArgumentException();
            ///CLOVER:ON
        }
        // Another check we could do is that the actual locale is at
        // the same level or less specific than the valid locale.
        this.validLocale = valid;
        this.actualLocale = actual;
    }

    /**
     * The most specific locale containing any resource data, or null.
     * @see com.ibm.icu.util.ULocale
     * @internal
     */
    private ULocale validLocale;

    /**
     * The locale containing data used to construct this object, or
     * null.
     * @see com.ibm.icu.util.ULocale
     * @internal
     */
    private ULocale actualLocale;

    // -------- END ULocale boilerplate --------
}