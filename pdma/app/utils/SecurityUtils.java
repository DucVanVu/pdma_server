package org.pepfar.pdma.app.utils;

import java.util.List;
import java.util.Set;

import org.pepfar.pdma.app.data.domain.Role;
import org.pepfar.pdma.app.data.domain.User;
import org.pepfar.pdma.app.data.dto.RoleDto;
import org.pepfar.pdma.app.data.dto.UserDto;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.google.common.collect.Lists;

public class SecurityUtils {

	/**
	 * Check if the current user is authenticated
	 * 
	 * @return
	 */
	public static boolean isAuthenticated() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		return CommonUtils.isNotNull(authentication) && authentication.isAuthenticated()
				&& !(authentication instanceof AnonymousAuthenticationToken);
	}

	/**
	 * Check if the current user has #role ro
	 * 
	 * @return
	 */
	public static boolean isUserInRole(User user, String role) {

		if (CommonUtils.isNull(user)) {
			return false;
		}

		Set<GrantedAuthority> roles = (Set<GrantedAuthority>) user.getAuthorities();
		for (GrantedAuthority ga : roles) {
			if (!(ga instanceof Role)) {
				continue;
			}

			if (ga.getAuthority().equals(role)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Check if a user has at least one role in the list
	 * 
	 * @param user
	 * @param roleNames
	 * @return
	 */
	public static boolean isUserInRoles(User user, String... roleNames) {

		if (CommonUtils.isNull(user) || CommonUtils.isEmpty(roleNames)) {
			return false;
		}

		List<String> listNames = Lists.newArrayList(roleNames);

		Set<GrantedAuthority> roles = (Set<GrantedAuthority>) user.getAuthorities();
		for (GrantedAuthority ga : roles) {
			if (!(ga instanceof Role)) {
				continue;
			}

			if (listNames.contains(ga.getAuthority())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Check if a user DTO contains a role
	 * 
	 * @param user
	 * @param role
	 * @return
	 */
	public static boolean isUserInRole(UserDto user, String role) {

		if (CommonUtils.isNull(user)) {
			return false;
		}

		Set<RoleDto> roles = user.getRoles();
		for (RoleDto r : roles) {
			if (r.getName().equals(role)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Return the currently authenticated user details
	 * 
	 * @return
	 */
	public static User getCurrentUser() {

		if (!isAuthenticated()) {
			return null;
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		return (User) authentication.getPrincipal();
	}

	/**
	 * Force signing in a user
	 * 
	 * @param user
	 */
	public static void setCurrentUser(User user) {

		Authentication authentication = new UsernamePasswordAuthenticationToken(user, user.getPassword(),
				user.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	/**
	 * Encrypt password with Spring Security's BCryptPasswordEncoder
	 * 
	 * @param password
	 * @return
	 */
	public static String getHashPassword(String password) {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		return passwordEncoder.encode(password);
	}

	/**
	 * Check if a plain password matches the encrypted password
	 * 
	 * @param encryptedPassword
	 * @param plainPassword
	 * @return
	 */
	public static boolean passwordsMatch(String encryptedPassword, String plainPassword) {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

		return passwordEncoder.matches(plainPassword, encryptedPassword);
	}
}
