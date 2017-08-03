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
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;

/**
 * A {@link ParamConverterProvider} which uses Moshi to deserialize JSON to the declared parameter
 * type. The parameter must be annotated with {@link Json} for the converter to apply.
 */
public final class MoshiParamConverterFactory implements ParamConverterProvider {
  private final Moshi moshi;

  public MoshiParamConverterFactory(Moshi moshi) {
    this.moshi = moshi;
  }

  @Override public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType,
      Annotation[] annotations) {
    for (Annotation annotation : annotations) {
      if (annotation instanceof Json) {
        JsonAdapter<T> adapter = moshi.adapter(genericType);
        return new MoshiParamConverter<>(adapter);
      }
    }
    return null;
  }

  private static final class MoshiParamConverter<T> implements ParamConverter<T> {
    private final JsonAdapter<T> adapter;

    MoshiParamConverter(JsonAdapter<T> adapter) {
      this.adapter = adapter;
    }

    @Override public T fromString(String value) {
      try {
        return adapter.fromJson(value);
      } catch (IOException e) {
        throw new IllegalArgumentException(e);
      }
    }

    @Override public String toString(T value) {
      return adapter.toJson(value);
    }
  }
}
