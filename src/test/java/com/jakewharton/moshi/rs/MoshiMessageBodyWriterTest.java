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
import javax.ws.rs.ext.MessageBodyWriter;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import org.junit.Test;

import static java.util.Collections.singletonList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class MoshiMessageBodyWriterTest {
  private final Moshi moshi = new Moshi.Builder().build();
  private final MessageBodyWriter<Object> writer = new MoshiMessageBodyWriter(moshi);

  @Test public void applies() {
    assertTrue(
        writer.isWriteable(Object.class, Object.class, new Annotation[0], APPLICATION_JSON_TYPE));
    assertTrue(writer.isWriteable(Object.class, Object.class, new Annotation[0],
        new MediaType("application", "json", "UTF-8")));
    assertFalse(
        writer.isWriteable(Object.class, Object.class, new Annotation[0], APPLICATION_XML_TYPE));
  }

  @Test public void writesClass() throws IOException {
    Buffer data = new Buffer();
    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    writer.writeTo("hey", String.class, String.class, new Annotation[0], APPLICATION_JSON_TYPE,
        headers, data.outputStream());
    assertEquals("\"hey\"", data.readUtf8());
    assertTrue(headers.isEmpty());
  }

  @Test public void writesParameterized() throws IOException {
    Buffer data = new Buffer();
    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    Type genericType = Types.newParameterizedType(List.class, String.class);
    writer.writeTo(singletonList("hey"), List.class, genericType, new Annotation[0],
        APPLICATION_JSON_TYPE, headers, data.outputStream());
    assertEquals("[\"hey\"]", data.readUtf8());
    assertTrue(headers.isEmpty());
  }

  @Test public void writeDoesNotClose() throws IOException {
    final AtomicBoolean closed = new AtomicBoolean();
    BufferedSink data = Okio.buffer(new ForwardingSink(new Buffer()) {
      @Override public void close() throws IOException {
        closed.set(true);
        super.close();
      }
    });
    writer.writeTo("hey", String.class, String.class, new Annotation[0], APPLICATION_JSON_TYPE,
        new MultivaluedHashMap<>(), data.outputStream());
    assertFalse(closed.get());
  }
}
