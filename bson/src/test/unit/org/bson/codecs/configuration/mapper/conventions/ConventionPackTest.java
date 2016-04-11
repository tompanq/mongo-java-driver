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
import org.bson.codecs.configuration.mapper.entities.Address;
import org.bson.codecs.configuration.mapper.entities.BaseGenericType;
import org.bson.codecs.configuration.mapper.entities.Complex;
import org.bson.codecs.configuration.mapper.entities.ContainerTypes;
import org.bson.codecs.configuration.mapper.entities.Entity;
import org.bson.codecs.configuration.mapper.entities.IntChild;
import org.bson.codecs.configuration.mapper.entities.NamedStringChild;
import org.bson.codecs.configuration.mapper.entities.Person;
import org.bson.codecs.configuration.mapper.entities.SecureEntity;
import org.bson.codecs.configuration.mapper.entities.StringChild;
import org.bson.codecs.configuration.mapper.entities.ZipCode;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("CheckStyle")
public class ConventionPackTest {

    public CodecRegistry getCodecRegistry() {
        final ClassModelCodecProvider codecProvider = ClassModelCodecProvider
            .builder()
            .register(BaseGenericType.class)
            .register(IntChild.class)
            .register(StringChild.class)
            .register(NamedStringChild.class)
            .register(Complex.class)
            .register(ContainerTypes.class)
            .build();
        return CodecRegistries.fromProviders(codecProvider, new ValueCodecProvider());
    }

    @Test
    public void testCollectionNesting() {
        final CodecRegistry registry = getCodecRegistry();
        ContainerTypes types = new ContainerTypes();
        final List<List<? extends BaseGenericType<?>>> doubleList =
            Arrays.asList(
                Arrays.<BaseGenericType<?>>asList(new IntChild(1), new IntChild(2)),
                Arrays.asList(new IntChild(3), new StringChild("Dee"))
                         );
        types.setDoubleList(doubleList);


        roundTrip(registry, types);

        types = new ContainerTypes();
        final List<List<List<? extends BaseGenericType<?>>>> tripleList = Arrays.asList(doubleList, doubleList);
        types.setTripleList(tripleList);
        roundTrip(registry, types);
    }

    @Test
    public void testCollections() {
        final CodecRegistry registry = getCodecRegistry();
        final ContainerTypes types = new ContainerTypes();
        types.setList(Arrays.asList(new IntChild(1), new IntChild(2), new IntChild(3), new StringChild("Dee")));

        roundTrip(registry, types);
    }

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
    public void testGenerics() {
        final ClassModelCodecProvider codecProvider = ClassModelCodecProvider
            .builder()
            .register(BaseGenericType.class)
            .register(IntChild.class)
            .register(StringChild.class)
            .register(Complex.class)
            .build();
        final CodecRegistry registry = CodecRegistries.fromProviders(codecProvider, new ValueCodecProvider());

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

        final Complex custom = new Complex(new IntChild(1234), new StringChild("Another round!"),
                                           new BaseGenericType<String>("Mongo just pawn in game of life"));

        document = new BsonDocument();

        complexClassModel.encode(new BsonDocumentWriter(document), custom, EncoderContext.builder().build());
        decode = complexClassModel.decode(new BsonDocumentReader(document), DecoderContext.builder().build());

        Assert.assertEquals(custom, decode);
        Assert.assertEquals(1234, custom.getIntChild().getT().intValue());
        Assert.assertEquals("Another round!", custom.getStringChild().getT());
        Assert.assertEquals("Mongo just pawn in game of life", custom.getBaseType().getT());
    }

    @Test
    public void testMapNesting() {
        ContainerTypes types = new ContainerTypes();
        final HashMap<String, Integer> map1 = new HashMap<String, Integer>();
        for (int i = 0; i < 10; i++) {
            map1.put(i + "", 10 - i);
        }
        final HashMap<String, Integer> map2 = new HashMap<String, Integer>();
        for (int i = 0; i < 10; i++) {
            map2.put(i + "", i);
        }
        final HashMap<String, Map<String, Integer>> map = new HashMap<String, Map<String, Integer>>();
        map.put("map1", map1);
        map.put("map2", map2);
        types.setDoubleMap(map);

        roundTrip(getCodecRegistry(), types);

        final Map<String, Map<String, Map<String, Integer>>> bigMap = new HashMap<String, Map<String, Map<String, Integer>>>();
        bigMap.put("uber", map);
        types = new ContainerTypes();
        types.setTripleMap(bigMap);

        roundTrip(getCodecRegistry(), types);
    }

    @Test
    public void testMaps() {
        final ContainerTypes types = new ContainerTypes();
        final HashMap<String, Integer> map = new HashMap<String, Integer>();
        for (int i = 0; i < 10; i++) {
            map.put(i + "", 10 - i);
        }
        types.setMap(map);

        roundTrip(getCodecRegistry(), types);
    }

    @Test
    public void testPolymorphism() {
        final CodecRegistry registry = getCodecRegistry();

        final BsonDocument document = new BsonDocument();
        final StringChild bruce = new NamedStringChild("string child", "Bruce");
        final Complex complex = new Complex(new IntChild(1), new StringChild("Kung Pow"), bruce);
        registry.get(Complex.class).encode(new BsonDocumentWriter(document), complex, EncoderContext.builder().build());
        final Complex decoded = registry.get(Complex.class).decode(new BsonDocumentReader(document), DecoderContext.builder().build());

        Assert.assertTrue("The baseType field should be a NamedStringChild", decoded.getBaseType() instanceof NamedStringChild);
        Assert.assertEquals(complex, decoded);
    }

    @Test
    public void testSets() {
        final CodecRegistry registry = getCodecRegistry();
        ContainerTypes types = new ContainerTypes();
        final Set<? extends BaseGenericType<?>> set = new HashSet<BaseGenericType<?>>(
            Arrays.asList(new IntChild(1), new IntChild(2), new IntChild(3), new StringChild("Dee")));
        types.setSet(set);
        roundTrip(registry, types);

        types = new ContainerTypes();
        final Set<Set<? extends BaseGenericType<?>>> doubleSet = new HashSet<Set<? extends BaseGenericType<?>>>();
        doubleSet.add(set);
        doubleSet.add(set);
        types.setDoubleSet(doubleSet);
        roundTrip(registry, types);

        types = new ContainerTypes();
        final Set<Set<Set<? extends BaseGenericType<?>>>> tripleSet = new HashSet<Set<Set<? extends BaseGenericType<?>>>>();
        tripleSet.add(doubleSet);
        tripleSet.add(doubleSet);
        types.setTripleSet(tripleSet);
    }

    @Test
    public void testMixedNesting() {
        ContainerTypes types = new ContainerTypes();
        Map<String, List<Set<? extends BaseGenericType<?>>>> mixed = new HashMap<String, List<Set<? extends BaseGenericType<?>>>>();
        final Set<? extends BaseGenericType<?>> set1 = new HashSet<BaseGenericType<?>>(
            Arrays.asList(new IntChild(1), new IntChild(2), new IntChild(3), new StringChild("Dee")));
        final Set<? extends BaseGenericType<?>> set2 = new HashSet<BaseGenericType<?>>(Collections.singletonList(new StringChild("Dee")));
        mixed.put("first", Arrays.asList(set1, set2));
        mixed.put("swapped", Arrays.asList(set2, set1, set2));
        types.setMixed(mixed);

        roundTrip(getCodecRegistry(), types);
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

    private <T> void roundTrip(final CodecRegistry registry, final T object) {
        final DecoderContext decoderContext = DecoderContext.builder().build();
        final EncoderContext encoderContext = EncoderContext.builder().build();

        final BsonDocument document = new BsonDocument();
        final Class<T> klass = (Class<T>) object.getClass();
        registry.get(klass).encode(new BsonDocumentWriter(document), object, encoderContext);
        final Object decoded = registry.get(klass).decode(new BsonDocumentReader(document), decoderContext);
        Assert.assertEquals(object, decoded);
    }
}
