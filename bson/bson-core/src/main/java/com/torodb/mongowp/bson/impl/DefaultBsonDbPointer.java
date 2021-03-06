/*
 * Copyright 2014 8Kdata Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.torodb.mongowp.bson.impl;

import com.torodb.mongowp.bson.BsonObjectId;
import com.torodb.mongowp.bson.abst.AbstractBsonDbPointer;

/**
 *
 */
public class DefaultBsonDbPointer extends AbstractBsonDbPointer {

  private static final long serialVersionUID = 4876427863475633401L;

  private final String string;
  private final BsonObjectId id;

  public DefaultBsonDbPointer(String string, BsonObjectId id) {
    this.string = string;
    this.id = id;
  }

  @Override
  public BsonObjectId getId() {
    return id;
  }

  @Override
  public String getNamespace() {
    return string;
  }

}
