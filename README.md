JAX-RS Moshi JSON Handlers
==========================

A message body reader/writer and parameter converter which uses Moshi to handle JSON.



Usage
-----

```java
ResourceConfig resourceConfig = new ResourceConfig();

Moshi moshi = new Moshi.Builder().build();

// Handles any request body marked as consuming application/json.
resourceConfig.register(new MoshiMessageBodyReader(moshi));
// Handles any response body marked as producing application/json.
resourceConfig.register(new MoshiMessageBodyWriter(moshi));
// Handles any parameter annotated with @Json.
resourceConfig.register(new MoshiParamConverterFactory(moshi));
```

_(This example uses Jersey, but any JAX-RS-compatible implementation will work.)_

The message body converters allow JSON serialization for request and response bodies:
```java
@POST @Path("register") //
@Consumes(MediaType.APPLICATION_JSON) //
@Produces(MediaType.APPLICATION_JSON) //
public RegisterResponse register(RegisterRequest registerRequest) {
  // ...
}
```

The parameter converter allow JSON serialization for `@Json`-annotated parameters.
```java
@POST @Path("checkin") //
@Consumes(MediaType.APPLICATION_FORM_URLENCODED) //
@Produces(MediaType.APPLICATION_JSON) //
public CheckinResponse checkin( //
    @FormParam("user") @Json User user) {
  // ...
}
```



Download
--------

Gradle:
```groovy
compile 'com.jakewharton:jax-rs-moshi:1.0.0'
```
or Maven:
```xml
<dependency>
  <groupId>com.jakewharton</groupId>
  <artifactId>jax-rs-moshi</artifactId>
  <version>1.0.0</version>
</dependency>
```

Snapshot versions are available in the Sonatype 'snapshots' repository: https://oss.sonatype.org/content/repositories/snapshots/



License
-------

    Copyright 2017 Jake Wharton

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
