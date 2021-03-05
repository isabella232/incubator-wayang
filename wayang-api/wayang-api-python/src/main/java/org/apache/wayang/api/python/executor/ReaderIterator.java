/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.wayang.api.python.executor;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ReaderIterator <Output> implements Iterator<Output> {

    private Output nextObj = null;
    private boolean eos = false;
    private DataInputStream stream = null;

    public ReaderIterator(DataInputStream stream) {
        this.stream = stream;
    }

    private Output read() {

        int END_OF_DATA_SECTION = -1;

        try {
            int length = this.stream.readInt();

            if (length > 0) {
                byte[] obj = new byte[length];
                stream.readFully(obj);
                String s = new String(obj, StandardCharsets.UTF_8);
                Output it = (Output) s;
                return it;
            } else if (length == END_OF_DATA_SECTION) {
                eos = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean hasNext() {
        boolean fst = nextObj != null;

        if(fst) return true;

        if(!eos){
            nextObj = read();
            return !eos;
        }

        return false;
    }

    @Override
    public Output next() {

        if(hasNext()){
            Output obj = nextObj;
            nextObj = null;
            return obj;
        }

        throw new NoSuchElementException();
    }

}