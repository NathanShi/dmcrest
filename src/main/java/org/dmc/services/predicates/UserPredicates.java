package org.dmc.services.predicates;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.types.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.dmc.services.data.entities.QUser;

import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

public class UserPredicates extends Predicates {

	public static Predicate likeFirstName(List<String> firstNameFilter) {
		BooleanBuilder builder = new BooleanBuilder();

		if (isNotEmpty(firstNameFilter)) {
			firstNameFilter.stream().filter(StringUtils::isNotEmpty).forEach(firstName ->
					builder.or(QUser.user.firstName.containsIgnoreCase(firstName)));
		}

		return builder.getValue();
	}

	public static Predicate likeLastName(List<String> lastNameFilter){
		BooleanBuilder builder = new BooleanBuilder();

		if (isNotEmpty(lastNameFilter)) {
			lastNameFilter.stream().filter(StringUtils::isNotEmpty).forEach(lastName ->
					builder.or(QUser.user.lastName.containsIgnoreCase(lastName)));
		}

		return builder.getValue();
	}

	public static Predicate likeDisplayName(List<String> likeDisplayName){
		BooleanBuilder builder = new BooleanBuilder();

		if (isNotEmpty(likeDisplayName)) {
			likeDisplayName.stream().filter(StringUtils::isNotEmpty).forEach(displayName ->
					builder.or(QUser.user.realname.containsIgnoreCase(displayName)));
		}

		return builder.getValue();
	}
}
