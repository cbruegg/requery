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

package io.requery.test.jpa;


import io.requery.Persistable;
import io.requery.query.MutableResult;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.io.Serializable;
import java.util.Optional;

@Entity
@Table(name = "Groups")
public interface Group extends Serializable, Persistable {

    @Id
    @GeneratedValue
    int getId();

    String getName();

    Optional<String> getDescription();

    GroupType getType();

    byte[] getPicture();

    @Version
    int getVersion();

    @JoinTable(joinColumns = {@JoinColumn(name = "personId", referencedColumnName = "id")},
        inverseJoinColumns = {@JoinColumn(name = "groupId", referencedColumnName = "id")})
    @ManyToMany
    MutableResult<Person> getPersons();

    @Transient
    String getTemporaryName();
}
