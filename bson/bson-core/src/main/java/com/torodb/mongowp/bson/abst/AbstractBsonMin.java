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
package com.torodb.mongowp.bson.abst;

import com.torodb.mongowp.bson.BsonMin;
import com.torodb.mongowp.bson.BsonType;
import com.torodb.mongowp.bson.BsonValue;
import com.torodb.mongowp.bson.BsonValueVisitor;
import com.torodb.mongowp.bson.utils.BsonTypeComparator;

public abstract class AbstractBsonMin extends AbstractBsonValue<BsonMin> implements BsonMin {

  @Override
  public Class<? extends BsonMin> getValueClass() {
    return this.getClass();
  }

  @Override
  public BsonMin getValue() {
    return this;
  }

  @Override
  public BsonType getType() {
    return BsonType.MIN;
  }

  @Override
  public int compareTo(BsonValue<?> obj) {
    if (obj == this) {
      return 0;
    }
    int diff = BsonTypeComparator.INSTANCE.compare(getType(), obj.getType());
    if (diff != 0) {
      return diff;
    }

    assert obj instanceof BsonMin;
    return 0;
  }

  @Override
  public final boolean equals(Object obj) {
    return this == obj || obj != null && obj instanceof BsonMin;
  }

  @Override
  public final int hashCode() {
    return HASH;
  }

  @Override
  public <R, A> R accept(BsonValueVisitor<R, A> visitor, A arg) {
    return visitor.visit(this, arg);
  }

}
