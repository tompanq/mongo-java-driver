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

package org.bson.codecs.jackson.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.bson.codecs.jackson.JacksonBsonParser;

import java.io.IOException;

/**
 * Provides a deserializer for BSON values
 *
 * @param <T> the BSON type
 */
public abstract class JacksonBsonDeserializer<T> extends JsonDeserializer<T> {
    @Override
    public T deserialize(final JsonParser jsonParser, final DeserializationContext ctxt)
        throws IOException {
        if (!(jsonParser instanceof JacksonBsonParser)) {
            throw new UnsupportedOperationException(getClass().getSimpleName() + " can only be used with JacksonBsonParser");
        }
        return deserialize((JacksonBsonParser) jsonParser, ctxt);
    }

    /**
     * @param bp   JacksonBsonParser used for reading BSON content
     * @param ctxt Context that can be used to access information about this deserialization activity.
     * @return the deserialized value
     * @throws IOException if an error occurs during deserialization
     * @see #deserialize(JsonParser, DeserializationContext)
     */
    public abstract T deserialize(JacksonBsonParser bp, DeserializationContext ctxt) throws IOException;
}
