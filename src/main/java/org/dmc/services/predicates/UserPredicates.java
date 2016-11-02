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

	public static Predicate likeUserName(List<String> likeUserName){
		BooleanBuilder builder = new BooleanBuilder();

		if (isNotEmpty(likeUserName)) {
			likeUserName.stream().filter(StringUtils::isNotEmpty).forEach(userName ->
					builder.or(QUser.user.username.containsIgnoreCase(userName)));
		}

		return builder.getValue();
	}
}