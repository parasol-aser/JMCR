/*

   Derby - Class org.apache.derby.iapi.jdbc.BrokeredPreparedStatement30

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package org.apache.derby.iapi.jdbc;

import java.sql.*;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

/**
	JDBC 3 implementation of PreparedStatement.
*/
public class BrokeredPreparedStatement30 extends BrokeredPreparedStatement {

	private final Object generatedKeys;
	public BrokeredPreparedStatement30(BrokeredStatementControl control, String sql, Object generatedKeys) throws SQLException {
		super(control,sql);
		this.generatedKeys = generatedKeys;
	}

	public final void setURL(int i, URL x)
        throws SQLException
    {
        getPreparedStatement().setURL( i, x);
    }
    public final ParameterMetaData getParameterMetaData()
        throws SQLException
    {
        return getPreparedStatement().getParameterMetaData();
    }
	/**
		Create a duplicate PreparedStatement to this, including state, from the passed in Connection.
	*/
	public PreparedStatement createDuplicateStatement(Connection conn, PreparedStatement oldStatement) throws SQLException {

		PreparedStatement newStatement;

		if (generatedKeys == null)
			newStatement = conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
		else {
			// The prepareStatement() calls that take a generated key value do not take resultSet* type
			// parameters, but since they don't return ResultSets that is OK. There are only for INSERT statements.
			if (generatedKeys instanceof Integer)
				newStatement = conn.prepareStatement(sql, ((Integer) generatedKeys).intValue());
			else if (generatedKeys instanceof int[])
				newStatement = conn.prepareStatement(sql, (int[]) generatedKeys);
			else
				newStatement = conn.prepareStatement(sql, (String[]) generatedKeys);
		}


		setStatementState(oldStatement, newStatement);

		return newStatement;
	}

	public void setAsciiStream(int parameterIndex, InputStream x)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setAsciiStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setBinaryStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setBlob(int parameterIndex, InputStream inputStream)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setBlob(int parameterIndex, InputStream inputStream, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setCharacterStream(int parameterIndex, Reader reader,
			long length) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setClob(int parameterIndex, Reader reader) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setNCharacterStream(int parameterIndex, Reader value)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setNCharacterStream(int parameterIndex, Reader value,
			long length) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setNClob(int parameterIndex, NClob value) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setNClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setNString(int parameterIndex, String value)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setRowId(int parameterIndex, RowId x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setSQLXML(int parameterIndex, SQLXML xmlObject)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

}
