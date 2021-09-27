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

package org.apache.wayang.core.util.mathex.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import org.apache.wayang.core.util.mathex.Context;
import org.apache.wayang.core.util.mathex.Expression;

/**
 * {@link Expression} implementation that represents a function with a static implementation.
 */
public class CompiledFunction implements Expression {

    /**
     * The name of the function.
     */
    final String name;

    /**
     * The implementation of this instance.
     */
    final ToDoubleFunction<double[]> implementation;

    /**
     * The argument {@link Expression}s.
     */
    final List<Expression> arguments;

    public CompiledFunction(String name, ToDoubleFunction<double[]> implementation, List<Expression> arguments) {
        this.name = name;
        this.implementation = implementation;
        this.arguments = arguments;
    }

    @Override
    public double evaluate(Context context) {
        // Evaluate the arguments.
        double[] args = new double[this.arguments.size()];
        int i = 0;
        for (Expression argument : this.arguments) {
            args[i++] = argument.evaluate(context);
        }

        // Apply the function.
        return this.implementation.applyAsDouble(args);
    }

    @Override
    public Expression specify(Context context) {
        final Expression specification = Expression.super.specify(context);
        if (specification == this) {
            List<Expression> specifiedArgs = new ArrayList<>(this.arguments.size());
            boolean isAnySpecified = false;
            for (Expression argument : this.arguments) {
                final Expression specifiedArg = argument.specify(context);
                isAnySpecified |= specifiedArg != argument;
                specifiedArgs.add(specifiedArg);
            }
            if (isAnySpecified) {
                return new CompiledFunction(this.name, this.implementation, specifiedArgs);
            }
        }
        return specification;
    }

    @Override
    public String toString() {
        return this.name + this.arguments.stream().map(Object::toString).collect(Collectors.joining(", ", "(", ")"));
    }
}
