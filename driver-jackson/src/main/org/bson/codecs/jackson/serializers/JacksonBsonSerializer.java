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

package org.bson.codecs.jackson.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bson.codecs.jackson.JacksonBsonGenerator;

import java.io.IOException;

/**
 * A BSON serializer for Jackson
 */
abstract class JacksonBsonSerializer<T> extends JsonSerializer<T> {
    @Override
    public void serialize(final T t, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException {
        if (t == null) {
            serializerProvider.defaultSerializeNull(jsonGenerator);
        } else {
            serialize(t, (JacksonBsonGenerator) jsonGenerator, serializerProvider);
        }
    }

    public abstract void serialize(T t, JacksonBsonGenerator generator, SerializerProvider provider)
        throws IOException;
}
