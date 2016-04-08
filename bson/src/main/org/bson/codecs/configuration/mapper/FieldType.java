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
import java.util.List;

@SuppressWarnings("CheckStyle")
public abstract class FieldType {
    private final FieldModel fieldModel;

    public FieldType(final FieldModel fieldModel) {
        this.fieldModel = fieldModel;
    }

    public static FieldType wrap(final FieldModel fieldModel, final ResolvedType type) {
        FieldType fieldType = null;
        if (FieldType.isCollection(type.getErasedType())) {
            final ResolvedType listType = type.getTypeParameters().get(0);
            fieldType = new IterableType(FieldType.wrap(fieldModel, listType), fieldModel);
        } else {
            fieldType = new ConcreteFieldType(fieldModel, type);
        }

        return fieldType;
    }

    public static boolean isCollection(final Class<?> type) {
        return Collection.class.isAssignableFrom(type);
    }

    public abstract Object decode(final BsonReader reader, final DecoderContext context);

    public abstract void encode(final Object o, final BsonWriter writer, final EncoderContext encoderContext);

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
        super(fieldModel);
        this.type = type;
    }

    public <T> T createContainerType() {
        try {
            return (T) ArrayList.class.newInstance();
        } catch (final InstantiationException e) {
            throw new ClassMappingException(e);
        } catch (final IllegalAccessException e) {
            throw new ClassMappingException(e);
        }
    }

    @Override
    public Object decode(final BsonReader reader, final DecoderContext context) {
        reader.readStartArray();

        final Collection<Object> list = createContainerType();
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
