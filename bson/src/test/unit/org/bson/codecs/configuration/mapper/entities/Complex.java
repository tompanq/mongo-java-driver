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

@SuppressWarnings("CheckStyle")
public class Complex {
    private IntChild intChild = new IntChild(100);
    private StringChild stringChild = new StringChild("what what?");
    private BaseGenericType<String> baseType = new BaseGenericType<String>("so tricksy!");

    public Complex() {
    }

    public Complex(final IntChild intChild, final StringChild stringChild,
                   final BaseGenericType<String> baseType) {
        this.intChild = intChild;
        this.stringChild = stringChild;
        this.baseType = baseType;
    }

    public BaseGenericType<String> getBaseType() {
        return baseType;
    }

    public IntChild getIntChild() {
        return intChild;
    }

    public void setBaseType(final BaseGenericType<String> baseType) {
        this.baseType = baseType;
    }

    public void setIntChild(final IntChild intChild) {
        this.intChild = intChild;
    }

    public StringChild getStringChild() {
        return stringChild;
    }

    public Complex setStringChild(final StringChild stringChild) {
        this.stringChild = stringChild;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Complex)) {
            return false;
        }

        final Complex complex = (Complex) o;

        if (intChild != null ? !intChild.equals(complex.intChild) : complex.intChild != null) {
            return false;
        }
        if (stringChild != null ? !stringChild.equals(complex.stringChild) : complex.stringChild != null) {
            return false;
        }
        return baseType != null ? baseType.equals(complex.baseType) : complex.baseType == null;

    }

    @Override
    public int hashCode() {
        int result = intChild != null ? intChild.hashCode() : 0;
        result = 31 * result + (stringChild != null ? stringChild.hashCode() : 0);
        result = 31 * result + (baseType != null ? baseType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("Complex{baseType=%s, intChild=%s, stringChild=%s}", baseType, intChild, stringChild);
    }
}
