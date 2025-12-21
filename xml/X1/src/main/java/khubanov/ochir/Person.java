package khubanov.ochir;

import java.util.LinkedHashSet;
import java.util.Set;

public class Person {
    String key;
    String id;

    String nameAttr;
    String firstName;
    String lastName;

    Gender gender = Gender.UNKNOWN;

    String fatherName;
    String motherName;
    Set<String> parentIds = new LinkedHashSet<>();
    String fatherId;
    String motherId;
    Set<String> unknownParentIds = new LinkedHashSet<>();

    Set<String> childrenIds = new LinkedHashSet<>();
    Set<String> sonsIds = new LinkedHashSet<>();
    Set<String> daughtersIds = new LinkedHashSet<>();
    Integer childrenNumberExpected;

    Set<String> siblingIds = new LinkedHashSet<>();
    Integer siblingsNumberExpected;
    Set<String> brothersIds = new LinkedHashSet<>();
    Set<String> sistersIds = new LinkedHashSet<>();
    Set<String> unknownSiblingIds = new LinkedHashSet<>();
    Set<String> siblingBrotherNames = new LinkedHashSet<>();
    Set<String> siblingSisterNames = new LinkedHashSet<>();

    Set<String> spouseIds = new LinkedHashSet<>();
    Set<String> wifeIds = new LinkedHashSet<>();
    Set<String> husbandIds = new LinkedHashSet<>();
    Set<String> spouseNames = new LinkedHashSet<>();
    Set<String> wifeNames = new LinkedHashSet<>();
    Set<String> husbandNames = new LinkedHashSet<>();
}
