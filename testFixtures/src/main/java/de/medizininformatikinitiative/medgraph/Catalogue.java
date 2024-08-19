package de.medizininformatikinitiative.medgraph;

import java.lang.reflect.AccessFlag;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a class whose only purpose it is to provide static constants. This interface provides some utility
 * functions for such classes.
 *
 * @author Markus Budeus
 */
public interface Catalogue {

	/**
	 * Equivalent to calling {@link #getAllFields(Class, boolean)} while exluding declared classes.
	 */
	static <T> List<T> getAllFields(Class<? extends Catalogue> catalogue) {
		return getAllFields(catalogue, false);
	}

	/**
	 * Returns all static final fields associated with the given {@link Catalogue}. Note this requires those fields to
	 * have a type which is compatible with the specified generic return type.
	 *
	 * @param catalogue              the catalogue class
	 * @param includeDeclaredClasses if true, fields from inner classes are included
	 * @param <T>                    the type as which to return the fields
	 * @return a list of all field names which belong to the given catalogue
	 * @throws ClassCastException if any of the searched classes contains a static final field not compatible with the
	 *                            generic type
	 */
	@SuppressWarnings("unchecked")
	static <T> List<T> getAllFields(Class<? extends Catalogue> catalogue, boolean includeDeclaredClasses) {
		List<Field> reflectiveFields = getAllReflectiveFields(catalogue, includeDeclaredClasses);
		List<T> acutalFields = new ArrayList<>(reflectiveFields.size());
		for (Field field : reflectiveFields) {
			try {
				acutalFields.add((T) field.get(null));
			} catch (IllegalAccessException e) {
				throw new RuntimeException("The FieldDictionary is broken!");
			} catch (ClassCastException e) {
				throw (ClassCastException) new ClassCastException(
						"Failed to coerce all catalog fields into the requested output class!").initCause(e);
			}
		}
		return acutalFields;
	}

	/**
	 * Returns all static final fields declared by the given class.
	 *
	 * @param catalogue              the class from which to acquire the fields
	 * @param includeDeclaredClasses if true, fields from declared inner classes are included
	 */
	@SuppressWarnings("unchecked")
	private static List<Field> getAllReflectiveFields(Class<? extends Catalogue> catalogue,
	                                                  boolean includeDeclaredClasses) {
		List<Field> fields = new LinkedList<>(Arrays.asList(catalogue.getDeclaredFields()));
		fields.removeIf(field -> !field.accessFlags().contains(AccessFlag.FINAL) || !field.accessFlags()
		                                                                                  .contains(AccessFlag.STATIC));

		if (includeDeclaredClasses)
			for (Class<?> nestedClass : catalogue.getDeclaredClasses()) {
				if (Catalogue.class.isAssignableFrom(nestedClass)) {
					fields.addAll(getAllReflectiveFields((Class<? extends Catalogue>) nestedClass, true));
				}
			}

		return fields;
	}


}
