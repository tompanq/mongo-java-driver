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

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Provides the encoding and decoding logic for a ClassModel
 *
 * @param <T> the type to encode/decode
 */
@SuppressWarnings({"unchecked"})
public class ClassModelCodec<T extends Object> implements Codec<T> {
    private final ClassModel classModel;
    private final ClassModelCodecProvider provider;
    private final CodecRegistry registry;
    private final Constructor<?> constructor;

    /**
     * Creates a Codec for the ClassModel
     *
     * @param provider the provider for this codec
     * @param registry the codec registry for this codec
     * @param model    the model to use
     */
    public ClassModelCodec(final ClassModel model, final ClassModelCodecProvider provider, final CodecRegistry registry) {
        this.classModel = model;
        this.provider = provider;
        this.registry = registry;
        try {
            constructor = classModel.getType().getConstructor();
            constructor.setAccessible(true);
        } catch (final NoSuchMethodException e) {
            throw new ClassMappingException("No zero arugment constructor was found for the type " + classModel.getType().getName());
        }
    }

    ClassModelCodec(final ClassModelCodec<T> codec, final List<Class<?>> parameterTypes) {
        this.classModel = new ClassModel(codec.getClassModel(), parameterTypes);
        this.provider = codec.provider;
        this.registry = codec.registry;
        try {
            constructor = classModel.getType().getConstructor();
            constructor.setAccessible(true);
        } catch (final NoSuchMethodException e) {
            throw new ClassMappingException("No zero arugment constructor was found for the type " + classModel.getType().getName());
        }
    }

    private T createInstance() throws InstantiationException, InvocationTargetException, IllegalAccessException {
        return (T) constructor.newInstance();
    }

    @Override
    public T decode(final BsonReader reader, final DecoderContext decoderContext) {
        try {
            reader.mark();
            final ClassModelCodec<T> codec = findActualCodec(reader);
            final T entity = codec.createInstance();
            reader.reset();
            reader.readStartDocument();
            codec.readFields(entity, reader, decoderContext);
            reader.readEndDocument();
            return entity;
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void encode(final BsonWriter writer, final Object entity, final EncoderContext encoderContext) {
        final ClassModelCodec<T> codec = (ClassModelCodec<T>) findActualCodec(entity.getClass());
        codec.writeFields(writer, entity, encoderContext);
    }

    @Override
    public Class<T> getEncoderClass() {
        return (Class<T>) classModel.getType();
    }

    /**
     * @return the ClassModel for this codec
     */
    public ClassModel getClassModel() {
        return classModel;
    }

    void readFields(final T entity, final BsonReader reader, final DecoderContext decoderContext) {
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            final String name = reader.readName();
            if (name.equals("_t")) {
                reader.readString();
            } else if (!name.equals("_t")) {
                final FieldModel fieldModel = classModel.getField(name);
                fieldModel.decode(entity, reader, decoderContext);
            } else {
                reader.readString();
            }
        }
    }

    @Override
    public String toString() {
        return String.format("ClassModelCodec<%s>", classModel.getName());
    }

    void writeFields(final BsonWriter writer, final Object entity, final EncoderContext encoderContext) {
        if (entity != null) {
            writer.writeStartDocument();
            writer.writeString("_t", entity.getClass().getName());
            for (final FieldModel fieldModel : classModel.getFields()) {
                fieldModel.encode(entity, writer, encoderContext);
            }
            writer.writeEndDocument();
        }
    }

    private <K> ClassModelCodec<K> findActualCodec(final BsonReader reader) {
        reader.readStartDocument();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            final String name = reader.readName();
            if (name.equals("_t")) {
                final Class<K> aClass;
                final String className = reader.readString();
                try {
                    aClass = (Class<K>) Class.forName(className);
                } catch (final ClassNotFoundException e) {
                    throw new ClassMappingException("A mapped class type could not be found: " + className);
                }
                return findActualCodec(aClass);
            } else {
                reader.skipValue();
            }
        }
        return (ClassModelCodec<K>) this;
    }

    private <K> ClassModelCodec<K> findActualCodec(final Class<K> aClass) {
        return (ClassModelCodec<K>) provider.get(aClass, registry);
    }
}
