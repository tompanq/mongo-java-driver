/*
 * Copyright (c) 2008-2014 MongoDB, Inc.
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

package org.bson.codecs.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.bson.BsonReader;
import org.bson.BsonSerializationException;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.io.IOException;

/**
 * A codec for  instances.
 *
 * @param <T> The parameterized type
 * @since 3.0
 */
public class JacksonCodec<T> implements Codec<T> {

    private final Class<T> clazz;

    private final ObjectMapper objectMapper;

    /**
     * @param clazz the class to serialize
     */
    public JacksonCodec(final Class<T> clazz) {
        this(new ObjectMapper(), clazz);
    }

    /**
     * @param objMapper The ObjectMapper to use
     * @param clazz     the class to serialize
     */
    public JacksonCodec(final ObjectMapper objMapper, final Class<T> clazz) {
        objectMapper = objMapper;
        objectMapper.registerModule(new JacksonBsonModule());
        this.clazz = clazz;
    }


    @Override
    public T decode(final BsonReader reader, final DecoderContext decoderContext) {

        try {
            return objectMapper.readValue(new JacksonBsonParser(reader), clazz);

        } catch (final IOException e) {
            throw new BsonSerializationException("error reading value", e);
        }

    }

    @Override
    public void encode(final BsonWriter writer, final T value, final EncoderContext encoderContext) {
        final JacksonBsonGenerator generator = new JacksonBsonGenerator(writer);

        try {
            final ObjectWriter w = objectMapper.writerFor(clazz);
            w.writeValue(generator, value);
            generator.close();

        } catch (final IOException e) {
            throw new BsonSerializationException("error writing value " + value, e);
        }
    }

    @Override
    public Class<T> getEncoderClass() {
        return clazz;
    }

}
