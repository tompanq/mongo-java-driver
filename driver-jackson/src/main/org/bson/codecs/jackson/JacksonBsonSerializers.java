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

/**
 * Created by guo on 7/28/14.
 */

import com.fasterxml.jackson.databind.module.SimpleSerializers;
import org.bson.BsonJavaScript;
import org.bson.BsonSymbol;
import org.bson.BsonTimestamp;
import org.bson.codecs.jackson.serializers.JacksonDateSerializer;
import org.bson.codecs.jackson.serializers.JacksonJavascriptSerializer;
import org.bson.codecs.jackson.serializers.JacksonObjectIdSerializer;
import org.bson.codecs.jackson.serializers.JacksonRegexSerializer;
import org.bson.codecs.jackson.serializers.JacksonSymbolSerializer;
import org.bson.codecs.jackson.serializers.JacksonTimestampSerializer;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.regex.Pattern;


class JacksonBsonSerializers extends SimpleSerializers {
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor
     */
    JacksonBsonSerializers() {
        addSerializer(Date.class, new JacksonDateSerializer());
        addSerializer(Pattern.class, new JacksonRegexSerializer());
        addSerializer(ObjectId.class, new JacksonObjectIdSerializer());
        addSerializer(BsonSymbol.class, new JacksonSymbolSerializer());
        addSerializer(BsonTimestamp.class, new JacksonTimestampSerializer());
        addSerializer(BsonJavaScript.class, new JacksonJavascriptSerializer());
    }
}
