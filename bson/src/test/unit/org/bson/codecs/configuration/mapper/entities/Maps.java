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

import java.util.HashMap;
import java.util.Map;

public class Maps extends NestedGenerics {
    private Map<String, Integer> map = new HashMap<String, Integer>();

    public Map<String, Integer> getMap() {
        return map;
    }

    public void setMap(final Map<String, Integer> map) {
        this.map = map;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Maps)) {
            return false;
        }

        final Maps maps = (Maps) o;

        return map != null ? map.equals(maps.map) : maps.map == null;

    }

    @Override
    public int hashCode() {
        return map != null ? map.hashCode() : 0;
    }
}
