package com.henry4j.commons;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.val;

import org.junit.Test;

import com.henry4j.commons.base.PojoMapper;
import com.henry4j.commons.stubbing.Bimock;
import com.henry4j.commons.stubbing.Bimock.Mode;
import com.henry4j.commons.stubbing.BimockModule;

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

    @Test
    public void testByteBufferMixIns() {
        val bb1 = ByteBuffer.wrap(new byte[] {1, 2, 3, 4});
        assertThat(bb1.get(), equalTo((byte)1));
        val json = pojoMapper.toJson(bb1);
        val bb2 = pojoMapper.fromJson(json, ByteBuffer.class);
        assertThat(bb2.get(), equalTo((byte)2));
    }
}
