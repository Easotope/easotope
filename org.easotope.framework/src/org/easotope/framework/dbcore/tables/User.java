/*
 * Copyright © 2016 by Devon Bowen.
 *
 * This file is part of Easotope.
 *
 * Easotope is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify this Program, or any covered work, by linking or combining
 * it with the Eclipse Rich Client Platform (or a modified version of that
 * library), containing parts covered by the terms of the Eclipse Public
 * License, the licensors of this Program grant you additional permission
 * to convey the resulting work. Corresponding Source for a non-source form
 * of such a combination shall include the source code for the parts of the
 * Eclipse Rich Client Platform used as well as that of the covered work.
 *
 * Easotope is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Easotope. If not, see <http://www.gnu.org/licenses/>.
 */

package org.easotope.framework.dbcore.tables;

import org.easotope.framework.dbcore.util.PasswordHash;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName=User.TABLE_NAME)
public class User extends TableObjectWithIntegerId {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "USER_V0";
	public static final String USERNAME_FIELD_NAME = "USERNAME";
	public static final String FULLNAME_FIELD_NAME = "FULLNAME";
	public static final String PASSWORDSALT_FIELD_NAME = "PASSWORDSALT";
	public static final String PASSWORDHASH_FIELD_NAME = "PASSWORDHASH";
	public static final String ISADMIN_FIELD_NAME = "ISADMIN";
	public static final String ISDISABLED_FIELD_NAME = "ISDISABLED";

	@DatabaseField(canBeNull=false, uniqueIndex=true, columnName=USERNAME_FIELD_NAME)
	public String username;
	@DatabaseField(columnName=FULLNAME_FIELD_NAME)
	public String fullName;
	@DatabaseField(dataType=DataType.BYTE_ARRAY, canBeNull=false, columnName=PASSWORDSALT_FIELD_NAME)
	public byte[] passwordSalt;
	@DatabaseField(dataType=DataType.BYTE_ARRAY, canBeNull=false, columnName=PASSWORDHASH_FIELD_NAME)
	public byte[] passwordHash;
	@DatabaseField(columnName=ISADMIN_FIELD_NAME)
	public boolean isAdmin;
	@DatabaseField(columnName=ISDISABLED_FIELD_NAME)
	public boolean isDisabled;

	public User() { }

	public User(User user) {
		super(user);
		username = user.username;
		fullName = user.fullName;
		passwordSalt = user.passwordSalt;
		passwordHash = user.passwordHash;
		isAdmin = user.isAdmin;
		isDisabled = user.isDisabled;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public void setPassword(String password) {
		passwordSalt = PasswordHash.generateSalt();
		passwordHash = PasswordHash.generateHash(password, passwordSalt);
	}
	
	public boolean hasPassword() {
		return passwordSalt != null && passwordHash != null;
	}

	public boolean passwordMatches(String password) {
		if (passwordSalt == null || passwordHash == null) {
			throw new RuntimeException("Password information is not available.");
		}

		byte[] newPasswordHash = PasswordHash.generateHash(password, passwordSalt);

		if (passwordHash.length != newPasswordHash.length) {
			return false;
		}
		
		for (int i=0; i<passwordHash.length; i++) {
			if (passwordHash[i] != newPasswordHash[i]) {
				return false;
			}
		}

		return true;
	}
	
	public boolean getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public boolean getIsDisabled() {
		return isDisabled;
	}

	public void setIsDisabled(boolean isDisabled) {
		this.isDisabled = isDisabled;
	}
}
