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
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import org.junit.Test;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public final class MoshiParamConverterFactoryTest {
  private final Moshi moshi = new Moshi.Builder().build();
  private final ParamConverterProvider provider = new MoshiParamConverterFactory(moshi);

  @Test public void noAnnotationReturnsNull() {
    ParamConverter<String> converter =
        provider.getConverter(String.class, String.class, new Annotation[0]);
    assertNull(converter);
  }

  @Test public void differentJsonAnnotationReturnsNull() {
    ParamConverter<String> converter =
        provider.getConverter(String.class, String.class, new Annotation[] {
            Annotations.other()
        });
    assertNull(converter);
  }

  @Test public void jsonAnnotationReturnsConverterClass() {
    ParamConverter<String> converter =
        provider.getConverter(String.class, String.class, new Annotation[] {
            Annotations.real()
        });
    String value = converter.fromString("\"hey\"");
    assertEquals("hey", value);
    String json = converter.toString("hey");
    assertEquals("\"hey\"", json);
  }

  @Test public void jsonAnnotationReturnsConverterParameterized() {
    Type genericType = Types.newParameterizedType(List.class, String.class);
    ParamConverter<List<String>> converter =
        (ParamConverter) provider.getConverter(List.class, genericType, new Annotation[] {
            Annotations.real()
        });
    List<String> value = converter.fromString("[\"hey\"]");
    assertEquals(singletonList("hey"), value);
    String json = converter.toString(singletonList("hey"));
    assertEquals("[\"hey\"]", json);
  }
}
