/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.runtime.memoize

import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Vaclav Pech
 */

public class MemoizeBetweenTest extends AbstractMemoizeTestCase {

    Closure buildMemoizeClosure(Closure cl) {
        cl.memoizeBetween(50, 100)
    }

    public void testParameters() {
        Closure cl = {}
        shouldFail(IllegalArgumentException) {
            cl.memoizeBetween(1, 0)
        }
        shouldFail(IllegalArgumentException) {
            cl.memoizeBetween(-2, -1)
        }
        shouldFail(IllegalArgumentException) {
            cl.memoizeBetween(-1, -1)
        }
    }

    public void testZeroCache() {
        def flag = false
        Closure cl = {
            flag = true
            it * 2
        }
        Closure mem = cl.memoizeBetween(0, 0)
        [1, 2, 3, 4, 5, 6].each {mem(it)}
        assert flag
        flag = false
        assert 12 == mem(6)
        assert flag
    }

    public void testLRUCache() {
        def flag = false
        Closure cl = {
            flag = true
            it * 2
        }
        Closure mem = cl.memoizeBetween(3, 3)
        [1, 2, 3, 4, 5, 6].each {mem(it)}
        assert flag
        flag = false
        assert 8 == mem(4)
        assert 10 == mem(5)
        assert 12 == mem(6)
        assert !flag
        assert 6 == mem(3)
        assert flag
        flag = false
        assert 10 == mem(5)
        assert 12 == mem(6)
        assert 6 == mem(3)
        assert !flag
        assert 8 == mem(4)
        assert flag

        flag = false
        assert 10 == mem(5)
        assert flag
    }

    public void testMemoizeBetweenConcurrently() {
        AtomicInteger cnt = new AtomicInteger(0)
        Closure cl = {
            cnt.incrementAndGet()
            it * 2
        }
        Closure mem = cl.memoizeBetween(3, 3)
        [4, 5, 6, 4, 5, 6, 4, 5, 6, 4, 5, 6].collect { num -> Thread.start { mem(num) } }*.join()

        assert 3 == cnt.get()
    }
}
