/*
 * Copyright (C) 2017 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jakewharton.moshi.rs;

import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NoContentException;
import javax.ws.rs.ext.MessageBodyReader;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import org.junit.Test;

import static java.util.Collections.singletonList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public final class MoshiMessageBodyReaderTest {
  private final Moshi moshi = new Moshi.Builder().build();
  private final MessageBodyReader<Object> reader = new MoshiMessageBodyReader(moshi);

  @Test public void applies() {
    assertTrue(
        reader.isReadable(Object.class, Object.class, new Annotation[0], APPLICATION_JSON_TYPE));
    assertTrue(reader.isReadable(Object.class, Object.class, new Annotation[0],
        new MediaType("application", "json", "UTF-8")));
    assertFalse(
        reader.isReadable(Object.class, Object.class, new Annotation[0], APPLICATION_XML_TYPE));
  }

  @Test public void emptyReadThrows() throws IOException {
    Buffer data = new Buffer();
    Class<Object> type = (Class) String.class;
    try {
      reader.readFrom(type, type, new Annotation[0], APPLICATION_JSON_TYPE,
          new MultivaluedHashMap<>(),
          data.inputStream());
      fail();
    } catch (NoContentException ignored) {
    }
  }

  @Test public void readsClass() throws IOException {
    Buffer data = new Buffer().writeUtf8("\"hey\"");
    Class<Object> type = (Class) String.class;
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    Object result = reader.readFrom(type, type, new Annotation[0], APPLICATION_JSON_TYPE,
        headers, data.inputStream());
    assertEquals("hey", result);
    assertTrue(headers.isEmpty());
  }

  @Test public void readsParameterized() throws IOException {
    Buffer data = new Buffer().writeUtf8("[\"hey\"]");
    Class<Object> type = (Class) List.class;
    Type genericType = Types.newParameterizedType(List.class, String.class);
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    Object result = reader.readFrom(type, genericType, new Annotation[0], APPLICATION_JSON_TYPE,
        headers, data.inputStream());
    assertEquals(singletonList("hey"), result);
    assertTrue(headers.isEmpty());
  }

  @Test public void readDoesNotClose() throws IOException {
    final AtomicBoolean closed = new AtomicBoolean();
    BufferedSource data = Okio.buffer(new ForwardingSource(new Buffer().writeUtf8("\"hey\"")) {
      @Override public void close() throws IOException {
        closed.set(true);
        super.close();
      }
    });
    Class<Object> type = (Class) String.class;
    reader.readFrom(type, type, new Annotation[0], APPLICATION_JSON_TYPE,
        new MultivaluedHashMap<>(), data.inputStream());
    assertFalse(closed.get());
  }
}
