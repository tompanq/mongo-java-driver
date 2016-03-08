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

package org.bson.codecs.configuration.mapper.conventions;

import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitable;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor.Base;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.BsonDocumentWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.ValueCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.configuration.mapper.ClassModelCodec;
import org.bson.codecs.configuration.mapper.ClassModelCodecProvider;
import org.bson.codecs.configuration.mapper.conventions.entities.Address;
import org.bson.codecs.configuration.mapper.conventions.entities.Entity;
import org.bson.codecs.configuration.mapper.conventions.entities.Person;
import org.bson.codecs.configuration.mapper.conventions.entities.SecureEntity;
import org.bson.codecs.configuration.mapper.conventions.entities.ZipCode;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("CheckStyle")
public class ConventionPackTest {

    @Test
    public void testCustomConventions() {
        final ClassModelCodecProvider codecProvider = ClassModelCodecProvider
            .builder()
            .setConventionPack(new CustomConventionPack())
            .register(Entity.class)
            .build();
        final CodecRegistry registry = CodecRegistries.fromProviders(codecProvider, new ValueCodecProvider());

        final Codec<Entity> codec = registry.get(Entity.class);
        final BsonDocument document = new BsonDocument();
        final BsonDocumentWriter writer = new BsonDocumentWriter(document);
        codec.encode(writer, new Entity(102L, 0, "Scrooge", "Ebenezer Scrooge"), EncoderContext.builder().build());
        Assert.assertEquals(document.getNumber("age").longValue(), 102L);
        Assert.assertEquals(document.getNumber("faves").intValue(), 0);
        Assert.assertEquals(document.getString("name").getValue(), "Scrooge");
        Assert.assertEquals(document.getString("full_name").getValue(), "Ebenezer Scrooge");
        Assert.assertFalse(document.containsKey("debug"));
    }

    @Test
    public void testDefaultConventions() {
        final ClassModelCodecProvider codecProvider = ClassModelCodecProvider
            .builder()
            .register(Entity.class)
            .build();
        final CodecRegistry registry = CodecRegistries.fromProviders(codecProvider, new ValueCodecProvider());

        final Codec<Entity> codec = registry.get(Entity.class);
        final BsonDocument document = new BsonDocument();
        final BsonDocumentWriter writer = new BsonDocumentWriter(document);
        codec.encode(writer, new Entity(102L, 0, "Scrooge", "Ebenezer Scrooge"), EncoderContext.builder().build());
        Assert.assertEquals(document.getNumber("age").longValue(), 102L);
        Assert.assertEquals(document.getNumber("faves").intValue(), 0);
        Assert.assertEquals(document.getString("name").getValue(), "Scrooge");
        Assert.assertEquals(document.getString("fullName").getValue(), "Ebenezer Scrooge");
        Assert.assertFalse(document.containsKey("debug"));
    }

    @Test
    public void testEmbeddedEntities() {
        final ClassModelCodecProvider codecProvider = ClassModelCodecProvider
            .builder()
            .register(Person.class)
            .register(Address.class)
            .register(ZipCode.class)
            .build();
        final CodecRegistry registry = CodecRegistries.fromProviders(codecProvider, new ValueCodecProvider());

        final Codec<Person> personCodec = registry.get(Person.class);
        final Codec<Address> addressCodec = registry.get(Address.class);
        final Codec<ZipCode> zipCodeCodec = registry.get(ZipCode.class);
        final BsonDocument personDocument = new BsonDocument();
        final BsonDocument addressDocument = new BsonDocument();
        final BsonDocument zipDocument = new BsonDocument();

        final ZipCode zip = new ZipCode(12345, 1234);
        final Address address = new Address("1000 Quiet Lane", "Whispering Pines", "HA", zip);
        final Person entity = new Person("Bob", "Ross", address);

        zipCodeCodec.encode(new BsonDocumentWriter(zipDocument), zip, EncoderContext.builder().build());
        Assert.assertEquals(zipDocument.getInt32("number").getValue(), 12345);
        Assert.assertEquals(zipDocument.getInt32("extended").getValue(), 1234);

        addressCodec.encode(new BsonDocumentWriter(addressDocument), address, EncoderContext.builder().build());
        Assert.assertEquals(addressDocument.getString("street").getValue(), "1000 Quiet Lane");
        Assert.assertEquals(addressDocument.getString("city").getValue(), "Whispering Pines");
        Assert.assertEquals(addressDocument.getString("state").getValue(), "HA");
        Assert.assertEquals(addressDocument.getDocument("zip"), zipDocument);

        personCodec.encode(new BsonDocumentWriter(personDocument), entity, EncoderContext.builder().build());
        Assert.assertEquals(personDocument.getString("firstName").getValue(), "Bob");
        Assert.assertEquals(personDocument.getString("lastName").getValue(), "Ross");
        Assert.assertEquals(personDocument.getDocument("home"), addressDocument);

        Assert.assertEquals(entity, personCodec.decode(new BsonDocumentReader(personDocument), DecoderContext.builder().build()));
    }

    @Test
    public void testGenerics() throws JsonMappingException {
        final ClassModelCodecProvider codecProvider = ClassModelCodecProvider
            .builder()
            .register(BaseType.class)
            .register(IntChild.class)
            .register(StringChild.class)
            .register(Complex.class)
            .build();
        final CodecRegistry registry = CodecRegistries.fromProviders(codecProvider, new ValueCodecProvider());

        final TypeResolver resolver = new TypeResolver();

        final ClassModelCodec<IntChild> intClassModel = ((ClassModelCodec<IntChild>) registry.get(IntChild.class));
        final ClassModelCodec<StringChild> stringClassModel = ((ClassModelCodec<StringChild>) registry.get(StringChild.class));
        final ClassModelCodec<Complex> complexClassModel = ((ClassModelCodec<Complex>) registry.get(Complex.class));


        BsonDocument document = new BsonDocument();
        intClassModel.encode(new BsonDocumentWriter(document), new IntChild(42), EncoderContext.builder().build());
        Assert.assertEquals(42, document.getInt32("t").intValue());

        document = new BsonDocument();
        stringClassModel.encode(new BsonDocumentWriter(document), new StringChild("I'm a child!"), EncoderContext.builder().build());
        Assert.assertEquals("I'm a child!", document.getString("t").getValue());

        document = new BsonDocument();
        final Complex complex = new Complex();
        complexClassModel.encode(new BsonDocumentWriter(document), complex, EncoderContext.builder().build());
        BsonDocument child = document.getDocument("intChild");
        Assert.assertNotNull(child);
        Assert.assertEquals(100, child.getInt32("t").intValue());

        child = document.getDocument("stringChild");
        Assert.assertNotNull(child);
        Assert.assertEquals("what what?", child.getString("t").getValue());

        child = document.getDocument("baseType");
        Assert.assertNotNull(child);
        Assert.assertEquals("so tricksy!", child.getString("t").getValue());

        Complex decode = complexClassModel.decode(new BsonDocumentReader(document), DecoderContext.builder().build());
        Assert.assertEquals(complex, decode);

        Complex custom = new Complex(new IntChild(1234), new StringChild("Another round!"),
                                     new BaseType<String>("Mongo just pawn in game of life"));

        document = new BsonDocument();

        complexClassModel.encode(new BsonDocumentWriter(document), custom, EncoderContext.builder().build());
        decode = complexClassModel.decode(new BsonDocumentReader(document), DecoderContext.builder().build());

        Assert.assertEquals(custom, decode);
        Assert.assertEquals(1234, custom.getIntChild().getT().intValue());
        Assert.assertEquals("Another round!", custom.getStringChild().getT());
        Assert.assertEquals("Mongo just pawn in game of life", custom.getBaseType().getT());
    }

    @Test
    public void testTransformingConventions() {
        final ClassModelCodecProvider codecProvider = ClassModelCodecProvider
            .builder()
            .setConventionPack(new TransformingConventionPack())
            .register(SecureEntity.class)
            .build();
        final CodecRegistry registry = CodecRegistries.fromProviders(codecProvider, new ValueCodecProvider());

        final Codec<SecureEntity> codec = registry.get(SecureEntity.class);
        final BsonDocument document = new BsonDocument();
        final BsonDocumentWriter writer = new BsonDocumentWriter(document);
        final SecureEntity entity = new SecureEntity("Bob", "my voice is my passport");

        codec.encode(writer, entity, EncoderContext.builder().build());
        Assert.assertEquals(document.getString("name").getValue(), "Bob");
        Assert.assertEquals(document.getString("password").getValue(), "zl ibvpr vf zl cnffcbeg");

        Assert.assertEquals(entity, codec.decode(new BsonDocumentReader(document), DecoderContext.builder().build()));
    }

    @SuppressWarnings("CheckStyle")
    private static class BaseType<T> {
        private T t;

        public BaseType() {
        }

        public BaseType(final T t) {
            this.t = t;
        }

        public T getT() {
            return t;
        }

        public void setT(final T t) {
            this.t = t;
        }

        @Override
        public int hashCode() {
            return t != null ? t.hashCode() : 0;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof BaseType)) {
                return false;
            }

            final BaseType<?> baseType = (BaseType<?>) o;

            return t != null ? t.equals(baseType.t) : baseType.t == null;

        }

    }

    @SuppressWarnings("CheckStyle")
    private static class IntChild extends BaseType<Integer> {
        public IntChild() {
        }

        public IntChild(final Integer integer) {
            super(integer);
        }
    }

    @SuppressWarnings("CheckStyle")
    private static class StringChild extends BaseType<String> {
        public StringChild() {
        }

        public StringChild(final String s) {
            super(s);
        }
    }

    private static class Complex {
        private IntChild intChild = new IntChild(100);
        private StringChild stringChild = new StringChild("what what?");
        private BaseType<String> baseType = new StringChild("so tricksy!");
        //        private Map<String, Double> map;

        public Complex() {
        }

        public Complex(final IntChild intChild, final StringChild stringChild,
                       final BaseType<String> baseType) {
            this.intChild = intChild;
            this.stringChild = stringChild;
            this.baseType = baseType;
        }

        public BaseType<String> getBaseType() {
            return baseType;
        }

        public Complex setBaseType(final BaseType<String> baseType) {
            this.baseType = baseType;
            return this;
        }

        public IntChild getIntChild() {
            return intChild;
        }

        public Complex setIntChild(final IntChild intChild) {
            this.intChild = intChild;
            return this;
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
    }

    private static class BsonSchemaVisitor extends JsonFormatVisitorWrapper.Base {
        private final ObjectMapper mapper;
        private final List<MessageElementVisitor> elements = new ArrayList<MessageElementVisitor>();

        public BsonSchemaVisitor(final ObjectMapper mapper) {
            this.mapper = mapper;
        }

        public void accept(final Class<?> typeClass) throws JsonMappingException {
            mapper.acceptJsonFormatVisitor(typeClass, this);
        }

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
}


class MessageElementVisitor extends Base {
    private final JavaType type;
    //       private final Builder builder;

    MessageElementVisitor(final JavaType type) {
        this.type = type;
        //           builder = new Builder();
        //           builder.name(type.getRawClass().getSimpleName());
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
        //        System.out.println("************ prop = " + prop);
        //           builder.add(new MongoField(prop));
    }

    @Override
    public void optionalProperty(final String name, final JsonFormatVisitable handler, final JavaType propertyTypeHint)
        throws JsonMappingException {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}

