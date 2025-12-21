package khubanov.ochir;

import java.util.Locale;

public class PeopleUtils {
    static String prefer(String oldVal, String newVal) {
        if (newVal == null || newVal.isBlank()) return oldVal;
        if (oldVal == null || oldVal.isBlank()) return newVal;
        if (oldVal.equals(newVal)) return oldVal;
        return oldVal;
    }

    static String normalizeName(String s) {
        if (s == null) return null;
        return s.trim().replaceAll("\\s+", " ");
    }

    static String buildNameKey(Person p) {
        if (p.firstName != null || p.lastName != null) {
            String fn = p.firstName != null ? p.firstName : "";
            String ln = p.lastName != null ? p.lastName : "";
            String name = normalizeName((fn + " " + ln).trim());
            if (name != null && !name.isEmpty()) return name;
        }
        if (p.nameAttr != null && !p.nameAttr.isBlank()) {
            return normalizeName(p.nameAttr);
        }
        return null;
    }

    static Gender parseGender(String raw) {
        if (raw == null) return Gender.UNKNOWN;
        String s = raw.trim().toLowerCase(Locale.ROOT);
        return switch (s) {
            case "male", "m" -> Gender.MALE;
            case "female", "f" -> Gender.FEMALE;
            default -> Gender.UNKNOWN;
        };
    }

    static boolean isIdLike(String v) {
        return v != null && v.matches("P\\d+");
    }

    static Integer parseIntSafe(String s) {
        if (s == null) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            System.err.println("Cannot parse int: " + s);
            return null;
        }
    }

    static boolean isNoneLike(String v) {
        if (v == null) return true;
        String s = v.trim();
        if (s.isEmpty()) return true;
        String low = s.toLowerCase(Locale.ROOT);
        return low.equals("none") || low.equals("unknown");
    }

    static void mergePerson(Person target, Person src) {
        target.firstName = prefer(target.firstName, src.firstName);
        target.lastName = prefer(target.lastName, src.lastName);
        target.nameAttr = prefer(target.nameAttr, src.nameAttr);

        if (target.gender == Gender.UNKNOWN && src.gender != Gender.UNKNOWN) {
            target.gender = src.gender;
        }

        target.fatherName = prefer(target.fatherName, src.fatherName);
        target.motherName = prefer(target.motherName, src.motherName);

        target.parentIds.addAll(src.parentIds);

        target.childrenIds.addAll(src.childrenIds);
        target.sonsIds.addAll(src.sonsIds);
        target.daughtersIds.addAll(src.daughtersIds);

        if (target.childrenNumberExpected == null) {
            target.childrenNumberExpected = src.childrenNumberExpected;
        }

        target.siblingIds.addAll(src.siblingIds);
        if (target.siblingsNumberExpected == null) {
            target.siblingsNumberExpected = src.siblingsNumberExpected;
        }
        target.siblingBrotherNames.addAll(src.siblingBrotherNames);
        target.siblingSisterNames.addAll(src.siblingSisterNames);

        target.spouseIds.addAll(src.spouseIds);
        target.wifeIds.addAll(src.wifeIds);
        target.husbandIds.addAll(src.husbandIds);
        target.spouseNames.addAll(src.spouseNames);
        target.wifeNames.addAll(src.wifeNames);
        target.husbandNames.addAll(src.husbandNames);
    }

    static String printableKey(Person p) {
        if (p.id != null && !p.id.isBlank()) return p.id;
        if (p.key != null && !p.key.isBlank()) return p.key;
        return "(unknown person)";
    }
}
