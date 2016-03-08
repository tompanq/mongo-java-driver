/*
 * Copyright (c) 2008-2015 MongoDB, Inc.
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.RawBsonDocument;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Provides the encoding and decoding logic for a ClassModel
 *
 * @param <T> the type to encode/decode
 */
@SuppressWarnings("unchecked")
public class ClassModelCodec<T extends Object> implements Codec<T> {
    private final ClassModel classModel;


    /**
     * Creates a Codec for the ClassModel
     *
     * @param mapper
     * @param model the model to use
     */
    public ClassModelCodec(final CodecRegistry codecRegistry, final ObjectMapper mapper, final ClassModel model) {
        this.classModel = model;
    }

    public ClassModelCodec(final ClassModelCodec<T> codec, final List<Class<?>> parameterTypes) {
        this.classModel = new ClassModel(codec.getClassModel(), parameterTypes);
    }

    @Override
    public T decode(final BsonReader reader, final DecoderContext decoderContext) {
        try {
            final Constructor<Object> ctor = classModel.getType().getConstructor();
            ctor.setAccessible(true);
            final T entity = (T) ctor.newInstance();
            reader.readStartDocument();
            while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                final String name = reader.readName();
                final FieldModel fieldModel = classModel.getField(name);
                fieldModel.decode(entity, reader, decoderContext);

            }
            reader.readEndDocument();
            return entity;
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void encode(final BsonWriter writer, final Object entity, final EncoderContext encoderContext) {
        writer.writeStartDocument();
        for (final FieldModel fieldModel : classModel.getFields()) {
            fieldModel.encode(entity, writer, encoderContext);
        }
        writer.writeEndDocument();
    }

    public ClassModel getClassModel() {
        return classModel;
    }

    @Override
    public Class<T> getEncoderClass() {
        return (Class<T>) classModel.getType();
    }

    @Override
    public String toString() {
        return String.format("ClassModelCodec<%s>", classModel.getName());
    }
}
