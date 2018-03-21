/*

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.apache.derby.iapi.services.cache;
import java.util.Hashtable;
class ClassSizeCatalog extends java.util.Hashtable
{
    ClassSizeCatalog()
    {
        put( "org.apache.derby.iapi.types.SQLBit", new int[]{4,4});
        put( "org.apache.derby.iapi.types.SQLBlob", new int[]{4,4});
        put( "java.util.Vector", new int[]{12,3});
        put( "org.apache.derby.iapi.types.SQLLongvarchar", new int[]{4,8});
        put( "org.apache.derby.iapi.types.SQLLongVarbit", new int[]{4,4});
        put( "org.apache.derby.impl.store.access.heap.HeapRowLocation", new int[]{12,3});
        put( "java.util.ArrayList", new int[]{8,3});
        put( "org.apache.derby.iapi.types.SQLTimestamp", new int[]{12,3});
        put( "org.apache.derby.impl.store.raw.data.RecordId", new int[]{8,3});
        put( "org.apache.derby.iapi.types.UserType", new int[]{0,3});
        put( "org.apache.derby.iapi.types.CollatorSQLLongvarchar", new int[]{4,9});
        put( "org.apache.derby.iapi.types.DataType", new int[]{0,2});
        put( "org.apache.derby.iapi.types.SQLInteger", new int[]{8,2});
        put( "org.apache.derby.impl.store.access.btree.index.B2I", new int[]{32,6});
        put( "org.apache.derby.iapi.types.BinaryDecimal", new int[]{4,3});
        put( "org.apache.derby.impl.store.access.btree.BTree", new int[]{20,6});
        put( "org.apache.derby.iapi.types.SQLChar", new int[]{4,8});
        put( "org.apache.derby.iapi.types.SQLTinyint", new int[]{5,2});
        put( "org.apache.derby.iapi.types.CollatorSQLChar", new int[]{4,9});
        put( "org.apache.derby.iapi.types.SQLTime", new int[]{8,3});
        put( "org.apache.derby.iapi.types.SQLClob", new int[]{4,8});
        put( "org.apache.derby.iapi.types.SQLBinary", new int[]{4,4});
        put( "org.apache.derby.iapi.types.SQLVarchar", new int[]{4,8});
        put( "org.apache.derby.iapi.types.SQLVarbit", new int[]{4,4});
        put( "org.apache.derby.iapi.types.SQLDate", new int[]{4,3});
        put( "org.apache.derby.impl.store.access.StorableFormatId", new int[]{4,2});
        put( "org.apache.derby.iapi.types.NumberDataType", new int[]{0,2});
        put( "org.apache.derby.iapi.types.CollatorSQLClob", new int[]{4,9});
        put( "org.apache.derby.iapi.types.XML", new int[]{8,4});
        put( "org.apache.derby.impl.store.access.conglomerate.GenericConglomerate", new int[]{0,2});
        put( "org.apache.derby.iapi.types.SQLDecimal", new int[]{4,4});
        put( "org.apache.derby.iapi.types.SQLBoolean", new int[]{12,2});
        put( "org.apache.derby.iapi.types.CollatorSQLVarchar", new int[]{4,9});
        put( "org.apache.derby.iapi.types.SQLRef", new int[]{0,3});
        put( "org.apache.derby.iapi.types.SQLDouble", new int[]{12,2});
        put( "java.lang.ref.WeakReference", new int[]{0,6});
        put( "org.apache.derby.impl.store.access.heap.Heap", new int[]{4,5});
        put( "java.math.BigDecimal", new int[]{4,3});
        put( "org.apache.derby.impl.store.access.btree.index.B2I_v10_2", new int[]{32,6});
        put( "org.apache.derby.iapi.types.SQLSmallint", new int[]{8,2});
        put( "org.apache.derby.impl.store.access.UTF", new int[]{0,3});
        put( "java.util.GregorianCalendar", new int[]{60,6});
        put( "org.apache.derby.iapi.store.raw.ContainerKey", new int[]{16,2});
        put( "org.apache.derby.iapi.types.SQLReal", new int[]{8,2});
        put( "org.apache.derby.iapi.types.BigIntegerDecimal", new int[]{4,3});
        put( "org.apache.derby.impl.services.cache.CachedItem", new int[]{24,3});
        put( "org.apache.derby.impl.store.access.heap.Heap_v10_2", new int[]{4,5});
        put( "org.apache.derby.iapi.types.SQLLongint", new int[]{12,2});
    }
}
