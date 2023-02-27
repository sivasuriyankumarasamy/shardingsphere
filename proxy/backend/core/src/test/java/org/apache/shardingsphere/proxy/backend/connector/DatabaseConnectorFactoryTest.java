/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.proxy.backend.connector;

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public final class DatabaseConnectorFactoryTest {
    
    @Test
    public void assertNewDatabaseConnectorWithoutParameter() {
        BackendConnection backendConnection = mock(BackendConnection.class, RETURNS_DEEP_STUBS);
        when(backendConnection.getConnectionSession().getDatabaseName()).thenReturn("db");
        SQLStatementContext<?> sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.emptyList());
        QueryContext queryContext = new QueryContext(sqlStatementContext, "schemaName", Collections.emptyList());
        ShardingSphereDatabase database = mockDatabase();
        ContextManager contextManager = mockContextManager(database);
        try (MockedStatic<ProxyContext> proxyContext = mockStatic(ProxyContext.class, RETURNS_DEEP_STUBS)) {
            proxyContext.when(() -> ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
            proxyContext.when(() -> ProxyContext.getInstance().getDatabase("db")).thenReturn(database);
            DatabaseConnector engine = DatabaseConnectorFactory.getInstance().newInstance(queryContext, backendConnection, false);
            assertThat(engine, instanceOf(DatabaseConnector.class));
        }
    }
    
    @Test
    public void assertNewDatabaseConnectorWithParameters() {
        BackendConnection backendConnection = mock(BackendConnection.class, RETURNS_DEEP_STUBS);
        when(backendConnection.getConnectionSession().getDatabaseName()).thenReturn("db");
        SQLStatementContext<?> sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.emptyList());
        ShardingSphereDatabase database = mockDatabase();
        ContextManager contextManager = mockContextManager(database);
        try (MockedStatic<ProxyContext> proxyContext = mockStatic(ProxyContext.class, RETURNS_DEEP_STUBS)) {
            proxyContext.when(() -> ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
            proxyContext.when(() -> ProxyContext.getInstance().getDatabase("db")).thenReturn(database);
            assertThat(DatabaseConnectorFactory.getInstance().newInstance(new QueryContext(sqlStatementContext, "schemaName", Collections.emptyList()), backendConnection, false),
                    instanceOf(DatabaseConnector.class));
        }
    }
    
    private ContextManager mockContextManager(final ShardingSphereDatabase database) {
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(Collections.singletonMap("db", database), mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(new Properties())));
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    private static ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.containsDataSource()).thenReturn(true);
        when(result.isComplete()).thenReturn(true);
        when(result.getProtocolType()).thenReturn(new H2DatabaseType());
        when(result.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        return result;
    }
}