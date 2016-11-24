/*
 * MongoWP
 * Copyright © 2014 8Kdata Technology (www.8kdata.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.eightkdata.mongowp.bson.netty.pool;

import io.netty.buffer.ByteBuf;

/**
 *
 */
public class AlwaysFalseStringPool extends StringPool {

  private AlwaysFalseStringPool() {
    super(NeverStringPoolPolicy.getInstance());
  }

  @Override
  protected String retrieveFromPool(ByteBuf stringBuf) {
    return getString(stringBuf);
  }

  public static AlwaysFalseStringPool getInstance() {
    return AlwaysFalseStringPoolHolder.INSTANCE;
  }

  private static class AlwaysFalseStringPoolHolder {

    private static final AlwaysFalseStringPool INSTANCE = new AlwaysFalseStringPool();
  }

  // @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD")
  private Object readResolve() {
    return AlwaysFalseStringPool.getInstance();
  }
}
