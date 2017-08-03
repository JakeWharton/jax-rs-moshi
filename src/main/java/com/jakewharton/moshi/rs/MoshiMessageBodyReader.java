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
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NoContentException;
import javax.ws.rs.ext.MessageBodyReader;
import okio.BufferedSource;
import okio.Okio;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

/**
 * A {@link MessageBodyReader} which uses Moshi to deserialize JSON to the declared body type.
 * Your endpoint must be annotated as consuming {@code application/json}.
 */
public final class MoshiMessageBodyReader implements MessageBodyReader<Object> {
  private final Moshi moshi;

  public MoshiMessageBodyReader(Moshi moshi) {
    this.moshi = moshi;
  }

  @Override public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return mediaType.isCompatible(APPLICATION_JSON_TYPE);
  }

  @Override public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {
    JsonAdapter<Object> adapter = moshi.adapter(genericType);
    BufferedSource source = Okio.buffer(Okio.source(entityStream));
    if (!source.request(1)) {
      throw new NoContentException("Stream is empty");
    }
    return adapter.fromJson(source);
    // Note: we do not close the InputStream per the interface documentation.
  }
}
