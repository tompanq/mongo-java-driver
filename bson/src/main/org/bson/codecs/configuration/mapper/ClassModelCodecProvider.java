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

import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.undercouch.bson4jackson.BsonFactory;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.configuration.mapper.conventions.ConventionPack;
import org.bson.codecs.configuration.mapper.conventions.DefaultConventionPack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Provides Codecs for various POJOs via the ClassModel abstractions.
 */
public class ClassModelCodecProvider implements CodecProvider {
    private final TypeResolver resolver = new TypeResolver();
    private final Set<Class<?>> registered;
    private final Map<Class<?>, ClassModelCodec<?>> mapped = new HashMap<Class<?>, ClassModelCodec<?>>();
    private final ConventionPack conventionPack;
    private ObjectMapper mapper;

    /**
     * Creates a provider for a given set of classes.
     *
     * @param conventionPack The conventions to use when mapping
     * @param registered     the classes to use
     */
    public ClassModelCodecProvider(final ConventionPack conventionPack, final Set<Class<?>> registered) {
        this.conventionPack = conventionPack;
        this.registered = registered;
        mapper = new ObjectMapper(new BsonFactory());
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.setAnnotationIntrospector(new MongoAnnotationIntrospector(conventionPack));
    }

    /**
     * Creates a Builder so classes can be registered before creating an immutable Provider.
     *
     * @return the Builder
     * @see ProviderBuilder#register(Class)
     */
    public static ProviderBuilder builder() {
        return new ProviderBuilder();
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry) {
        ClassModelCodec<?> codec = null;
        if (registered.contains(clazz)) {
            codec = mapped.get(clazz);
            if (codec == null && conventionPack.isMappable(clazz)) {
/*
                final BsonSchemaVisitor visitor = new BsonSchemaVisitor(mapper);
                try {
                    mapper.acceptJsonFormatVisitor(clazz, visitor);
                } catch (JsonMappingException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
*/

                final ClassModel model = new ClassModel(registry, resolver, clazz);
                conventionPack.apply(model);
                codec = new ClassModelCodec(registry, mapper, model);
                mapped.put(clazz, codec);
            }
        }
        return (Codec<T>) codec;
    }

    /**
     * A Builder for the ClassModelCodecProvider
     */
    public static class ProviderBuilder {
        private final Set<Class<?>> registered = new HashSet<Class<?>>();
        private ConventionPack conventionPack = new DefaultConventionPack();

        /**
         * Creates the ClassModelCodecProvider with the classes that have been registered.
         *
         * @return the Provider
         * @see #register(Class)
         */
        public ClassModelCodecProvider build() {
            return new ClassModelCodecProvider(conventionPack, registered);
        }

        /**
         * Registers a class with the builder for inclusion in the Provider.
         *
         * @param clazz the class to register
         * @return this
         */
        public ProviderBuilder register(final Class<?> clazz) {
            registered.add(clazz);
            return this;
        }

        /**
         * Sets the conventions to use when mapping types.
         *
         * @param conventionPack the ConventionPack to use with the ClassModelCodecProvider
         * @return this
         */
        public ProviderBuilder setConventionPack(final ConventionPack conventionPack) {
            this.conventionPack = conventionPack;
            return this;
        }
    }
/*

    @SuppressWarnings("CheckStyle")
    class BsonSchemaVisitor extends Base {
        private final ObjectMapper mapper;
        private final List<MessageElementVisitor> elements = new ArrayList<MessageElementVisitor>();

        public BsonSchemaVisitor(final ObjectMapper mapper) {
            this.mapper = mapper;
        }

*/
/*
    public void accept(final Class<?> typeClass) throws JsonMappingException {
        mapper.acceptJsonFormatVisitor(typeClass, this);
    }
*//*


        @Override
        public JsonObjectFormatVisitor expectObjectFormat(final JavaType type) throws JsonMappingException {
            final MessageElementVisitor visitor = new MessageElementVisitor(type);
            add(visitor);
            return visitor;
        }

        private void add(final MessageElementVisitor visitor) {
            elements.add(visitor);
        }

    }


    class MessageElementVisitor extends JsonObjectFormatVisitor.Base {
        private final JavaType type;
        private final ClassModel classModel;
        //    private final Builder builder;

        MessageElementVisitor(final JavaType type) {
            this.type = type;
            classModel = new ClassModel(type);
            //        builder = new Builder();
            //        builder.name(type.getRawClass().getSimpleName());
            //        builder.documentation("Message for " + type.toCanonical());
        }

        @Override
        public void property(final BeanProperty prop) throws JsonMappingException {
            throw new UnsupportedOperationException("Not implemented yet!");
        }

        @Override
        public void property(final String name, final JsonFormatVisitable handler, final JavaType propertyTypeHint)
            throws JsonMappingException {
            throw new UnsupportedOperationException("Not implemented yet!");
        }

        @Override
        public void optionalProperty(final BeanProperty prop) throws JsonMappingException {
            classModel.addField(new FieldModel(classModel, prop));
        }

        @Override
        public void optionalProperty(final String name, final JsonFormatVisitable handler, final JavaType propertyTypeHint)
            throws JsonMappingException {
            throw new UnsupportedOperationException("Not implemented yet!");
        }
    }
*/

}
