/*
 *  Copyright 2009 GT webMarque Ltd
 * 
 *  This file is part of GT portalBase.
 *
 *  GT portalBase is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  GT portalBase is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with GT portalBase.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gtwm.pb.auth;

import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.AuthenticatorInfo;
import com.gtwm.pb.model.interfaces.AuthManagerInfo;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.AppRoleInfo;
import com.gtwm.pb.model.interfaces.RoleObjectPrivilegeInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.UserGeneralPrivilegeInfo;
import com.gtwm.pb.model.interfaces.RoleGeneralPrivilegeInfo;
import com.gtwm.pb.model.interfaces.UserObjectPrivilegeInfo;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.HibernateUtil;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.RandomString;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import org.grlea.log.SimpleLogger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.HibernateException;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.TreeSet;

/**
 * Manages the application's Authenticator object - use the static methods in
 * this class to add user, roles and privileges. We are basically separating out
 * persistence code so that the Authenticator doesn't have to know about it
 */
public class AuthManager implements AuthManagerInfo {

	/**
	 * Make the application ready for using authentication by loading the
	 * authentication object from the object database or creating a new one if
	 * it doesn't exist, populated with a default user
	 * 
	 * @throws ObjectNotFoundException
	 *             If there was an internal error populating a new
	 *             authentication object
	 * @throws SQLException
	 *             If there was an error bootstrapping the relational database
	 *             authentication tables
	 * 
	 *             Note: No exception handling (finally block) is done here,
	 *             that's in the DatabaseDefn constructor that calls this
	 */
	public AuthManager(DataSource relationalDataSource) throws ObjectNotFoundException,
			CantDoThatException, MissingParametersException {
		try {
			Session hibernateSession = HibernateUtil.currentSession();
			Transaction hibernateTransaction = hibernateSession.beginTransaction();
			this.authenticator = (AuthenticatorInfo) hibernateSession.createQuery(
					"from Authenticator").uniqueResult();
			if (this.authenticator != null) {
				for (CompanyInfo company : ((Authenticator) authenticator).getCompanies()) {
					logger.info("" + company + " roles: " + company.getRoles());
					logger.info("" + company + " users: " + company.getUsers());
					logger.info("" + company + " modules: " + company.getModules());
					logger.info("" + company + " tabs: " + company.getTabAddresses());
				}
				logger.info("Users: " + ((Authenticator) authenticator).getUsers());
				for (AppRoleInfo role : ((Authenticator) authenticator).getRoles()) {
					logger.info("Role " + role + " users: " + role.getUsers());
				}
				logger.info("User privileges: "
						+ ((Authenticator) authenticator).getUserPrivileges());
				logger.info("Role privileges: "
						+ ((Authenticator) authenticator).getRolePrivileges());
			}
			if (this.authenticator == null) {
				try {
					// Now the object database work
					this.authenticator = new Authenticator();
					hibernateSession.save(this.authenticator);
					String masterPassword = (new RandomString()).toString();
					String masterUsername = "master";
					CompanyInfo masterCompany = new Company("Master Company");
					((Authenticator) this.authenticator).addCompany(masterCompany);
					AppUserInfo masterUser = new AppUser(masterCompany, null, masterUsername,
							"Master", "User", masterPassword);
					((Authenticator) this.authenticator).addUser(masterUser);
					((Authenticator) this.authenticator).addUserPrivilege(masterUser,
							PrivilegeType.MASTER);
					hibernateTransaction.commit();
					logger.info("Created master company + user");
				} catch (HibernateException hex) {
					hibernateTransaction.rollback();
					throw hex;
				}
			}
		} catch (RuntimeException rtex) {
			throw rtex;
		}
	}

	public AuthenticatorInfo getAuthenticator() {
		return this.authenticator;
	}

	public CompanyInfo getCompanyForLoggedInUser(HttpServletRequest request)
			throws ObjectNotFoundException {
		AppUserInfo loggedInUser = ((Authenticator) this.authenticator).getUserByUserName(request
				.getRemoteUser());
		return loggedInUser.getCompany();
	}

	/*
	 * Security note: anyone can see the display names of tables belonging to
	 * their company using this method, special privileges aren't required
	 */
	public synchronized SortedSet<String> getCompanyTableNames(HttpServletRequest request)
			throws ObjectNotFoundException {
		SortedSet<String> companyTableNames = new TreeSet<String>();
		CompanyInfo company = this.getCompanyForLoggedInUser(request);
		for (AppUserInfo user : company.getUsers()) {
			Set<UserGeneralPrivilegeInfo> userPrivileges = ((Authenticator) this.authenticator)
					.getPrivilegesForUser(user);
			for (UserGeneralPrivilegeInfo userPrivilege : userPrivileges) {
				if (userPrivilege instanceof UserObjectPrivilegeInfo) {
					companyTableNames.add(((UserObjectPrivilegeInfo) userPrivilege).getTable()
							.getTableName());
				}
			}
		}
		for (AppRoleInfo role : company.getRoles()) {
			Set<RoleGeneralPrivilegeInfo> rolePrivileges = ((Authenticator) this.authenticator)
					.getPrivilegesForRole(role);
			for (RoleGeneralPrivilegeInfo rolePrivilege : rolePrivileges) {
				if (rolePrivilege instanceof RoleObjectPrivilegeInfo) {
					companyTableNames.add(((RoleObjectPrivilegeInfo) rolePrivilege).getTable()
							.getTableName());
				}
			}
		}
		return companyTableNames;
	}

	public synchronized Set<TableInfo> getCompanyTables(HttpServletRequest request)
			throws ObjectNotFoundException, DisallowedException {
		CompanyInfo company = this.getCompanyForLoggedInUser(request);
		return getCompanyTables(request, company);
	}

	public Set<TableInfo> getCompanyTables(HttpServletRequest request, CompanyInfo company)
			throws DisallowedException {
		// these tables should only be returned for a company administrator
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE) || this.authenticator
				.loggedInUserAllowedTo(request, PrivilegeType.MASTER))) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		return this.getCompanyTablesWithoutChecks(company);
	}

	private Set<TableInfo> getCompanyTablesWithoutChecks(CompanyInfo company) {
		Set<TableInfo> companyTables = new TreeSet<TableInfo>();
		for (AppUserInfo user : company.getUsers()) {
			Set<UserGeneralPrivilegeInfo> userPrivileges = ((Authenticator) this.authenticator)
					.getPrivilegesForUser(user);
			for (UserGeneralPrivilegeInfo userPrivilege : userPrivileges) {
				if (userPrivilege instanceof UserObjectPrivilegeInfo) {
					companyTables.add(((UserObjectPrivilegeInfo) userPrivilege).getTable());
				}
			}
		}
		for (AppRoleInfo role : company.getRoles()) {
			Set<RoleGeneralPrivilegeInfo> rolePrivileges = ((Authenticator) this.authenticator)
					.getPrivilegesForRole(role);
			for (RoleGeneralPrivilegeInfo rolePrivilege : rolePrivileges) {
				if (rolePrivilege instanceof RoleObjectPrivilegeInfo) {
					companyTables.add(((RoleObjectPrivilegeInfo) rolePrivilege).getTable());
				}
			}
		}
		return companyTables;
	}

	private synchronized void addToTableCompanyCache(TableInfo table, CompanyInfo company) {
		this.tableCompaniesCache.put(table, company);
	}

	public boolean tableBelongsToCompany(CompanyInfo company, TableInfo table)
			throws ObjectNotFoundException {
		CompanyInfo cachedTableCompany = this.tableCompaniesCache.get(table);
		if (cachedTableCompany == null) {
			// find out what company the table belongs to
			for (CompanyInfo testCompany : ((Authenticator) this.authenticator).getCompanies()) {
				Set<TableInfo> companyTables = this.getCompanyTablesWithoutChecks(testCompany);
				if (companyTables.contains(table)) {
					this.addToTableCompanyCache(table, testCompany);
					if (company.equals(testCompany)) {
						return true;
					} else {
						return false;
					}
				}
			}
			throw new ObjectNotFoundException("Table " + table + " was not found in any company!");
		}
		if (cachedTableCompany.equals(company)) {
			return true;
		}
		return false;
	}

	public SortedSet<CompanyInfo> getCompanies(HttpServletRequest request)
			throws DisallowedException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.MASTER))) {
			throw new DisallowedException(PrivilegeType.MASTER);
		}
		return ((Authenticator) this.authenticator).getCompanies();
	}

	public synchronized void addCompany(HttpServletRequest request, CompanyInfo company)
			throws DisallowedException, CantDoThatException, CodingErrorException,
			MissingParametersException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.MASTER))) {
			throw new DisallowedException(PrivilegeType.MASTER);
		}
		HibernateUtil.activateObject(this.authenticator);
		((Authenticator) this.authenticator).addCompany(company);
		// now add an initial user to the company so it can be logged in to
		String adminUsername = "admin" + company.getCompanyName().toLowerCase();
		adminUsername = adminUsername.replaceAll("\\W", "");
		String adminPassword = (new RandomString()).toString();
		AppUserInfo adminUser = new AppUser(company, null, adminUsername, "User", "Admin",
				adminPassword);
		String adminRolename = adminUsername;
		AppRoleInfo adminRole = new AppRole(company, null, adminRolename);
		// no mapping from role to authenticator so we have to explicitly save
		// it
		HibernateUtil.currentSession().save(adminRole);
		try {
			this.addUser(request, adminUser);
			this.addRole(request, adminRole);
			this.assignUserToRole(request, adminUser, adminRole);
			((Authenticator) this.authenticator).addRolePrivilege(adminRole,
					PrivilegeType.ADMINISTRATE);
		} catch (MissingParametersException mpex) {
			throw new CodingErrorException("Error constructing/accessing user object", mpex);
		} catch (ObjectNotFoundException onfex) {
			throw new CodingErrorException("Error assigning user to role: user or role not found",
					onfex);
		}
	}

	public synchronized void removeCompany(HttpServletRequest request, CompanyInfo company)
			throws DisallowedException, CodingErrorException, CantDoThatException,
			ObjectNotFoundException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.MASTER))) {
			throw new DisallowedException(PrivilegeType.MASTER);
		}
		Set<TableInfo> companyTables = this.getCompanyTables(request, company);
		if (companyTables.size() > 0) {
			throw new CantDoThatException(
					"All tables must be removed before removing the company. Remaining tables are "
							+ companyTables);
		}
		logger.info("removing users & roles");
		try {
			for (AppUserInfo user : company.getUsers()) {
				logger.info("removing " + user);
				this.removeUser(request, user);
			}
			for (AppRoleInfo role : company.getRoles()) {
				logger.info("removing " + role);
				this.removeRole(request, role);
			}
		} catch (ObjectNotFoundException onfex) {
			throw new CodingErrorException("User or role in company " + company
					+ " not found in authenticator");
		}
		logger.info("removing company " + company);
		HibernateUtil.activateObject(this.authenticator);
		((Authenticator) this.authenticator).removeCompany(company);
		HibernateUtil.currentSession().delete(company);
	}

	public CompanyInfo getCompanyByInternalName(HttpServletRequest request,
			String internalCompanyName) throws DisallowedException, ObjectNotFoundException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.MASTER))) {
			throw new DisallowedException(PrivilegeType.MASTER);
		}
		for (CompanyInfo company : this.getCompanies(request)) {
			if (company.getInternalCompanyName().equals(internalCompanyName)) {
				return company;
			}
		}
		throw new ObjectNotFoundException("Company with ID '" + internalCompanyName + "' not found");
	}

	public synchronized void addUser(HttpServletRequest request, AppUserInfo newUser)
			throws DisallowedException, MissingParametersException, ObjectNotFoundException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE) || this.authenticator
				.loggedInUserAllowedTo(request, PrivilegeType.MASTER))) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		// only MASTER user is allowed to add a user to a different company
		if (!this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.MASTER)) {
			CompanyInfo companyLoggedInTo = this.getCompanyForLoggedInUser(request);
			CompanyInfo companyUserBelongsTo = newUser.getCompany();
			if (!companyUserBelongsTo.equals(companyLoggedInTo)) {
				throw new DisallowedException(PrivilegeType.MASTER);
			}
		}
		// Add user in memory by cascading addUser down to the actual
		// authenticator
		HibernateUtil.activateObject(this.authenticator);
		((Authenticator) this.authenticator).addUser(newUser);
	}

	public synchronized void updateUser(HttpServletRequest request, AppUserInfo appUser,
			String userName, String surname, String forename, String password)
			throws DisallowedException, MissingParametersException, CantDoThatException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		// check which fields are to be updated:
		// 1) treat zero-length strings as a deletion request,
		// 2) nulls indicate that the field value is to remain unchanged
		if (userName == null) {
			userName = appUser.getUserName();
		} else {
			if (userName.trim().equals("")) {
				throw new MissingParametersException("Username cannot be empty");
			}
			if (appUser.getUserName().equals(request.getRemoteUser())
					&& (!userName.equals(request.getRemoteUser()))) {
				throw new CantDoThatException("Unable to edit own account username");
			}
			Set<AppUserInfo> allUsers = ((Authenticator) this.authenticator).getUsers();
			// check that the new username isn't already in use by someone else
			for (AppUserInfo testUser : allUsers) {
				if (testUser.getUserName().equals(userName) && (!testUser.equals(appUser))) {
					throw new CantDoThatException("Username " + userName + " already exists");
				}
			}
		}
		if (password == null) {
			password = appUser.getPassword();
		} else {
			if (password.trim().equals("")) {
				throw new MissingParametersException("Password cannot be empty");
			}
		}
		if (surname == null)
			surname = appUser.getSurname();
		if (forename == null)
			forename = appUser.getForename();
		// Update user in memory:
		HibernateUtil.activateObject(appUser);
		((Authenticator) this.authenticator).updateUser(appUser, userName, surname, forename,
				password);
	}

	public synchronized void removeUser(HttpServletRequest request, AppUserInfo appUser)
			throws DisallowedException, ObjectNotFoundException, CodingErrorException,
			CantDoThatException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE) || this.authenticator
				.loggedInUserAllowedTo(request, PrivilegeType.MASTER))) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		if (appUser.getUserName().equals(request.getRemoteUser())) {
			throw new CantDoThatException("Unable to edit own account");
		}
		logger.info("removing user " + appUser);
		HibernateUtil.activateObject(this.authenticator);
		((Authenticator) this.authenticator).removeUser(appUser);
		HibernateUtil.currentSession().delete(appUser);
	}

	public synchronized AppUserInfo getUserByInternalName(HttpServletRequest request,
			String internalUserName) throws ObjectNotFoundException, DisallowedException {
		AppUserInfo foundUser = null;
		for (AppUserInfo user : ((Authenticator) this.authenticator).getUsers()) {
			if (user.getInternalUserName().equals(internalUserName)) {
				foundUser = user;
				break; // user found so no need to continue iterating
			}
		}
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			if (foundUser == null) {
				throw new DisallowedException(PrivilegeType.ADMINISTRATE);
			}
			if (!foundUser.getUserName().equals(request.getRemoteUser())) {
				throw new DisallowedException(PrivilegeType.ADMINISTRATE);
			}
		}
		if (foundUser == null) {
			throw new ObjectNotFoundException("User with internal name " + internalUserName
					+ " not found");
		} else {
			return foundUser;
		}
	}

	public synchronized void addRole(HttpServletRequest request, AppRoleInfo role)
			throws DisallowedException, MissingParametersException, ObjectNotFoundException,
			CodingErrorException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE) || this.authenticator
				.loggedInUserAllowedTo(request, PrivilegeType.MASTER))) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		// only MASTER user is allowed to add a user to a different company
		if (!this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.MASTER)) {
			CompanyInfo companyLoggedInTo = this.getCompanyForLoggedInUser(request);
			CompanyInfo companyRoleBelongsTo = role.getCompany();
			if (!companyRoleBelongsTo.equals(companyLoggedInTo)) {
				throw new DisallowedException(PrivilegeType.MASTER);
			}
		}
		HibernateUtil.activateObject(this.authenticator);
		((Authenticator) this.authenticator).addRole(role);
	}

	public synchronized void updateRole(HttpServletRequest request, AppRoleInfo role,
			String newRoleName) throws DisallowedException, MissingParametersException,
			ObjectNotFoundException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		if (role == null) {
			throw new MissingParametersException("oldRole field not specified");
		}
		if (role.getRoleName().equals("")) {
			throw new MissingParametersException("oldRole field blank");
		}
		if (newRoleName == null) {
			throw new MissingParametersException("newRole field not specified");
		}
		if (newRoleName.equals("")) {
			throw new MissingParametersException("newRole field blank");
		}
		HibernateUtil.activateObject(role);
		((Authenticator) this.authenticator).updateRole(role, newRoleName);
	}

	public synchronized void removeRole(HttpServletRequest request, AppRoleInfo role)
			throws DisallowedException, ObjectNotFoundException, CodingErrorException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE) || this.authenticator
				.loggedInUserAllowedTo(request, PrivilegeType.MASTER))) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		logger.info("removing role " + role);
		HibernateUtil.activateObject(this.authenticator);
		((Authenticator) this.authenticator).removeRole(role);
		HibernateUtil.currentSession().delete(role);
	}

	public synchronized AppRoleInfo getRoleByInternalName(String internalRoleName) {
		for (AppRoleInfo role : ((Authenticator) this.authenticator).getRoles()) {
			if (role.getInternalRoleName().equals(internalRoleName)) {
				return role;
			}
		}
		return null;
	}

	public synchronized void addRolePrivilege(HttpServletRequest request, AppRoleInfo role,
			PrivilegeType privilegeType) throws DisallowedException, CantDoThatException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		HibernateUtil.activateObject(this.authenticator);
		((Authenticator) this.authenticator).addRolePrivilege(role, privilegeType);
	}

	public synchronized void addRolePrivilege(HttpServletRequest request, AppRoleInfo role,
			PrivilegeType privilegeType, TableInfo table) throws IllegalArgumentException,
			DisallowedException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		HibernateUtil.activateObject(this.authenticator);
		((Authenticator) this.authenticator).addRolePrivilege(role, privilegeType, table);
	}

	public synchronized void removeRolePrivilege(HttpServletRequest request, AppRoleInfo role,
			PrivilegeType privilegeType) throws DisallowedException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		HibernateUtil.activateObject(this.authenticator);
		RoleGeneralPrivilegeInfo removedPrivilege = ((Authenticator) this.authenticator)
				.removeRolePrivilege(role, privilegeType);
		HibernateUtil.currentSession().delete(removedPrivilege);
	}

	public synchronized void removeRolePrivilege(HttpServletRequest request, AppRoleInfo role,
			PrivilegeType privilegeType, TableInfo table) throws IllegalArgumentException,
			DisallowedException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		HibernateUtil.activateObject(this.authenticator);
		RoleObjectPrivilegeInfo removedPrivilege = ((Authenticator) this.authenticator)
				.removeRolePrivilege(role, privilegeType, table);
		HibernateUtil.currentSession().delete(removedPrivilege);
	}

	public synchronized void addUserPrivilege(HttpServletRequest request, AppUserInfo appUser,
			PrivilegeType privilegeType) throws DisallowedException, CantDoThatException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		if (this.specifiedUserHasPrivilege(request, PrivilegeType.MASTER, appUser)) {
			throw new CantDoThatException("User '" + appUser
					+ "' has MASTER privileges and as such can't have any other privileges");
		}
		HibernateUtil.activateObject(this.authenticator);
		((Authenticator) this.authenticator).addUserPrivilege(appUser, privilegeType);
	}

	public synchronized void addUserPrivilege(HttpServletRequest request, AppUserInfo appUser,
			PrivilegeType privilegeType, TableInfo table) throws IllegalArgumentException,
			DisallowedException, CantDoThatException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		if (this.specifiedUserHasPrivilege(request, PrivilegeType.MASTER, appUser)) {
			throw new CantDoThatException("User '" + appUser
					+ "' has MASTER privileges and as such can't have any other privileges");
		}
		HibernateUtil.activateObject(this.authenticator);
		((Authenticator) this.authenticator).addUserPrivilege(appUser, privilegeType, table);
	}

	public synchronized void removeUserPrivilege(HttpServletRequest request, AppUserInfo appUser,
			PrivilegeType privilegeType) throws DisallowedException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		HibernateUtil.activateObject(this.authenticator);
		UserGeneralPrivilegeInfo removedPrivilege = ((Authenticator) this.authenticator)
				.removeUserPrivilege(appUser, privilegeType);
		HibernateUtil.currentSession().delete(removedPrivilege);
	}

	public synchronized void removeUserPrivilege(HttpServletRequest request, AppUserInfo appUser,
			PrivilegeType privilegeType, TableInfo table) throws IllegalArgumentException,
			DisallowedException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		HibernateUtil.activateObject(this.authenticator);
		UserObjectPrivilegeInfo removedPrivilege = ((Authenticator) this.authenticator)
				.removeUserPrivilege(appUser, privilegeType, table);
		HibernateUtil.currentSession().delete(removedPrivilege);
	}

	public Set<RoleObjectPrivilegeInfo> getRolePrivilegesOnTable(HttpServletRequest request,
			TableInfo table) throws DisallowedException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE, table);
		}
		Set<RoleObjectPrivilegeInfo> rolePrivilegesOnTable = new HashSet<RoleObjectPrivilegeInfo>();
		Set<RoleGeneralPrivilegeInfo> rolePrivileges = ((Authenticator) this.authenticator)
				.getRolePrivileges();
		for (RoleGeneralPrivilegeInfo rolePrivilege : rolePrivileges) {
			if (rolePrivilege instanceof RoleObjectPrivilege) {
				RoleObjectPrivilege roleObjectPrivilege = (RoleObjectPrivilege) rolePrivilege;
				if (roleObjectPrivilege.getTable().equals(table)) {
					rolePrivilegesOnTable.add(roleObjectPrivilege);
				}
			}
		}
		return rolePrivilegesOnTable;
	}

	public Set<UserObjectPrivilegeInfo> getUserPrivilegesOnTable(HttpServletRequest request,
			TableInfo table) throws DisallowedException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE, table);
		}
		Set<UserObjectPrivilegeInfo> userPrivilegesOnTable = new HashSet<UserObjectPrivilegeInfo>();
		Set<UserGeneralPrivilegeInfo> userPrivileges = ((Authenticator) this.authenticator)
				.getUserPrivileges();
		for (UserGeneralPrivilegeInfo userPrivilege : userPrivileges) {
			if (userPrivilege instanceof UserObjectPrivilege) {
				UserObjectPrivilege userObjectPrivilege = (UserObjectPrivilege) userPrivilege;
				if (userObjectPrivilege.getTable().equals(table)) {
					userPrivilegesOnTable.add(userObjectPrivilege);
				}
			}
		}
		return userPrivilegesOnTable;
	}

	public void removePrivilegesOnTable(HttpServletRequest request, TableInfo table)
			throws DisallowedException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.MANAGE_TABLE, table))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, table);
		}
		HibernateUtil.activateObject(this.authenticator);
		Set<UserGeneralPrivilegeInfo> allUserPrivileges = ((Authenticator) this.authenticator)
				.getUserPrivileges();
		for (UserGeneralPrivilegeInfo userPrivilege : allUserPrivileges) {
			if (userPrivilege instanceof UserObjectPrivilegeInfo) {
				if (((UserObjectPrivilegeInfo) userPrivilege).getTable().equals(table)) {
					AppUserInfo user = userPrivilege.getUser();
					PrivilegeType privilegeType = userPrivilege.getPrivilegeType();
					((Authenticator) this.authenticator).removeUserPrivilege(user, privilegeType,
							table);
					HibernateUtil.currentSession().delete(userPrivilege);
				}
			}
		}
		// The same thing with role privileges
		Set<RoleGeneralPrivilegeInfo> allRolePrivileges = ((Authenticator) this.authenticator)
				.getRolePrivileges();
		for (RoleGeneralPrivilegeInfo rolePrivilege : allRolePrivileges) {
			if (rolePrivilege instanceof RoleObjectPrivilegeInfo) {
				if (((RoleObjectPrivilegeInfo) rolePrivilege).getTable().equals(table)) {
					AppRoleInfo role = rolePrivilege.getRole();
					PrivilegeType privilegeType = rolePrivilege.getPrivilegeType();
					((Authenticator) this.authenticator).removeRolePrivilege(role, privilegeType,
							table);
					HibernateUtil.currentSession().delete(rolePrivilege);
				}
			}
		}
	}

	public synchronized void assignUserToRole(HttpServletRequest request, AppUserInfo user,
			AppRoleInfo role) throws DisallowedException, MissingParametersException,
			ObjectNotFoundException, CantDoThatException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE) || this.authenticator
				.loggedInUserAllowedTo(request, PrivilegeType.MASTER))) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		if (this.specifiedUserHasPrivilege(request, PrivilegeType.MASTER, user)) {
			throw new CantDoThatException("User '" + user
					+ "' has MASTER privileges and therefore can't be a member of any roles");
		}
		HibernateUtil.activateObject(role);
		((Authenticator) this.authenticator).destroyCache();
		role.assignUser(user);
	}

	public synchronized void removeUserFromRole(HttpServletRequest request, AppUserInfo user,
			AppRoleInfo role) throws DisallowedException, MissingParametersException,
			ObjectNotFoundException, CodingErrorException, CantDoThatException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		boolean roleIsCompanyAdmin = false;
		for (RoleGeneralPrivilegeInfo roleGeneralPrivilege : this.getPrivilegesForRole(request,
				role)) {
			if (roleGeneralPrivilege.getPrivilegeType().equals(PrivilegeType.ADMINISTRATE)) {
				roleIsCompanyAdmin = true;
			}
		}
		if (request.getRemoteUser().equals(user.getUserName()) && roleIsCompanyAdmin) {
			throw new CantDoThatException("Unable to remove admin role from own account");
		}
		HibernateUtil.activateObject(role);
		((Authenticator) this.authenticator).destroyCache();
		role.removeUser(user);
	}

	public AppUserInfo getUserByUserName(HttpServletRequest request, String userName)
			throws ObjectNotFoundException, DisallowedException {
		if (this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE)
				|| userName.equals(request.getRemoteUser())) {
			return ((Authenticator) this.authenticator).getUserByUserName(userName);
		} else {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
	}

	public SortedSet<AppUserInfo> getUsers(HttpServletRequest request) throws DisallowedException,
			ObjectNotFoundException {
		if (this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE)) {
			CompanyInfo company = this.getCompanyForLoggedInUser(request);
			return company.getUsers();
		} else {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
	}

	public SortedSet<AppRoleInfo> getRoles(HttpServletRequest request) throws DisallowedException,
			ObjectNotFoundException {
		if (this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE)) {
			CompanyInfo company = this.getCompanyForLoggedInUser(request);
			return company.getRoles();
		} else {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
	}

	public SortedSet<AppRoleInfo> getRolesForUser(HttpServletRequest request, AppUserInfo user)
			throws DisallowedException {
		if (this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE)) {
			return ((Authenticator) this.authenticator).getRolesForUser(user);
		} else {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
	}

	public Set<UserGeneralPrivilegeInfo> getPrivilegesForUser(HttpServletRequest request,
			AppUserInfo user) throws DisallowedException {
		if (this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE)
				|| this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.MASTER)) {
			return ((Authenticator) this.authenticator).getPrivilegesForUser(user);
		} else {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
	}

	public Set<RoleGeneralPrivilegeInfo> getPrivilegesForRole(HttpServletRequest request,
			AppRoleInfo role) throws DisallowedException {
		if (this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE)) {
			return ((Authenticator) this.authenticator).getPrivilegesForRole(role);
		} else {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
	}

	public EnumSet<PrivilegeType> getPrivilegeTypes(HttpServletRequest request)
			throws DisallowedException {
		if (this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE)) {
			return EnumSet.allOf(PrivilegeType.class);
		} else {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
	}

	public boolean specifiedUserHasPrivilege(HttpServletRequest request,
			PrivilegeType privilegeType, AppUserInfo user) throws DisallowedException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE) || this.authenticator
				.loggedInUserAllowedTo(request, PrivilegeType.MASTER))) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		Set<UserGeneralPrivilegeInfo> userPrivileges = getPrivilegesForUser(request, user);
		for (UserGeneralPrivilegeInfo privilege : userPrivileges) {
			// We aren't interested in object specific privileges here, that's
			// the other
			// specifiedUserHasPrivilege method
			if (!(privilege instanceof UserObjectPrivilegeInfo)) {
				if (privilege.getPrivilegeType().equals(privilegeType)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean specifiedUserHasPrivilege(HttpServletRequest request,
			PrivilegeType privilegeType, AppUserInfo user, TableInfo table)
			throws DisallowedException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		Set<UserGeneralPrivilegeInfo> userPrivileges = getPrivilegesForUser(request, user);
		for (UserGeneralPrivilegeInfo privilege : userPrivileges) {
			// We're only interested in table-specific privileges here, general
			// privileges are handled by the
			// other sessionUserHasPrivileges method
			if (privilege instanceof UserObjectPrivilegeInfo) {
				if (privilege.getPrivilegeType().equals(privilegeType)
						&& ((UserObjectPrivilegeInfo) privilege).getTable().getInternalTableName()
								.equals(table.getInternalTableName())) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean specifiedRoleHasPrivilege(HttpServletRequest request,
			PrivilegeType privilegeType, AppRoleInfo role, TableInfo table)
			throws DisallowedException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		Set<RoleGeneralPrivilegeInfo> rolePrivileges = getPrivilegesForRole(request, role);
		for (RoleGeneralPrivilegeInfo privilege : rolePrivileges) {
			if (privilege instanceof RoleObjectPrivilegeInfo) {
				if (privilege.getPrivilegeType().equals(privilegeType)
						&& ((RoleObjectPrivilegeInfo) privilege).getTable().getInternalTableName()
								.equals(table.getInternalTableName())) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean specifiedRoleHasPrivilege(HttpServletRequest request,
			PrivilegeType privilegeType, AppRoleInfo role) throws DisallowedException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		Set<RoleGeneralPrivilegeInfo> rolePrivileges = getPrivilegesForRole(request, role);
		for (RoleGeneralPrivilegeInfo privilege : rolePrivileges) {
			if (!(privilege instanceof RoleObjectPrivilegeInfo)) {
				if (privilege.getPrivilegeType().equals(privilegeType)) {
					return true;
				}
			}
		}
		return false;
	}

	private AuthenticatorInfo authenticator = null;

	/**
	 * A cache of which company each table belongs to
	 */
	private Map<TableInfo, CompanyInfo> tableCompaniesCache = new HashMap<TableInfo, CompanyInfo>();

	private static final SimpleLogger logger = new SimpleLogger(AuthManager.class);
}