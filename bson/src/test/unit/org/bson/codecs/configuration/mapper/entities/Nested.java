/*
 * Copyright (c) 2008-2016 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bson.codecs.configuration.mapper.entities;

import java.util.ArrayList;
import java.util.List;

public class Nested extends NestedGenerics {
    private List<List<? extends BaseGenericType<?>>> list = new ArrayList<List<? extends BaseGenericType<?>>>();

    public Nested() {
    }

    public Nested(final List<List<? extends BaseGenericType<?>>> list) {
        this.list = list;
    }

    public List<List<? extends BaseGenericType<?>>> getList() {
        return list;
    }

    public void setList(final List<List<? extends BaseGenericType<?>>> list) {
        this.list = list;
    }

    @Override
    public int hashCode() {
        return list != null ? list.hashCode() : 0;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Nested)) {
            return false;
        }

        return compare(list, ((Nested) o).list);
    }
}
