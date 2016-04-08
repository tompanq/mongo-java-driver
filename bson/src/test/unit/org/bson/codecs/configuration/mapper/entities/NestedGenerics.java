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

import java.util.List;

public class NestedGenerics {
    protected boolean compare(final List<?> l1, final List<?> l2) {
        for (int i = 0; i < l1.size(); i++) {
            return compare(l1.get(i), l2.get(i));
        }
        return true;
    }

    protected boolean compare(final Object o1, final Object o2) {
        if (o1 instanceof List) {
            return compare((List<?>) o1, (List<?>) o2);
        }
        return o1.equals(o2);
    }
}
