package com.cloudhopper.smpp.util;

/*
 * #%L
 * ch-smpp
 * %%
 * Copyright (C) 2009 - 2012 Cloudhopper by Twitter
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/**
 * Utility class for atomically generating SMPP PDU sequence numbers.  This
 * implementation will atomically increment the sequence number and wrap it
 * around back to 1 when it hits the max 0x7FFFFFFF.
 * 
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
public class SequenceNumber {

    // AT&T Wireless uses a sequence number of 0 to start. This violates SMPP 3.4
    // specifications of the valid range of sequence numbers, but we'll need
    // to permit it.
    // Update: Removed the top nibble since the range of sequence numbers is 
    //         prepended with the INSTANCE_INDEX (see below).
    public static final int MIN_VALUE = 0x0000000;
    public static final int DEFAULT_VALUE = 0x0000001;
    public static final int MAX_VALUE = 0xFFFFFFF;

    /*
     * Handlers for multiple binds will have their own instance of SequenceNumber
     * to generate sequence numbers of requests.  To be able to map back to 
     * correct request for DRs, each instance of SequenceNumber will need to use
     * a different range.  The instanceIndex will be used for the top nibble of
     * the sequence number generated.  This ensures that each instance will
     * generate a different set of sequence numbers (up to a maximum of 16 
     * instances).
     */
    public static int instanceIndex = 0;
    public int rangeNibble;

    private int value;

    public SequenceNumber() {
        this.rangeNibble = instanceIndex;
        this.value = DEFAULT_VALUE;
        instanceIndex++;
    }

    public SequenceNumber(int initialValue) throws InvalidSequenceNumberException {
        this.rangeNibble = instanceIndex;
        assertValid(initialValue);
        this.value = initialValue;
        instanceIndex++;
    }

    public static int concatSeqNo(int upper, int lower) {
        return ((upper % 16) << 28) + (lower % (MAX_VALUE+1));
    }
    /**
     * Get the next number in this sequence's scheme. This method is synchronized
     * so its safe for multiple threads to call.
     */
    synchronized public int next() {
        // the next value is the current value
        int nextValue = this.value;

        if (this.value == MAX_VALUE) {
            // wrap this around back to 1
            this.value = DEFAULT_VALUE;
        } else {
            this.value++;
        }

        return concatSeqNo(this.rangeNibble, nextValue);
    }

    /**
     * Get the next number in this sequence's scheme without causing it to move
     * to the next-in-sequence. This method returns the number that will be
     * returned by the next call to <code>next()</code> without actually
     * increasing the sequence. Multiple calls to <code>peek</code> will
     * return the same number until a call to <code>next()</code> is made.
     */
    synchronized public int peek() {
        return concatSeqNo(this.rangeNibble, this.value);
    }

    /**
     * Reset the sequence scheme to the beginning of the sequence (min value
     * which is 1).
     */
    synchronized public void reset() {
        this.value = DEFAULT_VALUE;
    }

    static public void assertValid(int sequenceNumber) throws InvalidSequenceNumberException {
        // turns out that some operators ignore the specifications and actually
        // use all 32 bits of the sequence number -- instead of validating it
        // we'll just assert that everything is valid
        /**
        if (sequenceNumber < MIN_VALUE || sequenceNumber > MAX_VALUE) {
            throw new InvalidSequenceNumberException("Sequence number [" + sequenceNumber + "] is not in range from " + MIN_VALUE + " to " + MAX_VALUE);
        }
         */
    }
}

