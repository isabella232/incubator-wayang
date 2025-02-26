/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.wayang.core.util.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import org.apache.wayang.core.api.exception.WayangException;

/**
 * JSONObject is the wrapper for the {@link ObjectNode} to enable the
 * similar functionality as the http://javadox.com/org.json/json/20151123/org/json/JSONObject.html
 *
 * TODO: require implement the rest of the functionalities follow the link below
 * https://github.com/stleary/JSON-java/blob/master/src/main/java/org/json/JSONObject.java
 *
 * TODO: the java doc is not done because is missing implementation and it performed some
 *       modification on the code
 */
public class JSONObject {
  public static final NullNode NULL = NullNode.getInstance();

  private ObjectNode node;

  public JSONObject(){
    ObjectMapper mapper = new ObjectMapper();
    // create a JSON object
    this.node = mapper.createObjectNode();
  }

  public JSONObject(String content){
    ObjectMapper mapper = new ObjectMapper();
    // create a JSON object
    try {
      this.node = (ObjectNode) mapper.readTree(content);
    } catch (JsonProcessingException e) {
      throw new WayangException(e);
    }
  }

  public JSONObject(Map<String, Object> map){
    ObjectMapper mapper = new ObjectMapper();
    this.node = mapper.createObjectNode();
    // create a JSON object
    map.entrySet()
        .stream()
        .forEach(
          entry -> {
            insertType(entry.getValue()).accept(entry.getKey(), entry.getValue());
          }
        );

  }

  JSONObject(ObjectNode node){
    this.node = node;
  }

  public ObjectNode getNode(){
    return this.node;
  }

  public boolean has(String key){
    return this.node.has(key);
  }

  public String get(String key){
    return this.node.get(key).asText();
  }

  public String getString(String key){
    return this.node.get(key).asText();
  }

  public double getDouble(String key){
    return this.node.get(key).asDouble();
  }

  public long getLong(String key){
    return this.node.get(key).asLong();
  }

  public int getInt(String key){
    return this.node.get(key).asInt();
  }

  public JSONObject getJSONObject(String key){
    JsonNode value = this.getNode().get(key);
    if(value == null){
      return null;
    }
    if(!value.isObject()) {
      throw new WayangException("the key does not exist on the component");
    }
    return new JSONObject((ObjectNode) value);
  }

  public JSONArray getJSONArray(String key){
    JsonNode value = this.getNode().get(key);
    if(value == null){
      return null;
    }
    if(!value.isArray()) {
      throw new WayangException("the key does not exist on the component");
    }
    return new JSONArray((ArrayNode) value);
  }

  public JSONObject put(String key, String value){
    this.getNode().put(key, value);
    return this;
  }
  public JSONObject put(String key, int value){
    this.getNode().put(key, value);
    return this;
  }

  public JSONObject put(String key, double value){
    this.getNode().put(key, value);
    return this;
  }

  public JSONObject put(String key, long value){
    this.getNode().put(key, value);
    return this;
  }

  public JSONObject put(String key, Object value){
    this.insertType(value).accept(key, value);
    return this;
  }

  public void write(BufferedWriter writter) throws IOException {
    writter.write(this.getNode().toString());
  }

  public JSONObject put(String key, JSONObject value){
    if(this.getNode().has(key)){
      this.getNode().replace(key, (value == null)? NULL:value.getNode());
    }else {
      this.getNode().set(key, (value == null)? NULL: value.getNode());
    }
    return this;
  }

  public JSONObject put(String key, JSONArray value){
    if(this.getNode().has(key)){
      this.getNode().replace(key, value.getNode());
    }else {
      this.getNode().set(key, value.getNode());
    }
    return this;
  }

  public JSONObject putOpt(String key, Object value){
    if(value == null){
      return this;
    }
    this.put(key, value);
    return this;
  }

  public JSONObject optJSONObject(String key){
    try {
      return this.getJSONObject(key);
    }catch (WayangException ex){
      return null;
    }
  }

  public JSONArray optJSONArray(String key){
    try {
      return this.getJSONArray(key);
    }catch (WayangException ex){
      return null;
    }
  }

  public double optDouble(String key){
    return this.optDouble(key, Double.NaN);
  }

  public double optDouble(String key, double value){
    try {
      return this.getDouble(key);
    }catch (WayangException ex){
      return value;
    }
  }

  public Set<String> keySet(){
    return Sets.newHashSet(Iterators.filter(this.node.fieldNames(), String.class));
  }

  public int length(){
    return this.node.size();
  }

  BiConsumer<String, Object> insertType(Object value){
    ObjectNode node = this.getNode();
    if(value == null){
      return (k, v) -> {
        if(node.has(k)){
          node.replace(k, NULL);
        }else{
          node.set(k, NULL);
        }
      };
    }else if(value instanceof Integer){
      return (k, v) ->  node.put(k, (Integer) v);
    }else if(value instanceof Long){
      return (k, v) ->  node.put(k, (Long) v);
    }else if(value instanceof Float){
      return (k, v) ->  node.put(k, (Float) v);
    }else if(value instanceof String){
      return (k, v) ->  node.put(k, (String) v);
    }else if(value instanceof Double){
      return (k, v) ->  node.put(k, (Double) v);
    }else if(value instanceof ObjectNode){
      return (k, v) -> {
        if(node.has(k)){
          node.replace(k, (ObjectNode)v);
        }else{
          node.set(k, (ObjectNode)v);
        }
      };
    }else if(value instanceof ArrayNode){
      return (k, v) -> {
        if(node.has(k)){
          node.replace(k, (ArrayNode)v);
        }else{
          node.set(k, (ArrayNode)v);
        }
      };
    }else if(value instanceof JSONArray){
      return (k, v) -> {
        if(node.has(k)){
          node.replace(k, ((JSONArray)v).getNode());
        }else{
          node.set(k, ((JSONArray)v).getNode());
        }
      };
    }else if(value instanceof JSONObject){
      return (k, v) -> {
        if(node.has(k)){
          node.replace(k, ((JSONObject)v).getNode());
        }else{
          node.set(k, ((JSONObject)v).getNode());
        }
      };
    }
    throw new WayangException("The type is not recognizable "+ value.getClass());
  }

  @Override
  public String toString() {
    return this.getNode().toString();
  }
}
