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

package io.requery.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

class EntityNameResolver {

    private final EntityGraph graph;

    EntityNameResolver(EntityGraph graph) {
        this.graph = graph;
    }

    ClassName typeNameOf(EntityDescriptor type) {
        return ClassName.bestGuess(type.typeName().toString());
    }

    ClassName embeddedTypeNameOf(EntityDescriptor embedded, EntityDescriptor parent) {
        String className = parent.typeName().className() + "_" + embedded.typeName().className();
        return ClassName.get(parent.typeName().packageName(), className);
    }

    TypeName tryGeneratedTypeName(TypeMirror typeMirror) {
        Optional<ClassName> name = generatedTypeNameOf(typeMirror);
        return name.isPresent() ? name.get() : TypeName.get(typeMirror);
    }

    Optional<ClassName> generatedTypeNameOf(TypeMirror typeMirror) {
        // if it's a generated type, used the generated type name (not the abstract one)
        return graph.entities().stream()
            .filter(entity -> entity.typeName().className().equals(typeMirror.toString()))
            .map(this::typeNameOf).findFirst();
    }

    Optional<ClassName> generatedTypeNameOf(TypeElement typeElement) {
        return graph.entities().stream()
            .filter(entity -> entity.element().getQualifiedName()
                .equals(typeElement.getQualifiedName()))
            .map(this::typeNameOf).findFirst();
    }

    ClassName joinEntityName(AssociativeEntityDescriptor descriptor,
                             EntityDescriptor a,
                             EntityDescriptor b) {
        if (descriptor.type().isPresent()) {
            return descriptor.type()
                .flatMap(this::generatedTypeNameOf)
                .orElse(ClassName.bestGuess(descriptor.name()));
        }
        String className;
        if (Names.isEmpty(descriptor.name())) {
            className = a.typeName().className() + "_" + b.typeName().className();
        } else {
            className = Names.upperCaseFirst(descriptor.name());
        }
        return ClassName.get(a.typeName().packageName(), className);
    }
}
