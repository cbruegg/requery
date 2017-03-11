/*
 * Copyright 2016 requery.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.requery.query.function;

import io.requery.query.Expression;

public class Coalesce<T> extends Function<T> {

    private final Expression<?>[] expressions;

    private Coalesce(Expression<T>[] attributes) {
        super("coalesce", attributes[0].getClassType());
        this.expressions = attributes;
    }

    @SafeVarargs
    public static <C> Coalesce<C> coalesce(Expression<C>... attributes) {
        return new Coalesce<>(attributes);
    }

    @Override
    public Expression<?>[] arguments() {
        return expressions;
    }
}
