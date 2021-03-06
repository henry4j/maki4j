#### maki4j has a home for **bimock** tool that helps us record & replay public objects and their methods for unit tests.

* Downloads: [maki4j-0.2.jar](https://raw.github.com/henry4j/maki4j/master/target/maki4j-0.2.jar), [maki4j-0.2-sources.jar](https://raw.github.com/henry4j/maki4j/master/target/maki4j-0.2-sources.jar)

##### Introduction

* How about hardcoding stubbing one, or consecutive calls?
  * the general idea is once stubbed, the method will always return stubbed value regardless of how many times it is called. Last stubbing is more important - when you stubbed the same method with the same arguments many times. Sometimes we need to stub with different return value/exception for the same method call. Typical use case could be mocking iterators. Original version of Mockito did not have this feature to promote simple mocking. For example, instead of iterators one could use Iterable or simply collections. Those offer natural ways of stubbing (e.g. using real collections). In rare scenarios stubbing consecutive calls could be useful, though -- yes, it certainly works, but we often gave this up.
* How about automating stubbing arbitrary calls on public methods?
  * let's use a bimock (bidirectional mock) which has a factory method `of` that takes a real object, a mode of record, or replay, and a resource file.
  * when in Record mode, it records method invocations with return values or exceptions into the resource file in the JSON format.
  * when in Replay mode, it sets up method invocations and answers out of the resource file when it starts up, and replays answers of returns or throws.
  * also, it throws up a runtime exception to indicate a potential bug, as soon as unexpected, or additional method invocations happen on the bimock.

##### Getting Started

* Declare Spring Beans in AppConfig.java, or application-config.xml

###### example @Bean definitions in AppConfig.java

```java
@Configuration
public class AppConfig {
    @Bean @Autowired(required = true)
    Bimock bimock(PojoMapper pojoMapper) {
        return new com.henry4j.commons.stubbing.Bimock(pojoMapper);
    }
 
    @Bean
    Module bimockModule() {
        return new com.henry4j.commons.stubbing.BimockModule();
    }
 
    @Bean @Autowired(required = true)
    PojoMapper pojoMapper(Module... modules) {
        return new com.henry4j.commons.base.PojoMapper(modules);
    }
}
```

###### example `<bean />` definitions in application-config.xml

```java
<beans>
  <!-- Be cautious of BeanCurrentlyInCreationException: Error creating bean 'pojoMapper': Requested bean is currently in creation -->
  <bean id="bimock" class="com.henry4j.commons.stubbing.Bimock" />
  <bean id="bimockModule" class="com.henry4j.commons.stubbing.BimockModule" />
  <bean id="pojoMapper" class="com.henry4j.commons.base.PojoMapper" />
</beans>
```

###### example unit tests that use bimock and playback files

* [BimockTest](https://github.com/henry4j/maki4j/blob/master/sources/maki4j/src/test/java/com/henry4j/commons/BimockTest.java) -- this is a self test with its resources, [test-record-and-replay-list.json](https://github.com/henry4j/maki4j/blob/master/sources/maki4j/src/test/resources/test-record-and-replay-list.json), and [test-record-and-replay-map.json](https://github.com/henry4j/maki4j/blob/master/sources/maki4j/src/test/resources/test-record-and-replay-map.json)

```java
public class BimockTest {
    private Mode mode = Mode.Replay;
    private PojoMapper pojoMapper = new PojoMapper(new BimockModule());
    private Bimock bimock = new Bimock(pojoMapper);

    @Test
    public void testRecordAndReplayMap() throws IOException {
        val map = bimock.of(new HashMap<String, Integer>(), mode, new File("src/test/resources/test-record-and-replay-map.json"));
        assertThat(map.put("abc", 3), equalTo(null));
        assertThat(map.size(), equalTo(1));
        assertThat(map.get("abc"), equalTo(3));
    }

    @Test
    public void testRecordAndReplayList() {
        List<Long> list = new ArrayList<Long>();
        list = bimock.of(list, mode, new File("src/test/resources/test-record-and-replay-list.json"));
        try {
            assertThat(list.remove(-1), nullValue());
            fail();
        } catch (ArrayIndexOutOfBoundsException e) {
            assertThat(e.getMessage(), equalTo("-1"));
        }
        assertThat(list.add(100L), equalTo(true));
        assertThat(list.toArray(new Long[1]), equalTo(new Long[] { 100L }));
    }
}
```

##### Bimock Implementation in 3 classes

* [Bimock](https://github.com/henry4j/maki4j/blob/master/sources/maki4j/src/main/java/com/henry4j/commons/stubbing/Bimock.java) -- this is the main module that depends on PojoMapper and BimockModule.
* [BimockModule](https://github.com/henry4j/maki4j/blob/master/sources/maki4j/src/main/java/com/henry4j/commons/stubbing/BimockModule.java) -- this Jackson module contains mix-ins that control serialization and deserialization.
* [PojoMapper](https://github.com/henry4j/maki4j/blob/master/sources/maki4j/src/main/java/com/henry4j/commons/base/PojoMapper.java) -- this is the mapper that serializes POJO to JSON and deserializes POJO from JSON.

##### When in Trouble with Bimock

* Resources when to customize serialization & deserialization, or contact me:
  * http://wiki.fasterxml.com/JacksonMixInAnnotations
  * http://wiki.fasterxml.com/JacksonPolymorphicDeserialization

##### Additional Resources

* [Goodbye! to expect-run-verify](http://monkeyisland.pl/2008/02/01/deathwish/)
* [Mockito Tutorial](http://docs.mockito.googlecode.com/hg/latest/org/mockito/Mockito.html)
