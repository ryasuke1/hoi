package khubanov.ochir;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class PeopleConsistency {
    public void validate(Map<String, Person> people) {
        for (Person p : people.values()) {
            if (p.childrenNumberExpected != null) {
                int actual = p.childrenIds.size();
                if (actual != p.childrenNumberExpected) {
                    System.err.printf(
                            "Children count mismatch for %s: expected %d, actual %d%n",
                            PeopleUtils.printableKey(p), p.childrenNumberExpected, actual
                    );
                }
            }
            if (p.siblingsNumberExpected != null) {
                int actual = p.siblingIds.size();
                if (actual != p.siblingsNumberExpected) {
                    System.err.printf(
                            "Siblings count mismatch for %s: expected %d, actual %d%n",
                            PeopleUtils.printableKey(p), p.siblingsNumberExpected, actual
                    );
                }
            }
        }
    }

    public void resolveSiblingNamesByPersons(Map<String, Person> people) {
        Map<String, Person> byName = new HashMap<>();
        for (Person p : people.values()) {
            String key = PeopleUtils.buildNameKey(p);
            if (key != null && !byName.containsKey(key)) {
                byName.put(key, p);
            }
        }
        for (Person p : people.values()) {
            Set<String> newBro = new LinkedHashSet<>();
            for (String name : p.siblingBrotherNames) {
                Person sib = byName.get(name);
                if (sib != null && sib.id != null) {
                    p.siblingIds.add(sib.id);
                } else {
                    newBro.add(name);
                }
            }
            p.siblingBrotherNames = newBro;

            Set<String> newSis = new LinkedHashSet<>();
            for (String name : p.siblingSisterNames) {
                Person sib = byName.get(name);
                if (sib != null && sib.id != null) {
                    p.siblingIds.add(sib.id);
                } else {
                    newSis.add(name);
                }
            }
            p.siblingSisterNames = newSis;
        }
    }

    public void buildSiblingGraph(Map<String, Person> people) {
        Map<String, Person> byId = new HashMap<>();
        for (Person p : people.values()) {
            if (p.id != null && !p.id.isBlank()) {
                byId.put(p.id, p);
            }
        }
        for (Person p : people.values()) {
            if (p.id == null || p.id.isBlank()) continue;
            for (String sibId : p.siblingIds) {
                Person sib = byId.get(sibId);
                if (sib != null && sib.id != null) {
                    sib.siblingIds.add(p.id);
                }
            }
        }
    }

    public void splitSiblingsByGender(Map<String, Person> people) {
        Map<String, Person> byId = new HashMap<>();
        for (Person p : people.values()) {
            if (p.id != null && !p.id.isBlank()) {
                byId.put(p.id, p);
            }
        }
        for (Person p : people.values()) {
            p.brothersIds.clear();
            p.sistersIds.clear();
            p.unknownSiblingIds.clear();
            for (String sibId : p.siblingIds) {
                Person sib = byId.get(sibId);
                if (sib == null) {
                    p.unknownSiblingIds.add(sibId);
                    continue;
                }
                if (sib.gender == Gender.MALE) {
                    p.brothersIds.add(sibId);
                } else if (sib.gender == Gender.FEMALE) {
                    p.sistersIds.add(sibId);
                } else {
                    p.unknownSiblingIds.add(sibId);
                }
            }
        }
    }

    public void resolveParentsByGender(Map<String, Person> people) {
        Map<String, Person> byId = new HashMap<>();
        for (Person p : people.values()) {
            if (p.id != null && !p.id.isBlank()) {
                byId.put(p.id, p);
            }
        }
        for (Person child : people.values()) {
            child.fatherId = null;
            child.motherId = null;
            child.unknownParentIds.clear();
            for (String parentId : child.parentIds) {
                Person parent = byId.get(parentId);
                if (parent == null) {
                    child.unknownParentIds.add(parentId);
                    continue;
                }
                if (parent.gender == Gender.MALE) {
                    if (child.fatherId == null) {
                        child.fatherId = parentId;
                    } else {
                        child.unknownParentIds.add(parentId);
                    }
                } else if (parent.gender == Gender.FEMALE) {
                    if (child.motherId == null) {
                        child.motherId = parentId;
                    } else {
                        child.unknownParentIds.add(parentId);
                    }
                } else {
                    child.unknownParentIds.add(parentId);
                }
            }
        }
    }

    public void resolveSpousesByName(Map<String, Person> people) {
        Map<String, Person> byName = new HashMap<>();
        for (Person p : people.values()) {
            String key = PeopleUtils.buildNameKey(p);
            if (key != null && !byName.containsKey(key)) {
                byName.put(key, p);
            }
        }
        for (Person p : people.values()) {
            Set<String> newWifeNames = new LinkedHashSet<>();
            for (String name : p.wifeNames) {
                Person s = byName.get(name);
                if (s != null && s.id != null) {
                    p.spouseIds.add(s.id);
                    p.wifeIds.add(s.id);
                } else {
                    newWifeNames.add(name);
                }
            }
            p.wifeNames = newWifeNames;

            Set<String> newHusbandNames = new LinkedHashSet<>();
            for (String name : p.husbandNames) {
                Person s = byName.get(name);
                if (s != null && s.id != null) {
                    p.spouseIds.add(s.id);
                    p.husbandIds.add(s.id);
                } else {
                    newHusbandNames.add(name);
                }
            }
            p.husbandNames = newHusbandNames;

            Set<String> newSpouseNames = new LinkedHashSet<>();
            for (String name : p.spouseNames) {
                Person s = byName.get(name);
                if (s != null && s.id != null) {
                    p.spouseIds.add(s.id);
                } else {
                    newSpouseNames.add(name);
                }
            }
            p.spouseNames = newSpouseNames;
        }
    }

    public void buildSpouseGraph(Map<String, Person> people) {
        Map<String, Person> byId = new HashMap<>();
        for (Person p : people.values()) {
            if (p.id != null && !p.id.isBlank()) {
                byId.put(p.id, p);
            }
        }
        for (Person p : people.values()) {
            if (p.id == null || p.id.isBlank()) continue;
            for (String sid : p.spouseIds) {
                Person s = byId.get(sid);
                if (s != null && s.id != null) {
                    s.spouseIds.add(p.id);
                }
            }
        }
    }
}
