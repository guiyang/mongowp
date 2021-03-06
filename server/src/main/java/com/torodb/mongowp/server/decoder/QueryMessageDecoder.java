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
package com.torodb.mongowp.server.decoder;

import static com.torodb.mongowp.bson.BsonType.BOOLEAN;
import static com.torodb.mongowp.bson.BsonType.DOUBLE;
import static com.torodb.mongowp.bson.BsonType.INT32;
import static com.torodb.mongowp.bson.BsonType.INT64;
import static com.torodb.mongowp.bson.BsonType.NULL;
import static com.torodb.mongowp.bson.BsonType.UNDEFINED;
import static com.torodb.mongowp.bson.utils.BsonDocumentReader.AllocationType.HEAP;
import static com.torodb.mongowp.bson.utils.BsonDocumentReader.AllocationType.OFFHEAP_VALUES;

import com.torodb.mongowp.bson.BsonBoolean;
import com.torodb.mongowp.bson.BsonDocument;
import com.torodb.mongowp.bson.BsonDocument.Entry;
import com.torodb.mongowp.bson.BsonDouble;
import com.torodb.mongowp.bson.BsonInt32;
import com.torodb.mongowp.bson.BsonInt64;
import com.torodb.mongowp.bson.BsonString;
import com.torodb.mongowp.bson.BsonValue;
import com.torodb.mongowp.bson.impl.PrimitiveBsonDouble;
import com.torodb.mongowp.bson.impl.SingleEntryBsonDocument;
import com.torodb.mongowp.bson.netty.NettyBsonDocumentReader;
import com.torodb.mongowp.bson.netty.NettyStringReader;
import com.torodb.mongowp.bson.netty.annotations.Loose;
import com.torodb.mongowp.bson.netty.annotations.ModifiesIndexes;
import com.torodb.mongowp.bson.utils.BsonDocumentReaderException;
import com.torodb.mongowp.exceptions.BadValueException;
import com.torodb.mongowp.exceptions.InvalidBsonException;
import com.torodb.mongowp.exceptions.MongoException;
import com.torodb.mongowp.messages.request.QueryMessage;
import com.torodb.mongowp.messages.request.QueryMessage.Builder;
import com.torodb.mongowp.messages.request.QueryMessage.ExplainOption;
import com.torodb.mongowp.messages.request.QueryMessage.QueryOption;
import com.torodb.mongowp.messages.request.QueryMessage.QueryOptions;
import com.torodb.mongowp.messages.request.RequestBaseMessage;
import com.torodb.mongowp.server.util.EnumBitFlags;
import com.torodb.mongowp.server.util.EnumInt32FlagsUtil;
import io.netty.buffer.ByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;
import java.util.Locale;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

/**
 *
 */
@ThreadSafe
public class QueryMessageDecoder extends AbstractMessageDecoder<QueryMessage> {

  private static final Logger LOGGER = LogManager.getLogger(QueryMessageDecoder.class);

  private final NettyStringReader stringReader;
  private final NettyBsonDocumentReader docReader;

  @Inject
  public QueryMessageDecoder(NettyStringReader stringReader, NettyBsonDocumentReader docReader) {
    this.stringReader = stringReader;
    this.docReader = docReader;
  }

  @Override
  public QueryMessage decode(ByteBuf buffer, RequestBaseMessage requestBaseMessage)
      throws MongoException {

    try {
      MyBsonContext bsonContext = new MyBsonContext(buffer);

      int flags = buffer.readInt();
      String fullCollectionName = stringReader.readCString(buffer, true);
      final int numberToSkip = buffer.readInt();
      final int numberToReturn = buffer.readInt();

      //TODO: improve the way database and cache are pooled
      QueryMessage.Builder queryBuilder = new Builder(
          requestBaseMessage,
          bsonContext,
          getDatabase(fullCollectionName).intern(),
          getCollection(fullCollectionName).intern(),
          getQueryOptions(flags)
      );

      analyzeDoc(buffer, queryBuilder);

      BsonDocument returnFieldsSelector = null;
      if (buffer.readableBytes() > 0) {
        returnFieldsSelector = docReader.readDocument(HEAP, buffer);
      }

      assert buffer.readableBytes() == 0;

      queryBuilder.setReturnFieldsSelector(returnFieldsSelector)
          .setNumberToReturn(numberToReturn)
          .setNumberToSkip(numberToSkip);

      return queryBuilder.build();
    } catch (BsonDocumentReaderException ex) {
      throw new InvalidBsonException(ex);
    }
  }

  private QueryOptions getQueryOptions(int flags) {
    EnumSet<QueryOption> qoSet = EnumSet.noneOf(QueryOption.class);
    if (EnumInt32FlagsUtil.isActive(Flag.TAILABLE_CURSOR, flags)) {
      qoSet.add(QueryOption.TAILABLE_CURSOR);
    }
    if (EnumInt32FlagsUtil.isActive(Flag.SLAVE_OK, flags)) {
      qoSet.add(QueryOption.SLAVE_OK);
    }
    if (EnumInt32FlagsUtil.isActive(Flag.OPLOG_REPLAY, flags)) {
      qoSet.add(QueryOption.OPLOG_REPLAY);
    }
    if (EnumInt32FlagsUtil.isActive(Flag.NO_CURSOR_TIMEOUT, flags)) {
      qoSet.add(QueryOption.NO_CURSOR_TIMEOUT);
    }
    if (EnumInt32FlagsUtil.isActive(Flag.AWAIT_DATA, flags)) {
      qoSet.add(QueryOption.AWAIT_DATA);
    }
    if (EnumInt32FlagsUtil.isActive(Flag.EXHAUST, flags)) {
      qoSet.add(QueryOption.EXHAUST);
    }
    if (EnumInt32FlagsUtil.isActive(Flag.PARTIAL, flags)) {
      qoSet.add(QueryOption.PARTIAL);
    }

    return new QueryOptions(qoSet);
  }

  private void analyzeDoc(@Loose @ModifiesIndexes ByteBuf docByteBuf, Builder messageBuilder) throws
      BadValueException, BsonDocumentReaderException {
    BsonDocument doc = docReader.readDocument(OFFHEAP_VALUES, docByteBuf);

    if (doc.containsKey("$query")) {

      for (Entry<?> entry : doc) {
        switch (entry.getKey().toLowerCase(Locale.ENGLISH)) {
          case "$query": {
            messageBuilder.setQuery(getQuery(entry.getValue()));
            break;
          }
          case "$comment": {
            messageBuilder.setComment(getComment(entry.getValue()));
            break;
          }
          case "$explain": {
            messageBuilder.setExplainOption(getExplain(entry.getValue()));
            break;
          }
          case "$hint": {
            messageBuilder.setHint(getHint(entry.getValue()));
            break;
          }
          case "$maxScan": {
            messageBuilder.setMaxScan(getMaxScan(entry.getValue()));
            break;
          }
          case "$maxTimeMS": {
            messageBuilder.setMaxTimeMs(getMaxTimeMs(entry.getValue()));
            break;
          }
          case "$max": {
            messageBuilder.setMax(getMax(entry.getValue()));
            break;
          }
          case "$min": {
            messageBuilder.setMin(getMin(entry.getValue()));
            break;
          }
          case "$orderBy": {
            messageBuilder.setOrderBy(getOrderBy(entry.getValue()));
            break;
          }
          case "$returnKey": {
            messageBuilder.setReturnKey(getReturnKey(entry.getValue()));
            break;
          }
          case "$showDiskLoc": {
            messageBuilder.setShowDiscLoc(getShowDiskLoc(entry.getValue()));
            break;
          }
          case "$snapshot": {
            messageBuilder.setSnapshot(getSnapshot(entry.getValue()));
            break;
          }
          default: {
            LOGGER.warn("Ignored attribute/query operator '{}' "
                + "because it is not recognized", entry.getKey());
          }
        }
        assert messageBuilder.getQuery() != null;
      }
    } else {
      messageBuilder.setQuery(doc);
    }
  }

  private BsonDocument getQuery(BsonValue<?> value) throws BadValueException {
    if (!(value instanceof BsonDocument)) {
      throw new BadValueException("Unknown top level operator: $query");
    }
    return (BsonDocument) value;
  }

  @Nullable
  private String getComment(BsonValue<?> value) {
    if (value instanceof BsonString) {
      return ((BsonString) value).getValue();
    }
    return null;
  }

  @Nonnull
  private ExplainOption getExplain(BsonValue<?> value) {
    //TODO: Parse $explain
    return ExplainOption.NONE;
  }

  @Nullable
  private BsonDocument getHint(BsonValue<?> value) throws BadValueException {
    if (value instanceof BsonDocument) {
      return (BsonDocument) value;
    }
    if (value instanceof BsonString) {
      return new SingleEntryBsonDocument(((BsonString) value).getValue(), PrimitiveBsonDouble
          .newInstance(1));
    }
    throw new BadValueException("$hint must be either a string or a nested object");
  }

  private long getMaxScan(BsonValue<?> value) {
    switch (value.getType()) {
      case INT32:
        return ((BsonInt32) value).intValue();
      case INT64:
        return ((BsonInt64) value).longValue();
      case DOUBLE:
        return Math.round(((BsonDouble) value).doubleValue());
      default:
        return -1;
    }
  }

  private int getMaxTimeMs(BsonValue<?> value) {
    //TODO: Parse $maxScan
    return -1;
  }

  @Nullable
  private BsonDocument getMax(BsonValue<?> value) {
    //TODO: readDocument $max
    return null;
  }

  @Nullable
  private BsonDocument getMin(BsonValue<?> value) {
    //TODO: readDocument $min
    return null;
  }

  private boolean getReturnKey(BsonValue<?> value) {
    switch (value.getType()) {
      case BOOLEAN:
        return ((BsonBoolean) value).getPrimitiveValue();
      case UNDEFINED:
      case NULL:
        return false;
      default:
        return true;
    }
  }

  @Nullable
  private BsonDocument getOrderBy(BsonValue<?> value) {
    //TODO: readDocument $orderBy
    return null;
  }

  private boolean getSnapshot(BsonValue<?> value) {
    switch (value.getType()) {
      case BOOLEAN:
        return ((BsonBoolean) value).getPrimitiveValue();
      case UNDEFINED:
      case NULL:
        return false;
      default:
        return true;
    }
  }

  private boolean getShowDiskLoc(BsonValue<?> value) {
    switch (value.getType()) {
      case BOOLEAN:
        return ((BsonBoolean) value).getPrimitiveValue();
      case UNDEFINED:
      case NULL:
        return false;
      default:
        return true;
    }
  }

  private enum Flag implements EnumBitFlags {
    TAILABLE_CURSOR(1),
    SLAVE_OK(2),
    OPLOG_REPLAY(3),
    NO_CURSOR_TIMEOUT(4),
    AWAIT_DATA(5),
    EXHAUST(6),
    PARTIAL(7);

    @Nonnegative
    private final int flagBitPosition;

    private Flag(@Nonnegative int flagBitPosition) {
      this.flagBitPosition = flagBitPosition;
    }

    @Override
    public int getFlagBitPosition() {
      return flagBitPosition;
    }
  }
}
