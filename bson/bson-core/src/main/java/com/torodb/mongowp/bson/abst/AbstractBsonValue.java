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

import com.torodb.mongowp.bson.BsonArray;
import com.torodb.mongowp.bson.BsonBinary;
import com.torodb.mongowp.bson.BsonBoolean;
import com.torodb.mongowp.bson.BsonDateTime;
import com.torodb.mongowp.bson.BsonDbPointer;
import com.torodb.mongowp.bson.BsonDecimal128;
import com.torodb.mongowp.bson.BsonDeprecated;
import com.torodb.mongowp.bson.BsonDocument;
import com.torodb.mongowp.bson.BsonDouble;
import com.torodb.mongowp.bson.BsonInt32;
import com.torodb.mongowp.bson.BsonInt64;
import com.torodb.mongowp.bson.BsonJavaScript;
import com.torodb.mongowp.bson.BsonJavaScriptWithScope;
import com.torodb.mongowp.bson.BsonNull;
import com.torodb.mongowp.bson.BsonNumber;
import com.torodb.mongowp.bson.BsonObjectId;
import com.torodb.mongowp.bson.BsonRegex;
import com.torodb.mongowp.bson.BsonString;
import com.torodb.mongowp.bson.BsonTimestamp;
import com.torodb.mongowp.bson.BsonUndefined;
import com.torodb.mongowp.bson.BsonValue;

abstract class AbstractBsonValue<V> implements BsonValue<V> {

  @Override
  public boolean isNumber() {
    return false;
  }

  @Override
  public boolean isDouble() {
    return false;
  }

  @Override
  public boolean isInt32() {
    return false;
  }

  @Override
  public boolean isInt64() {
    return false;
  }

  @Override
  public boolean isString() {
    return false;
  }

  @Override
  public boolean isDocument() {
    return false;
  }

  @Override
  public boolean isArray() {
    return false;
  }

  @Override
  public boolean isBinary() {
    return false;
  }

  @Override
  public boolean isUndefined() {
    return false;
  }

  @Override
  public boolean isObjectId() {
    return false;
  }

  @Override
  public boolean isBoolean() {
    return false;
  }

  @Override
  public boolean isDateTime() {
    return false;
  }

  @Override
  public boolean isNull() {
    return false;
  }

  @Override
  public boolean isRegex() {
    return false;
  }

  @Override
  public boolean isDbPointer() {
    return false;
  }

  @Override
  public boolean isJavaScript() {
    return false;
  }

  @Override
  public boolean isJavaScriptWithScope() {
    return false;
  }

  @Override
  public boolean isTimestamp() {
    return false;
  }

  @Override
  public boolean isDeprecated() {
    return false;
  }

  @Override
  public boolean isDecimal128() {
    return false;
  }
  
  @Override
  public BsonNumber asNumber() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
        "Values of type " + getType() + " cannot be casted to number");
  }

  @Override
  public BsonDouble asDouble() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
        "Values of type " + getType() + " cannot be casted to double");
  }

  @Override
  public BsonString asString() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
        "Values of type " + getType() + " cannot be casted to string");
  }

  @Override
  public BsonDocument asDocument() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
        "Values of type " + getType() + " cannot be casted to document");
  }

  @Override
  public BsonArray asArray() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
        "Values of type " + getType() + " cannot be casted to array");
  }

  @Override
  public BsonBinary asBinary() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
        "Values of type " + getType() + " cannot be casted to binary");
  }

  @Override
  public BsonUndefined asUndefined() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
        "Values of type " + getType() + " cannot be casted to undefined");
  }

  @Override
  public BsonObjectId asObjectId() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
        "Values of type " + getType() + " cannot be casted to objectId");
  }

  @Override
  public BsonBoolean asBoolean() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
        "Values of type " + getType() + " cannot be casted to boolean");
  }

  @Override
  public BsonDateTime asDateTime() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
        "Values of type " + getType() + " cannot be casted to dateTime");
  }

  @Override
  public BsonNull asNull() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
        "Values of type " + getType() + " cannot be casted to null");
  }

  @Override
  public BsonRegex asRegex() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
        "Values of type " + getType() + " cannot be casted to regex");
  }

  @Override
  public BsonDbPointer asDbPointer() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
        "Values of type " + getType() + " cannot be casted to dbPointer");
  }

  @Override
  public BsonJavaScript asJavaScript() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
        "Values of type " + getType() + " cannot be casted to javaScript");
  }

  @Override
  public BsonJavaScriptWithScope asJavaScriptWithScope() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
        "Values of type " + getType() + " cannot be casted to javaScript with scope");
  }

  @Override
  public BsonInt32 asInt32() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
        "Values of type " + getType() + " cannot be casted to int32");
  }

  @Override
  public BsonTimestamp asTimestamp() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
        "Values of type " + getType() + " cannot be casted to timestamp");
  }

  @Override
  public BsonInt64 asInt64() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
        "Values of type " + getType() + " cannot be casted to int64");
  }

  @Override
  public BsonDecimal128 asDecimal128() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
        "Values of type " + getType() + " cannot be casted to decimal128");
  }
  
  @Override
  public BsonDeprecated asDeprecated() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
        "Values of type " + getType() + " cannot be casted to deprecated");
  }

}
