/*
 *******************************************************************************
 * Copyright (C) 1996-2003, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/dev/test/calendar/AstroTest.java,v $ 
 * $Date: 2003/10/02 20:50:57 $ 
 * $Revision: 1.14 $
 *
 *****************************************************************************************
 */
package com.ibm.icu.dev.test.calendar;

// AstroTest

import java.util.Date;
import java.util.Locale;

import com.ibm.icu.dev.test.*;
import com.ibm.icu.util.*;
import com.ibm.icu.util.CalendarAstronomer.*;
import com.ibm.icu.text.DateFormat;

// TODO: try finding next new moon after  07/28/1984 16:00 GMT

public class AstroTest extends TestFmwk {
    public static void main(String[] args) throws Exception {
        new AstroTest().run(args);
    }

    static final double PI = Math.PI;
    
    static GregorianCalendar gc = new GregorianCalendar(new SimpleTimeZone(0, "UTC"));
    static CalendarAstronomer astro = new CalendarAstronomer();
    
    public void TestSolarLongitude() {
        final double tests[][] = {
            { 1980, 7, 27, 00, 00, 124.114347 },
            { 1988, 7, 27, 00, 00, 124.187732 },
        };
        logln("");
        for (int i = 0; i < tests.length; i++) {
            gc.clear();
            gc.set((int)tests[i][0], (int)tests[i][1]-1, (int)tests[i][2], (int)tests[i][3], (int) tests[i][4]);
            
            astro.setDate(gc.getTime());
            
            double longitude = astro.getSunLongitude();
            longitude = 0;
            Equatorial result = astro.getSunPosition();
            result = null;
        }
    }
    
    public void TestLunarPosition() {
        final double tests[][] = {
            { 1979, 2, 26, 16, 00,  0, 0 },
        };
        logln("");
        
        for (int i = 0; i < tests.length; i++) {
            gc.clear();
            gc.set((int)tests[i][0], (int)tests[i][1]-1, (int)tests[i][2], (int)tests[i][3], (int) tests[i][4]);
            astro.setDate(gc.getTime());
            
            Equatorial result = astro.getMoonPosition();
            result = null;
        }

    }
    
    public void TestCoordinates() {
        Equatorial result = astro.eclipticToEquatorial(139.686111 * PI/ 180.0, 4.875278* PI / 180.0);
        logln("result is " + result + ";  " + result.toHmsString());
    }
    
    public void TestCoverage() {
	GregorianCalendar cal = new GregorianCalendar(1958, Calendar.AUGUST, 15);
	Date then = cal.getTime();
	CalendarAstronomer myastro = new CalendarAstronomer(then);

	//Latitude:  34 degrees 05' North  
	//Longitude:  118 degrees 22' West  
	double laLat = 34 + 5d/60, laLong = 360 - (118 + 22d/60);
	CalendarAstronomer myastro2 = new CalendarAstronomer(laLong, laLat);

	double eclLat = laLat * Math.PI / 360;
	double eclLong = laLong * Math.PI / 360;
	Ecliptic ecl = new Ecliptic(eclLat, eclLong);
	logln("ecliptic: " + ecl);

	CalendarAstronomer myastro3 = new CalendarAstronomer();
	myastro3.setJulianDay((4713 + 2000) * 365.25);

	CalendarAstronomer[] astronomers = {
	    myastro, myastro2, myastro3, myastro2 // check cache
	};

	for (int i = 0; i < astronomers.length; ++i) {
	    CalendarAstronomer astro = astronomers[i];

	    logln("astro: " + astro);
	    logln("   time: " + astro.getTime());
	    logln("   date: " + astro.getDate());
	    logln("   cent: " + astro.getJulianCentury());
	    logln("   gw sidereal: " + astro.getGreenwichSidereal());
	    logln("   loc sidereal: " + astro.getLocalSidereal());
	    logln("   equ ecl: " + astro.eclipticToEquatorial(ecl));
	    logln("   equ long: " + astro.eclipticToEquatorial(eclLong));
	    logln("   horiz: " + astro.eclipticToHorizon(eclLong));
	    logln("   sunrise: " + new Date(astro.getSunRiseSet(true)));
	    logln("   sunset: " + new Date(astro.getSunRiseSet(false)));
	    logln("   moon phase: " + astro.getMoonPhase());
	    logln("   moonrise: " + new Date(astro.getMoonRiseSet(true)));
	    logln("   moonset: " + new Date(astro.getMoonRiseSet(false)));
	    logln("   prev summer solstice: " + new Date(astro.getSunTime(CalendarAstronomer.SUMMER_SOLSTICE, false)));
	    logln("   next summer solstice: " + new Date(astro.getSunTime(CalendarAstronomer.SUMMER_SOLSTICE, true)));
	    logln("   prev full moon: " + new Date(astro.getMoonTime(CalendarAstronomer.FULL_MOON, false)));
	    logln("   next full moon: " + new Date(astro.getMoonTime(CalendarAstronomer.FULL_MOON, true)));
	}
    }

    static final long DAY_MS = 24*60*60*1000L;

    public void TestSunriseTimes() {
        //        logln("Sunrise/Sunset times for San Jose, California, USA");
        //        CalendarAstronomer astro = new CalendarAstronomer(-121.55, 37.20);
        //        TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");

        // We'll use a table generated by the UNSO website as our reference
        // From: http://aa.usno.navy.mil/
        //-Location: W079 25, N43 40
        //-Rise and Set for the Sun for 2001
        //-Zone:  4h West of Greenwich
        int[] USNO = {
             6,59, 19,45,
             6,57, 19,46,
             6,56, 19,47,
             6,54, 19,48,
             6,52, 19,49,
             6,50, 19,51,
             6,48, 19,52,
             6,47, 19,53,
             6,45, 19,54,
             6,43, 19,55,
             6,42, 19,57,
             6,40, 19,58,
             6,38, 19,59,
             6,36, 20, 0,
             6,35, 20, 1,
             6,33, 20, 3,
             6,31, 20, 4,
             6,30, 20, 5,
             6,28, 20, 6,
             6,27, 20, 7,
             6,25, 20, 8,
             6,23, 20,10,
             6,22, 20,11,
             6,20, 20,12,
             6,19, 20,13,
             6,17, 20,14,
             6,16, 20,16,
             6,14, 20,17,
             6,13, 20,18,
             6,11, 20,19,
        };

        logln("Sunrise/Sunset times for Toronto, Canada");
        CalendarAstronomer astro = new CalendarAstronomer(-(79+25/60), 43+40/60);
        TimeZone tz = TimeZone.getTimeZone("America/Montreal");

        GregorianCalendar cal = new GregorianCalendar(tz, Locale.US);
        GregorianCalendar cal2 = new GregorianCalendar(tz, Locale.US);
        cal.clear();
        cal.set(Calendar.YEAR, 2001);
        cal.set(Calendar.MONTH, Calendar.APRIL);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 12); // must be near local noon for getSunRiseSet to work

        DateFormat df = DateFormat.getTimeInstance(cal, DateFormat.MEDIUM, Locale.US);
        DateFormat df2 = DateFormat.getDateTimeInstance(cal, DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US);
        DateFormat day = DateFormat.getDateInstance(cal, DateFormat.MEDIUM, Locale.US);
				
        for (int i=0; i < 30; i++) {
            astro.setDate(cal.getTime());
			
            Date sunrise = new Date(astro.getSunRiseSet(true));
            Date sunset = new Date(astro.getSunRiseSet(false));

            cal2.setTime(cal.getTime());
            cal2.set(Calendar.SECOND,      0);
            cal2.set(Calendar.MILLISECOND, 0);

            cal2.set(Calendar.HOUR_OF_DAY, USNO[4*i+0]);
            cal2.set(Calendar.MINUTE,      USNO[4*i+1]);
            Date exprise = cal2.getTime();
            cal2.set(Calendar.HOUR_OF_DAY, USNO[4*i+2]);
            cal2.set(Calendar.MINUTE,      USNO[4*i+3]);
            Date expset = cal2.getTime();
            // Compute delta of what we got to the USNO data, in seconds
            int deltarise = Math.abs((int)(sunrise.getTime() - exprise.getTime()) / 1000);
            int deltaset = Math.abs((int)(sunset.getTime() - expset.getTime()) / 1000);

            // Allow a deviation of 0..MAX_DEV seconds
            // It would be nice to get down to 60 seconds, but at this
            // point that appears to be impossible without a redo of the
            // algorithm using something more advanced than Duffett-Smith.
            final int MAX_DEV = 180;
            if (deltarise > MAX_DEV || deltaset > MAX_DEV) {
                if (deltarise > MAX_DEV) {
                    errln("FAIL: " + day.format(cal.getTime()) +
                          ", Sunrise: " + df2.format(sunrise) +
                          " (USNO " + df.format(exprise) +
                          " d=" + deltarise + "s)");
                } else {
                    logln(day.format(cal.getTime()) +
                          ", Sunrise: " + df.format(sunrise) +
                          " (USNO " + df.format(exprise) + ")");
                }
                if (deltaset > MAX_DEV) {
                    errln("FAIL: " + day.format(cal.getTime()) +
                          ", Sunset: " + df2.format(sunset) +
                          " (USNO " + df.format(expset) +
                          " d=" + deltaset + "s)");
                } else {
                    logln(day.format(cal.getTime()) +
                          ", Sunset: " + df.format(sunset) +
                          " (USNO " + df.format(expset) + ")");
                }
            } else {
                logln(day.format(cal.getTime()) +
                      ", Sunrise: " + df.format(sunrise) +
                      " (USNO " + df.format(exprise) + ")" +
                      ", Sunset: " + df.format(sunset) +
                      " (USNO " + df.format(expset) + ")");
            }
            cal.add(Calendar.DATE, 1);
        }		

//		CalendarAstronomer a = new CalendarAstronomer(-(71+5/60), 42+37/60);
//		cal.clear();
//		cal.set(cal.YEAR, 1986);
//		cal.set(cal.MONTH, cal.MARCH);
//		cal.set(cal.DATE, 10);		
//		cal.set(cal.YEAR, 1988);
//		cal.set(cal.MONTH, cal.JULY);
//		cal.set(cal.DATE, 27);		
//		a.setDate(cal.getTime());
//		long r = a.getSunRiseSet2(true);		
    }

    public void TestBasics() {
        // Check that our JD computation is the same as the book's (p. 88)
        GregorianCalendar cal3 = new GregorianCalendar(TimeZone.getTimeZone("GMT"), Locale.US);
        DateFormat d3 = DateFormat.getDateTimeInstance(cal3, DateFormat.MEDIUM,DateFormat.MEDIUM,Locale.US);
        cal3.clear();
        cal3.set(Calendar.YEAR, 1980);
        cal3.set(Calendar.MONTH, Calendar.JULY);
        cal3.set(Calendar.DATE, 27);
        astro.setDate(cal3.getTime());
        double jd = astro.getJulianDay() - 2447891.5;
        double exp = -3444;
        if (jd == exp) {
            logln(d3.format(cal3.getTime()) + " => " + jd);
        } else {
            errln("FAIL: " + d3.format(cal3.getTime()) + " => " + jd +
                  ", expected " + exp);
        }

//        cal3.clear();
//        cal3.set(cal3.YEAR, 1990);
//        cal3.set(cal3.MONTH, Calendar.JANUARY);
//        cal3.set(cal3.DATE, 1);
//        cal3.add(cal3.DATE, -1);
//        astro.setDate(cal3.getTime());
//        astro.foo();
    }
}
