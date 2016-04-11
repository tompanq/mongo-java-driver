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
import java.util.Map;
import java.util.Set;

/**
 * A test entity containing various forms of nested container types.
 */
public class ContainerTypes extends NestedGenerics {
    private List<? extends BaseGenericType> list;
    private List<List<? extends BaseGenericType<?>>> doubleList;
    private List<List<List<? extends BaseGenericType<?>>>> tripleList;

    private Map<String, Integer> map;
    private Map<String, Map<String, Integer>> doubleMap;
    private Map<String, Map<String, Map<String, Integer>>> tripleMap;

    private Set<? extends BaseGenericType> set;
    private Set<Set<? extends BaseGenericType<?>>> doubleSet;
    private Set<Set<Set<? extends BaseGenericType<?>>>> tripleSet;

    private Map<String, List<Set<? extends BaseGenericType<?>>>> mixed;

    /**
     * @return the double list
     */
    public List<List<? extends BaseGenericType<?>>> getDoubleList() {
        return doubleList;
    }

    /**
     * @param doubleList the double list
     */
    public void setDoubleList(final List<List<? extends BaseGenericType<?>>> doubleList) {
        this.doubleList = doubleList;
    }

    /**
     * @return the double map
     */
    public Map<String, Map<String, Integer>> getDoubleMap() {
        return doubleMap;
    }

    /**
     * @param doubleMap the double map
     */
    public void setDoubleMap(final Map<String, Map<String, Integer>> doubleMap) {
        this.doubleMap = doubleMap;
    }

    /**
     * @return the double set
     */
    public Set<Set<? extends BaseGenericType<?>>> getDoubleSet() {
        return doubleSet;
    }

    /**
     * @param doubleSet the double set
     */
    public void setDoubleSet(final Set<Set<? extends BaseGenericType<?>>> doubleSet) {
        this.doubleSet = doubleSet;
    }

    /**
     * return the list
     */
    public List<? extends BaseGenericType> getList() {
        return list;
    }

    /**
     * @param list the list
     */
    public void setList(final List<? extends BaseGenericType> list) {
        this.list = list;
    }

    /**
     * return the map
     */
    public Map<String, Integer> getMap() {
        return map;
    }

    /**
     * @param map the map
     */
    public void setMap(final Map<String, Integer> map) {
        this.map = map;
    }

    /**
     * @return the mixed container
     */
    public Map<String, List<Set<? extends BaseGenericType<?>>>> getMixed() {
        return mixed;
    }

    /**
     * @param mixed the mixed container
     */
    public void setMixed(
        final Map<String, List<Set<? extends BaseGenericType<?>>>> mixed) {
        this.mixed = mixed;
    }

    /**
     * return the set
     */
    public Set<? extends BaseGenericType> getSet() {
        return set;
    }

    /**
     * @param set the set
     */
    public void setSet(final Set<? extends BaseGenericType> set) {
        this.set = set;
    }

    /**
     * @return the triple list
     */
    public List<List<List<? extends BaseGenericType<?>>>> getTripleList() {
        return tripleList;
    }

    /**
     * @param tripleList the triple list
     */
    public void setTripleList(final List<List<List<? extends BaseGenericType<?>>>> tripleList) {
        this.tripleList = tripleList;
    }

    /**
     * @return the triple map
     */
    public Map<String, Map<String, Map<String, Integer>>> getTripleMap() {
        return tripleMap;
    }

    /**
     * @param tripleMap the triple map
     */
    public void setTripleMap(final Map<String, Map<String, Map<String, Integer>>> tripleMap) {
        this.tripleMap = tripleMap;
    }

    /**
     * @return the triple set
     */
    public Set<Set<Set<? extends BaseGenericType<?>>>> getTripleSet() {
        return tripleSet;
    }

    /**
     * @param tripleSet the triple set
     */
    public void setTripleSet(final Set<Set<Set<? extends BaseGenericType<?>>>> tripleSet) {
        this.tripleSet = tripleSet;
    }

    @Override
    public int hashCode() {
        int result = list != null ? list.hashCode() : 0;
        result = 31 * result + (doubleList != null ? doubleList.hashCode() : 0);
        result = 31 * result + (tripleList != null ? tripleList.hashCode() : 0);
        result = 31 * result + (map != null ? map.hashCode() : 0);
        result = 31 * result + (doubleMap != null ? doubleMap.hashCode() : 0);
        result = 31 * result + (tripleMap != null ? tripleMap.hashCode() : 0);
        result = 31 * result + (set != null ? set.hashCode() : 0);
        result = 31 * result + (doubleSet != null ? doubleSet.hashCode() : 0);
        result = 31 * result + (tripleSet != null ? tripleSet.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContainerTypes)) {
            return false;
        }

        final ContainerTypes that = (ContainerTypes) o;

        if (list != null ? !list.equals(that.list) : that.list != null) {
            return false;
        }
        if (doubleList != null ? !doubleList.equals(that.doubleList) : that.doubleList != null) {
            return false;
        }
        if (tripleList != null ? !tripleList.equals(that.tripleList) : that.tripleList != null) {
            return false;
        }
        if (map != null ? !map.equals(that.map) : that.map != null) {
            return false;
        }
        if (doubleMap != null ? !doubleMap.equals(that.doubleMap) : that.doubleMap != null) {
            return false;
        }
        if (tripleMap != null ? !tripleMap.equals(that.tripleMap) : that.tripleMap != null) {
            return false;
        }
        if (set != null ? !set.equals(that.set) : that.set != null) {
            return false;
        }
        if (doubleSet != null ? !doubleSet.equals(that.doubleSet) : that.doubleSet != null) {
            return false;
        }
        return tripleSet != null ? tripleSet.equals(that.tripleSet) : that.tripleSet == null;

    }
}
