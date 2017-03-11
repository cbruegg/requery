/*
 * Copyright 2017 requery.io
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

package io.requery.query;

/**
 * Generic expression interface.
 *
 * @author Nikhil Purushe
 *
 * @param <V> type of the expression
 */
public interface Expression<V> {

    /**
     * @return identifier of the expression
     */
    String getName();

    /**
     * @return {@link Class} that results when the expression is evaluated
     */
    Class<V> getClassType();

    /**
     * @return type of the expression
     */
    ExpressionType getExpressionType();

    /**
     * @return inner expression contained by this instance or null if none
     */
    Expression<V> getInnerExpression();
}
