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

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import okio.BufferedSink;
import okio.Okio;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

/**
 * A {@link MessageBodyWriter} which uses Moshi to serialized the declared body type to JSON.
 * Your endpoint must be annotated as producing {@code application/json}.
 */
public final class MoshiMessageBodyWriter implements MessageBodyWriter<Object> {
  private final Moshi moshi;

  public MoshiMessageBodyWriter(Moshi moshi) {
    this.moshi = moshi;
  }

  @Override public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return mediaType.isCompatible(APPLICATION_JSON_TYPE);
  }

  @Override public long getSize(Object o, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  @Override public void writeTo(Object o, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    JsonAdapter<Object> adapter = moshi.adapter(genericType);
    BufferedSink sink = Okio.buffer(Okio.sink(entityStream));
    adapter.toJson(sink, o);
    sink.emit();
    // Note: we do not close the OutputStream per the interface documentation.
  }
}
