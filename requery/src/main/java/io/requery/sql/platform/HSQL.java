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

package io.requery.sql.platform;

import io.requery.query.element.LimitedElement;
import io.requery.query.function.Function;
import io.requery.query.function.Random;
import io.requery.sql.Mapping;
import io.requery.sql.gen.LimitGenerator;
import io.requery.sql.gen.Generator;

/**
 * platform configuration for HSQLDB.
 */
public class HSQL extends Generic {

    public HSQL() {
    }

    @Override
    public void addMappings(Mapping mapping) {
        mapping.aliasFunction(new Function.Name("rand"), Random.class);
    }

    @Override
    public Generator<LimitedElement> limitGenerator() {
        return new LimitGenerator();
    }

    @Override
    public boolean supportsGeneratedColumnsInPrepareStatement() {
        return false;
    }
}
