/**
 * Copyright (C) 2011 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.cloudhopper.smpp.util;

// third party imports
import org.junit.*;

// my imports

/**
 *
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
public class SequenceNumberTest {
    
    @Test
    public void usage() throws Exception {
        SequenceNumber seqNum = new SequenceNumber();
        Assert.assertEquals(1, seqNum.next());
        Assert.assertEquals(2, seqNum.next());
        Assert.assertEquals(3, seqNum.next());
        Assert.assertEquals(4, seqNum.next());

        // This is the second initialization so it will have a range
        // of [0x10000000, 0x20000000).
        seqNum = new SequenceNumber(0xFFFFFFF);
        Assert.assertEquals(0x1FFFFFFF, seqNum.next());
        Assert.assertEquals(0x10000001, seqNum.next());  // wrap around
        Assert.assertEquals(0x10000002, seqNum.next());
        Assert.assertEquals(0x10000003, seqNum.next());

        Assert.assertEquals(0x10000004, seqNum.peek());

        seqNum.reset();
        
        Assert.assertEquals(0x10000001, seqNum.peek());
        Assert.assertEquals(0x10000001, seqNum.next());
        Assert.assertEquals(0x10000002, seqNum.next());
        Assert.assertEquals(0x10000003, seqNum.next());
    }
}
