package com.progbits.web;

import javax.sql.DataSource;

/**
 * Defines the SQL to use during Login attempts.
 * 
 * 
 * @author scarr
 */
public class LoginOptions {
	private String loginIdSql = "SELECT loginId FROM users WHERE userName=? AND password=? ";
	private String fullNameSql = "SELECT fullName FROM users WHERE userName=? AND password=? ";
	private String rolesSql = "SELECT roleName FROM roles WHERE loginId=?";
	
	private DataSource ds;

	public DataSource getDatasource() {
		return ds;
	}

	public void setDatasource(DataSource ds) {
		this.ds = ds;
	}

	public String getLoginIdSql() {
		return loginIdSql;
	}

	public void setLoginIdSql(String loginIdSql) {
		this.loginIdSql = loginIdSql;
	}

	public String getFullNameSql() {
		return fullNameSql;
	}

	public void setFullNameSql(String fullNameSql) {
		this.fullNameSql = fullNameSql;
	}

	public String getRolesSql() {
		return rolesSql;
	}

	public void setRolesSql(String rolesSql) {
		this.rolesSql = rolesSql;
	}
	
}
