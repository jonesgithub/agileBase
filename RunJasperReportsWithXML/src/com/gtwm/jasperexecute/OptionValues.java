package com.gtwm.jasperexecute;

//Copyright 2007 Oliver Kohll
//modified by Charly Clairmont

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.


/**
 * These enumerations used when parsing command line
 */
public class OptionValues {

	public enum OutputType {
		PDF, HTML
		//CCH20071129-01
		,JXLS
		//CCH20071208-06
		,JPG
	}

	public enum ParamType {
		STRING, BOOLEAN, DOUBLE, INTEGER
		//CCH20071237-10
		, DATE, TIME
		//CCH20071237-10
		//CCH20080313-20
		,LOCALE
		//CCH20080313-20
	}
	
	public enum DatabaseType {
		
		POSTGRESQL("org.postgresql.Driver", "jdbc:postgresql://%s:%s/%s", "5432"), 
		
		MYSQL("com.mysql.jdbc.Driver", "jdbc:mysql://%s:%s/%s", "3306"),
		
		ORACLE("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@%s:%s:%s", "1521"),
		
		DB2("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://%s:%s/%s", "50000"),
		
		MSSQLSERVER("net.sourceforge.jtds.jdbc.Driver", "jdbc:jtds:sqlserver://%s:%s/%s", "1433"),
		
		SYBASE("com.sybase.jdbc3.jdbc.SybDriver", "jdbc:sybase:Tds:%s:%s/%s", "2648"),
		
		FIREBIRD("org.firebirdsql.jdbc.FBDriver","jdbc:firebirdsql://","/")
		
		;
		DatabaseType(String driverName, String url, String defaulPort) {
			this.driverName = driverName;
			this.url = url;
			this.defaultPort = defaulPort;
		}
		
		/**
		 * Get the String that needs to be used with Class.forName() when setting up JDBC
		 * 
		 * @see http://jdbc.postgresql.org/documentation/82/load.html
		 * @see http://www.kitebird.com/articles/jdbc.html
		 */
		public String getDriverName() {
			return this.driverName;
		}
		
		/**
		 * Get the String that needs to be used with url when setting up JDBC
		 * 
		 * @see http://jdbc.postgresql.org/documentation/82/load.html
		 * @see http://www.kitebird.com/articles/jdbc.html
		 */
		public String getUrl() {
			return this.url;
		}
		
		/**
		 * Get the String that needs to be used with port when setting up JDBC
		 * 
		 * @see http://jdbc.postgresql.org/documentation/82/load.html
		 * @see http://www.kitebird.com/articles/jdbc.html
		 */
		public String getDefaultPort() {
			return this.defaultPort;
		}
		
		
		private String driverName;
		
		private String url;
		
		private String defaultPort;
	}
}
