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

package org.bson.codecs.configuration.mapper;

import com.fasterxml.classmate.ResolvedType;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("CheckStyle")
public abstract class FieldType {
    private final FieldModel fieldModel;
    private Class<?> concreteClass;

    public FieldType(final FieldModel fieldModel) {
        this.fieldModel = fieldModel;
    }

    protected FieldType(final FieldModel fieldModel, final Class<?> concreteClass) {
        this.fieldModel = fieldModel;
        this.concreteClass = concreteClass;
    }

    public static FieldType wrap(final FieldModel fieldModel, final ResolvedType type) {
        FieldType fieldType = null;
        if (FieldType.isCollection(type.getErasedType())) {
            final ResolvedType listType = type.getTypeParameters().get(0);
            fieldType = new IterableType(FieldType.wrap(fieldModel, listType), fieldModel);
        } else if (FieldType.isMap(type.getErasedType())) {
            final List<ResolvedType> types = type.getTypeParameters();
            final ResolvedType keyType = types.get(0);
            final ResolvedType valueType = types.get(1);
            if (!(keyType.getErasedType().isAssignableFrom(String.class))) {
                throw new ClassMappingException(String.format("Map key types must be Strings.  Found %s instead.",
                                                              keyType.getErasedType()));
            }
            fieldType = new MapType(FieldType.wrap(fieldModel, valueType), fieldModel);
        } else {
            fieldType = new ConcreteFieldType(fieldModel, type);
        }

        return fieldType;
    }

    public static boolean isCollection(final Class<?> type) {
        return Collection.class.isAssignableFrom(type);
    }

    public static boolean isMap(final Class<?> type) {
        return Map.class.isAssignableFrom(type);
    }

    public <T> T createConcreteType() {
        try {
            return (T) getConcreteClass().newInstance();
        } catch (final Exception e) {
            throw new ClassMappingException(e);
        }
    }

    public abstract Object decode(final BsonReader reader, final DecoderContext context);

    public abstract void encode(final Object o, final BsonWriter writer, final EncoderContext encoderContext);

    public Class<?> getConcreteClass() {
        return concreteClass;
    }

    public void setConcreteClass(final Class<?> concreteClass) {
        this.concreteClass = concreteClass;
    }

    public FieldModel getFieldModel() {
        return fieldModel;
    }

}

class ConcreteFieldType extends FieldType {
    private final ResolvedType type;

    ConcreteFieldType(final FieldModel fieldModel, final ResolvedType type) {
        super(fieldModel);
        this.type = type;
    }

    @Override
    public Object decode(final BsonReader reader, final DecoderContext context) {
        return getFieldModel().getCodec(type.getErasedType()).decode(reader, context);

    }

    @Override
    public void encode(final Object o, final BsonWriter writer, final EncoderContext encoderContext) {
        final Codec<Object> codec = (Codec<Object>) getFieldModel().getCodec(o.getClass());
        codec.encode(writer, getFieldModel().getConverter().apply(o), encoderContext);
    }

}

@SuppressWarnings("CheckStyle")
class IterableType extends FieldType {
    private final FieldType type;

    public IterableType(final FieldType type, final FieldModel fieldModel) {
        super(fieldModel, ArrayList.class);
        this.type = type;
    }

    @Override
    public Object decode(final BsonReader reader, final DecoderContext context) {
        reader.readStartArray();

        final Collection<Object> list = createConcreteType();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            list.add(type.decode(reader, context));
        }
        reader.readEndArray();
        return list;
    }

    @Override
    public void encode(final Object o, final BsonWriter writer, final EncoderContext encoderContext) {
        writer.writeStartArray();
        for (final Object object : (List<?>) o) {
            type.encode(object, writer, encoderContext);
        }
        writer.writeEndArray();
    }
}

class MapType extends FieldType {
    private final FieldType type;

    MapType(final FieldType type, final FieldModel fieldModel) {
        super(fieldModel, HashMap.class);
        this.type = type;
    }

    @Override
    public Object decode(final BsonReader reader, final DecoderContext context) {
        final Map<Object, Object> map = createConcreteType();
        reader.readStartDocument();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            map.put(reader.readName(), type.decode(reader, context));
        }
        reader.readEndDocument();
        return map;
    }

    @Override
    public void encode(final Object o, final BsonWriter writer, final EncoderContext encoderContext) {
        writer.writeStartDocument();
        for (final Entry<?, ?> entry : ((Map<?, ?>) o).entrySet()) {
            writer.writeName(entry.getKey().toString());
            type.encode(entry.getValue(), writer, encoderContext);
        }
        writer.writeEndDocument();
    }
}
