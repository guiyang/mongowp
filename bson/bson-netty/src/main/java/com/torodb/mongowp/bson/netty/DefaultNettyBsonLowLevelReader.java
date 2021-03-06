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
package com.torodb.mongowp.bson.netty;

import com.google.common.primitives.UnsignedBytes;
import com.torodb.mongowp.bson.BsonArray;
import com.torodb.mongowp.bson.BsonBinary;
import com.torodb.mongowp.bson.BsonBoolean;
import com.torodb.mongowp.bson.BsonDateTime;
import com.torodb.mongowp.bson.BsonDbPointer;
import com.torodb.mongowp.bson.BsonDecimal128;
import com.torodb.mongowp.bson.BsonDocument;
import com.torodb.mongowp.bson.BsonDocument.Entry;
import com.torodb.mongowp.bson.BsonDouble;
import com.torodb.mongowp.bson.BsonInt32;
import com.torodb.mongowp.bson.BsonInt64;
import com.torodb.mongowp.bson.BsonJavaScript;
import com.torodb.mongowp.bson.BsonJavaScriptWithScope;
import com.torodb.mongowp.bson.BsonMax;
import com.torodb.mongowp.bson.BsonMin;
import com.torodb.mongowp.bson.BsonNull;
import com.torodb.mongowp.bson.BsonObjectId;
import com.torodb.mongowp.bson.BsonRegex;
import com.torodb.mongowp.bson.BsonString;
import com.torodb.mongowp.bson.BsonTimestamp;
import com.torodb.mongowp.bson.BsonUndefined;
import com.torodb.mongowp.bson.BsonValue;
import com.torodb.mongowp.bson.impl.ByteArrayBsonBinary;
import com.torodb.mongowp.bson.impl.ByteArrayBsonObjectId;
import com.torodb.mongowp.bson.impl.DefaultBsonDbPointer;
import com.torodb.mongowp.bson.impl.DefaultBsonJavaScript;
import com.torodb.mongowp.bson.impl.DefaultBsonJavaScriptWithCode;
import com.torodb.mongowp.bson.impl.DefaultBsonRegex;
import com.torodb.mongowp.bson.impl.DefaultBsonTimestamp;
import com.torodb.mongowp.bson.impl.FalseBsonBoolean;
import com.torodb.mongowp.bson.impl.ListBsonArray;
import com.torodb.mongowp.bson.impl.LongBsonDateTime;
import com.torodb.mongowp.bson.impl.LongsBsonDecimal128;
import com.torodb.mongowp.bson.impl.MapBasedBsonDocument;
import com.torodb.mongowp.bson.impl.PrimitiveBsonDouble;
import com.torodb.mongowp.bson.impl.PrimitiveBsonInt32;
import com.torodb.mongowp.bson.impl.PrimitiveBsonInt64;
import com.torodb.mongowp.bson.impl.SimpleBsonMax;
import com.torodb.mongowp.bson.impl.SimpleBsonMin;
import com.torodb.mongowp.bson.impl.SimpleBsonNull;
import com.torodb.mongowp.bson.impl.SimpleBsonUndefined;
import com.torodb.mongowp.bson.impl.StringBsonDeprecated;
import com.torodb.mongowp.bson.impl.StringBsonString;
import com.torodb.mongowp.bson.impl.TrueBsonBoolean;
import com.torodb.mongowp.bson.netty.annotations.Loose;
import com.torodb.mongowp.bson.netty.annotations.ModifiesIndexes;
import io.netty.buffer.ByteBuf;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

/**
 *
 */
@Immutable
public class DefaultNettyBsonLowLevelReader extends NettyBsonLowLevelReader {

  @Inject
  public DefaultNettyBsonLowLevelReader(NettyStringReader stringReader) {
    super(stringReader);
  }

  @Override
  BsonArray readArray(@Loose @ModifiesIndexes ByteBuf byteBuf) throws NettyBsonReaderException {
    int length = byteBuf.readInt();
    int significantLenght = length - 4 - 1;

    ByteBuf significantSlice = byteBuf.readSlice(significantLenght);

    byte b = byteBuf.readByte();
    assert b == 0x00;

    ArrayList<BsonValue<?>> list = new ArrayList<>();
    while (significantSlice.readableBytes() > 0) {
      list.add(readArrayEntry(significantSlice));
    }
    return new ListBsonArray(list);
  }

  @Override
  BsonBinary readBinary(@Loose @ModifiesIndexes ByteBuf byteBuf) {
    int length = byteBuf.readInt();
    byte subtype = byteBuf.readByte();
    byte[] content = new byte[length];

    byteBuf.readBytes(content);

    return new ByteArrayBsonBinary(ParsingTools.getBinarySubtype(subtype), subtype, content);
  }

  @Override
  BsonDateTime readDateTime(@Loose @ModifiesIndexes ByteBuf byteBuf) {
    return new LongBsonDateTime(byteBuf.readLong());
  }

  @Override
  BsonDbPointer readDbPointer(@Loose @ModifiesIndexes ByteBuf byteBuf) {
    String str = getStringReader().readString(byteBuf, false);

    byte[] bytes = new byte[12];
    byteBuf.readBytes(bytes);

    return new DefaultBsonDbPointer(str, new ByteArrayBsonObjectId(bytes));
  }

  @Override
  BsonValue<?> readDeprecated(@Loose @ModifiesIndexes ByteBuf byteBuf) {
    return new StringBsonDeprecated(getStringReader().readString(byteBuf, false));
  }

  @Override
  BsonDocument readDocument(@Loose @ModifiesIndexes ByteBuf byteBuf)
      throws NettyBsonReaderException {
    int length = byteBuf.readInt();
    int significantLenght = length - 4 - 1;

    ByteBuf significantSlice = byteBuf.readSlice(significantLenght);

    byte b = byteBuf.readByte();
    assert b == 0x00;

    LinkedHashMap<String, BsonValue<?>> values = new LinkedHashMap<>();
    while (significantSlice.readableBytes() > 0) {
      Entry<?> entry = readDocumentEntry(significantSlice);
      values.put(entry.getKey(), entry.getValue());
    }
    return new MapBasedBsonDocument(values);
  }

  @Override
  BsonDocument readJavaScriptScope(@Loose @ModifiesIndexes ByteBuf byteBuf)
      throws NettyBsonReaderException {
    return readDocument(byteBuf);
  }

  @Override
  BsonDouble readDouble(@Loose @ModifiesIndexes ByteBuf byteBuf) {
    return PrimitiveBsonDouble.newInstance(byteBuf.readDouble());
  }

  @Override
  BsonDecimal128 readDecimal128(@Loose @ModifiesIndexes ByteBuf byteBuf) {
    return LongsBsonDecimal128.newInstance(new BigDecimal(byteBuf.readDouble()));
  }
  
  
  @Override
  BsonBoolean readBoolean(@Loose @ModifiesIndexes ByteBuf byteBuf) throws NettyBsonReaderException {
    byte readByte = byteBuf.readByte();
    if (readByte == 0x00) {
      return FalseBsonBoolean.getInstance();
    }
    if (readByte == 0x01) {
      return TrueBsonBoolean.getInstance();
    }
    throw new NettyBsonReaderException("Unexpected boolean byte. 0x00 or "
        + "0x01 was expected, but 0x" + UnsignedBytes.toString(readByte, 16) + " was read");
  }

  @Override
  BsonInt32 readInt32(@Loose @ModifiesIndexes ByteBuf byteBuf) {
    return PrimitiveBsonInt32.newInstance(byteBuf.readInt());
  }

  @Override
  BsonInt64 readInt64(@Loose @ModifiesIndexes ByteBuf byteBuf) {
    return PrimitiveBsonInt64.newInstance(byteBuf.readLong());
  }

  @Override
  BsonJavaScript readJavaScript(@Loose @ModifiesIndexes ByteBuf byteBuf) {
    return new DefaultBsonJavaScript(getStringReader().readString(byteBuf, false));
  }

  @Override
  BsonJavaScriptWithScope readJavaScriptWithScope(@Loose @ModifiesIndexes ByteBuf byteBuf)
      throws NettyBsonReaderException {
    byteBuf.readInt();
    String js = getStringReader().readString(byteBuf, false);
    BsonDocument scope = readJavaScriptScope(byteBuf);

    return new DefaultBsonJavaScriptWithCode(js, scope);
  }

  @Override
  BsonMax readMax(@Loose @ModifiesIndexes ByteBuf byteBuf) {
    return SimpleBsonMax.getInstance();
  }

  @Override
  BsonMin readMin(@Loose @ModifiesIndexes ByteBuf byteBuf) {
    return SimpleBsonMin.getInstance();
  }

  @Override
  BsonNull readNull(@Loose @ModifiesIndexes ByteBuf byteBuf) {
    return SimpleBsonNull.getInstance();
  }

  @Override
  BsonObjectId readObjectId(@Loose @ModifiesIndexes ByteBuf byteBuf) {
    byte[] bytes = new byte[12];
    byteBuf.readBytes(bytes);

    return new ByteArrayBsonObjectId(bytes);
  }

  @Override
  BsonRegex readRegex(@Loose @ModifiesIndexes ByteBuf byteBuf) throws NettyBsonReaderException {
    String pattern = getStringReader().readCString(byteBuf, false);
    String options = getStringReader().readCString(byteBuf, true);

    return new DefaultBsonRegex(options, pattern);
  }

  @Override
  BsonString readString(@Loose @ModifiesIndexes ByteBuf byteBuf) {
    return new StringBsonString(getStringReader().readString(byteBuf, false));
  }

  @Override
  BsonTimestamp readTimestamp(@Loose @ModifiesIndexes ByteBuf byteBuf) {
    int ordinal = byteBuf.readInt();
    int seconds = byteBuf.readInt();
    return new DefaultBsonTimestamp(seconds, ordinal);
  }

  @Override
  BsonUndefined readUndefined(@Loose @ModifiesIndexes ByteBuf byteBuf) {
    return SimpleBsonUndefined.getInstance();
  }

}
