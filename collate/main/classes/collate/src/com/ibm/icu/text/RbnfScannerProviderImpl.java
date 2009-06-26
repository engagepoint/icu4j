/*
*******************************************************************************
* Copyright (C) 2009, International Business Machines Corporation and         *
* others. All Rights Reserved.                                                *
*******************************************************************************
*/

package com.ibm.icu.text;

import com.ibm.icu.util.ULocale;

import java.util.HashMap;
import java.util.Map;

public class RbnfScannerProviderImpl implements RbnfLenientScannerProvider {
    private Map<String, RbnfLenientScanner> cache;

    public RbnfScannerProviderImpl() {
        cache = new HashMap<String, RbnfLenientScanner>();
    }

    public RbnfLenientScanner get(ULocale locale, String extras) {
        RbnfLenientScanner result = null;
        String key = locale.toString() + "/" + extras;
        synchronized(cache) {
            result = cache.get(key);
            if (result != null) {
                return result;
            }
        }
        result = createScanner(locale, extras);
        synchronized(cache) {
            cache.put(key, result);
        }
        return result;
    }

    protected RbnfLenientScanner createScanner(ULocale locale, String extras) {
        RuleBasedCollator collator = null;
        try {
            // create a default collator based on the locale,
            // then pull out that collator's rules, append any additional
            // rules specified in the description, and create a _new_
            // collator based on the combination of those rules
            collator = (RuleBasedCollator)Collator.getInstance(locale.toLocale());
            if (extras != null) {
                String rules = collator.getRules() + extras;
                collator = new RuleBasedCollator(rules);
            }
            collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        }
        catch (Exception e) {
            // If we get here, it means we have a malformed set of
            // collation rules, which hopefully won't happen
            if (true){ // debug hook
                e.printStackTrace();
            }
            collator = null;
        }

        return new RbnfLenientScannerImpl(collator);
    }

    private static class RbnfLenientScannerImpl implements RbnfLenientScanner {
        private final RuleBasedCollator collator;

        private RbnfLenientScannerImpl(RuleBasedCollator rbc) {
            this.collator = rbc;
        }

        public boolean allIgnorable(String s) {
            CollationElementIterator iter = collator.getCollationElementIterator(s);

            int o = iter.next();
            while (o != CollationElementIterator.NULLORDER
                   && CollationElementIterator.primaryOrder(o) == 0) {
                o = iter.next();
            }
            return o == CollationElementIterator.NULLORDER;
        }

        public int[] findText(String str, String key, int startingAt) {
            int p = startingAt;
            int keyLen = 0;

            // basically just isolate smaller and smaller substrings of
            // the target string (each running to the end of the string,
            // and with the first one running from startingAt to the end)
            // and then use prefixLength() to see if the search key is at
            // the beginning of each substring.  This is excruciatingly
            // slow, but it will locate the key and tell use how long the
            // matching text was.
            while (p < str.length() && keyLen == 0) {
                keyLen = prefixLength(str.substring(p), key);
                if (keyLen != 0) {
                    return new int[] { p, keyLen };
                }
                ++p;
            }
            // if we make it to here, we didn't find it.  Return -1 for the
            // location.  The length should be ignored, but set it to 0,
            // which should be "safe"
            return new int[] { -1, 0 };
        }

        public int[] findText2(String str, String key, int startingAt) {

            CollationElementIterator strIter = collator.getCollationElementIterator(str);
            CollationElementIterator keyIter = collator.getCollationElementIterator(key);

            int keyStart = -1;

            strIter.setOffset(startingAt);

            int oStr = strIter.next();
            int oKey = keyIter.next();
            while (oKey != CollationElementIterator.NULLORDER) {
                while (oStr != CollationElementIterator.NULLORDER &&
                       CollationElementIterator.primaryOrder(oStr) == 0)
                    oStr = strIter.next();

                while (oKey != CollationElementIterator.NULLORDER &&
                       CollationElementIterator.primaryOrder(oKey) == 0)
                    oKey = keyIter.next();

                if (oStr == CollationElementIterator.NULLORDER) {
                    return new int[] { -1, 0 };
                }

                if (oKey == CollationElementIterator.NULLORDER) {
                    break;
                }

                if (CollationElementIterator.primaryOrder(oStr) ==
                    CollationElementIterator.primaryOrder(oKey)) {
                    keyStart = strIter.getOffset();
                    oStr = strIter.next();
                    oKey = keyIter.next();
                } else {
                    if (keyStart != -1) {
                        keyStart = -1;
                        keyIter.reset();
                    } else {
                        oStr = strIter.next();
                    }
                }
            }

            if (oKey == CollationElementIterator.NULLORDER) {
                return new int[] { keyStart, strIter.getOffset() - keyStart };
            }

            return new int[] { -1, 0 };
        }

        public int prefixLength(String str, String prefix) {
            // Create two collation element iterators, one over the target string
            // and another over the prefix.
            //
            // Previous code was matching "fifty-" against " fifty" and leaving
            // the number " fifty-7" to parse as 43 (50 - 7).
            // Also it seems that if we consume the entire prefix, that's ok even
            // if we've consumed the entire string, so I switched the logic to
            // reflect this.

            CollationElementIterator strIter = collator.getCollationElementIterator(str);
            CollationElementIterator prefixIter = collator.getCollationElementIterator(prefix);

            // match collation elements between the strings
            int oStr = strIter.next();
            int oPrefix = prefixIter.next();

            while (oPrefix != CollationElementIterator.NULLORDER) {
                // skip over ignorable characters in the target string
                while (CollationElementIterator.primaryOrder(oStr) == 0 && oStr !=
                       CollationElementIterator.NULLORDER) {
                    oStr = strIter.next();
                }

                // skip over ignorable characters in the prefix
                while (CollationElementIterator.primaryOrder(oPrefix) == 0 && oPrefix !=
                       CollationElementIterator.NULLORDER) {
                    oPrefix = prefixIter.next();
                }

                // if skipping over ignorables brought to the end of
                // the prefix, we DID match: drop out of the loop
                if (oPrefix == CollationElementIterator.NULLORDER) {
                    break;
                }

                // if skipping over ignorables brought us to the end
                // of the target string, we didn't match and return 0
                if (oStr == CollationElementIterator.NULLORDER) {
                    return 0;
                }

                // match collation elements from the two strings
                // (considering only primary differences).  If we
                // get a mismatch, dump out and return 0
                if (CollationElementIterator.primaryOrder(oStr) != 
                    CollationElementIterator.primaryOrder(oPrefix)) {
                    return 0;
                }

                // otherwise, advance to the next character in each string
                // and loop (we drop out of the loop when we exhaust
                // collation elements in the prefix)

                oStr = strIter.next();
                oPrefix = prefixIter.next();
            }

            int result = strIter.getOffset();
            if (oStr != CollationElementIterator.NULLORDER) {
                --result;
            }
            return result;
        }
    }
}