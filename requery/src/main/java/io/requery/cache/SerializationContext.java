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

package io.requery.cache;

import io.requery.meta.EntityModel;
import io.requery.meta.Type;
import io.requery.util.ClassMap;

class SerializationContext {

    private static final ClassMap<Type<?>> types = new ClassMap<>();

    public static void map(EntityModel model) {
        for (Type<?> type : model.getTypes()) {
            types.put(type.getClassType(), type);
        }
    }

    public static <E> Type<E> getType(Class<E> entityClass) {
        @SuppressWarnings("unchecked")
        Type<E> type = (Type<E>) types.get(entityClass);
        if (type == null) {
            throw new IllegalStateException();
        }
        return type;
    }

    private SerializationContext() {
    }
}
