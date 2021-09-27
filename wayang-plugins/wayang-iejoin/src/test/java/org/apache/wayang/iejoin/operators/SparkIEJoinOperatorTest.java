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

package org.apache.wayang.iejoin.operators;

import java.util.Arrays;
import java.util.List;
import org.apache.wayang.basic.data.Record;
import org.apache.wayang.basic.data.Tuple2;
import org.apache.wayang.core.function.TransformationDescriptor;
import org.apache.wayang.core.platform.ChannelInstance;
import org.apache.wayang.core.types.DataSetType;
import org.apache.wayang.core.types.DataUnitType;
import org.apache.wayang.spark.channels.RddChannel;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test suite for {@link SparkIEJoinOperator}.
 */
public class SparkIEJoinOperatorTest extends SparkOperatorTestBase {


    @Test
    public void testExecution() {
        Record r1 = new Record(100, 10);
        Record r2 = new Record(200, 20);
        Record r3 = new Record(300, 30);
        Record r11 = new Record(250, 5);
        // Prepare test data.
        RddChannel.Instance input0 = this.createRddChannelInstance(Arrays.asList(r1, r2, r3, r11));
        RddChannel.Instance input1 = this.createRddChannelInstance(Arrays.asList(r1, r2, r3));
        RddChannel.Instance output = this.createRddChannelInstance();

        // Build the Cartesian operator.
        SparkIEJoinOperator<Integer, Integer, Record> IEJoinOperator =
                new SparkIEJoinOperator<Integer, Integer, Record>(
                        DataSetType.createDefaultUnchecked(Record.class),
                        DataSetType.createDefaultUnchecked(Record.class),
                        //0, 0, JoinCondition.GreaterThan, 1, 1, JoinCondition.LessThan
                        new TransformationDescriptor<Record, Integer>(word -> (Integer) word.getField(0),
                                DataUnitType.<Record>createBasic(Record.class),
                                DataUnitType.<Integer>createBasicUnchecked(Integer.class)
                        ),
                        new TransformationDescriptor<Record, Integer>(word -> (Integer) word.getField(0),
                                DataUnitType.<Record>createBasic(Record.class),
                                DataUnitType.<Integer>createBasicUnchecked(Integer.class)
                        ),
                        IEJoinMasterOperator.JoinCondition.GreaterThan,
                        new TransformationDescriptor<Record, Integer>(word -> (Integer) word.getField(1),
                                DataUnitType.<Record>createBasic(Record.class),
                                DataUnitType.<Integer>createBasicUnchecked(Integer.class)
                        ),
                        new TransformationDescriptor<Record, Integer>(word -> (Integer) word.getField(1),
                                DataUnitType.<Record>createBasic(Record.class),
                                DataUnitType.<Integer>createBasicUnchecked(Integer.class)
                        ),
                        IEJoinMasterOperator.JoinCondition.LessThan
                );

        // Set up the ChannelInstances.
        final ChannelInstance[] inputs = new ChannelInstance[]{input0, input1};
        final ChannelInstance[] outputs = new ChannelInstance[]{output};

        // Execute.
        evaluate(IEJoinOperator, inputs, outputs);

        // Verify the outcome.
        final List<Tuple2<Record, Record>> result = output.<Tuple2<Record, Record>>provideRdd().collect();
        Assert.assertEquals(2, result.size());
        //Assert.assertEquals(result.get(0), new Tuple2(1, "a"));

    }

}
