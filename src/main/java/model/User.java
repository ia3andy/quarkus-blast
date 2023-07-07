package model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.quarkiverse.renarde.security.RenardeUser;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "user_table", uniqueConstraints = @UniqueConstraint(columnNames = {"tenantId", "authId"}))
public class User extends PanacheEntity implements RenardeUser {
	
	@Column(nullable = false)
	public String email;
	// NOT AN ID
	public String userName;
	public String firstName;
	public String lastName;

	public String tenantId;
	public String authId;

	public boolean isAdmin;

	@Override
	public boolean registered(){
	    return true;
	}

	@Override
	public Set<String> roles() {
		Set<String> roles = new HashSet<>();
		if(isAdmin) {
			roles.add("admin");
		}
		return roles;
	}

    @Override
    public String userId() {
        return authId;
    }

	//
	// Helpers

    public static User findByAuthId(String tenantId, String authId) {
        return find("tenantId = ?1 AND authId = ?2", tenantId, authId).firstResult();
    }
}