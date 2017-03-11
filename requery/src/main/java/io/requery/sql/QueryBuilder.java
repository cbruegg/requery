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

package io.requery.sql;

import io.requery.meta.Attribute;
import io.requery.meta.Type;
import io.requery.query.Expression;
import io.requery.query.ExpressionType;
import io.requery.util.function.Function;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * String Builder for creating SQL statements.
 *
 * @author Nikhil Purushe
 */
public class QueryBuilder implements CharSequence {

    public interface Appender<T> {
        void append(QueryBuilder qb, T value);
    }

    public static class Options {
        private final String quotedIdentifier;
        private final Function<String, String> tableTransformer;
        private final Function<String, String> columnTransformer;
        private final boolean lowercaseKeywords;
        private final boolean quoteTableNames;
        private final boolean quoteColumnNames;

        public Options(String quotedIdentifier,
                       boolean lowercaseKeywords,
                       Function<String, String> tableTransformer,
                       Function<String, String> columnTransformer,
                       boolean quoteTableNames,
                       boolean quoteColumnNames) {

            if (quotedIdentifier.equals(" ")) {
                quotedIdentifier = "\"";
            }
            this.quotedIdentifier = quotedIdentifier;
            this.tableTransformer = tableTransformer;
            this.columnTransformer = columnTransformer;
            this.lowercaseKeywords = lowercaseKeywords;
            this.quoteTableNames = quoteTableNames;
            this.quoteColumnNames = quoteColumnNames;
        }
    }

    private final StringBuilder sb;
    private final Options options;

    public QueryBuilder(Options options) {
        this.options = options;
        this.sb = new StringBuilder(32);
    }

    @Override
    @Nonnull
    public String toString() {
        return sb.toString();
    }

    @Override
    public int length() {
        return sb.length();
    }

    @Override
    public char charAt(int index) {
        return sb.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return sb.subSequence(start, end);
    }

    public QueryBuilder keyword(Keyword... keywords) {
        for (Keyword keyword : keywords) {
            sb.append(options.lowercaseKeywords ?
                keyword.toString().toLowerCase(Locale.ROOT) : keyword);
            sb.append(" ");
        }
        return this;
    }

    public QueryBuilder appendIdentifier(String value, String identifier) {
        return append(identifier, false).append(value, false).append(identifier);
    }

    public QueryBuilder appendQuoted(String value) {
        return appendIdentifier(value, "\'");
    }

    public QueryBuilder tableName(Object value) {
        String name = value.toString();
        if (options.tableTransformer != null) {
            name = options.tableTransformer.apply(name);
        }
        if (options.quoteTableNames) {
            appendIdentifier(name, options.quotedIdentifier);
        } else {
            append(name);
        }
        return space();
    }

    public QueryBuilder tableNames(Iterable<Expression<?>> values) {
        Set<Type<?>> types = new LinkedHashSet<>();
        for (Expression<?> expression : values) {
            if (expression.getExpressionType() == ExpressionType.ATTRIBUTE) {
                Attribute attribute = (Attribute) expression;
                types.add(attribute.getDeclaringType());
            }
        }
        return commaSeparated(types, new Appender<Type<?>>() {
            @Override
            public void append(QueryBuilder qb, Type<?> value) {
                tableName(value.getName());
            }
        });
    }

    public QueryBuilder attribute(Attribute value) {
        String name = options.columnTransformer == null ?
                value.getName() : options.columnTransformer.apply(value.getName());
        if(options.quoteColumnNames) {
            appendIdentifier(name, options.quotedIdentifier);
        } else {
            append(name);
        }
        return space();
    }

    public QueryBuilder aliasAttribute(String alias, Attribute value) {
        append(alias);
        append(".");
        return attribute(value);
    }

    public QueryBuilder append(Object value) {
        return append(value, false);
    }

    public QueryBuilder value(Object value) {
        return append(value, true);
    }

    public QueryBuilder append(Object value, boolean space) {
        if (value == null) {
            keyword(Keyword.NULL);
        } else if (value instanceof String[]) {
            commaSeparated(Arrays.asList((String[]) value));
        } else {
            if (value instanceof Keyword) {
                sb.append(options.lowercaseKeywords ?
                        value.toString().toLowerCase(Locale.ROOT) : value.toString());
            } else {
                sb.append(value.toString());
            }
        }
        if (space) {
            sb.append(" ");
        }
        return this;
    }

    public <T> QueryBuilder appendWhereConditions(Set<Attribute<T, ?>> attributes) {
        int index = 0;
        for (Attribute attribute : attributes) {
            if (index > 0) {
                keyword(Keyword.AND);
                space();
            }
            attribute(attribute);
            space();
            append("=?");
            space();
            index++;
        }
        return this;
    }

    public QueryBuilder commaSeparatedExpressions(Iterable<Expression<?>> values) {
        return commaSeparated(values, new QueryBuilder.Appender<Expression<?>>() {
            @Override
            public void append(QueryBuilder qb, Expression<?> value) {
                switch (value.getExpressionType()) {
                    case ATTRIBUTE:
                        qb.attribute((Attribute) value);
                        break;
                    default:
                        qb.append(value.getName()).space();
                        break;
                }
            }
        });
    }

    public QueryBuilder commaSeparatedAttributes(Iterable<? extends Attribute<?, ?>> values) {
        return commaSeparated(values, new QueryBuilder.Appender<Attribute<?, ?>>() {
            @Override
            public void append(QueryBuilder qb, Attribute<?, ?> value) {
                qb.attribute(value);
            }
        });
    }

    public <T> QueryBuilder commaSeparated(Iterable<? extends T> values) {
        return commaSeparated(values, null);
    }

    public <T> QueryBuilder commaSeparated(Iterable<? extends T> values, Appender<T> appender) {
        return commaSeparated(values.iterator(), appender);
    }

    public <T> QueryBuilder commaSeparated(Iterator<? extends T> values, Appender<T> appender) {
        int i = 0;
        while (values.hasNext()) {
            T value = values.next();
            if (i > 0) {
                comma();
            }
            if (appender == null) {
                append(value);
            } else {
                appender.append(this, value);
            }
            i++;
        }
        return this;
    }

    public QueryBuilder openParenthesis() {
        sb.append("(");
        return this;
    }

    public QueryBuilder closeParenthesis() {
        if (sb.charAt(sb.length() - 1) == ' ') {
            sb.setCharAt(sb.length() - 1, ')');
        } else {
            sb.append(')');
        }
        return this;
    }

    public QueryBuilder space() {
        if (sb.charAt(sb.length() - 1) != ' ') {
            sb.append(" ");
        }
        return this;
    }

    public QueryBuilder comma() {
        if (sb.charAt(sb.length() - 1) == ' ') {
            sb.setCharAt(sb.length() - 1, ',');
        } else {
            sb.append(',');
        }
        space();
        return this;
    }
}
