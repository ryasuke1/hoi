package khubanov.ochir;

import java.util.*;

public class PeopleJaxbAdapter {

    public JaxbModel.JaxbPeople adapt(Map<String, Person> people) {
        JaxbModel.JaxbPeople root = new JaxbModel.JaxbPeople();
        Map<Person, JaxbModel.JaxbPerson> personToJaxb = new IdentityHashMap<>();
        Map<String, JaxbModel.JaxbPerson> byXmlId = new LinkedHashMap<>();

        int gen = 1;
        for (Person p : people.values()) {
            JaxbModel.JaxbPerson jp = new JaxbModel.JaxbPerson();

            String xmlId = normalizeXmlId(p.id);
            if (xmlId == null || byXmlId.containsKey(xmlId)) {
                do {
                    xmlId = "GEN_" + (gen++);
                } while (byXmlId.containsKey(xmlId));
            }

            jp.id = xmlId;
            jp.sourceKey = blankToNull(p.key);
            jp.rawName = blankToNull(p.nameAttr);
            jp.firstName = blankToNull(p.firstName);
            jp.lastName = blankToNull(p.lastName);
            jp.gender = mapGender(p.gender);

            personToJaxb.put(p, jp);
            byXmlId.put(jp.id, jp);
            root.persons.add(jp);
        }

        Map<String, JaxbModel.JaxbPerson> rawIdToJaxb = new HashMap<>();
        for (Person p : people.values()) {
            if (p.id != null && !p.id.isBlank()) {
                rawIdToJaxb.putIfAbsent(p.id, personToJaxb.get(p));
            }
        }

        for (Person p : people.values()) {
            JaxbModel.JaxbPerson jp = personToJaxb.get(p);

            jp.parents = buildParents(p, rawIdToJaxb);
            if (jp.parents.isEmpty()) jp.parents = null;

            jp.children = buildChildren(p, rawIdToJaxb);
            if (jp.children.isEmpty()) jp.children = null;

            jp.siblings = buildSiblings(p, rawIdToJaxb);
            if (jp.siblings.isEmpty()) jp.siblings = null;

            jp.spouses = buildSpouses(p, rawIdToJaxb);
            if (jp.spouses.isEmpty()) jp.spouses = null;
        }

        root.count = root.persons.size();
        return root;
    }

    private JaxbModel.ParentsSection buildParents(Person p, Map<String, JaxbModel.JaxbPerson> byId) {
        JaxbModel.ParentsSection s = new JaxbModel.ParentsSection();

        if (p.fatherId != null) {
            s.father = linkById(byId, p.fatherId);
        } else if (p.fatherName != null && !p.fatherName.isBlank()) {
            s.father = JaxbModel.PersonLink.byName(p.fatherName);
        }

        if (p.motherId != null) {
            s.mother = linkById(byId, p.motherId);
        } else if (p.motherName != null && !p.motherName.isBlank()) {
            s.mother = JaxbModel.PersonLink.byName(p.motherName);
        }

        for (String pid : sorted(p.unknownParentIds)) {
            s.parents.add(linkByIdOrName(byId, pid));
        }

        return s;
    }

    private JaxbModel.ChildrenSection buildChildren(Person p, Map<String, JaxbModel.JaxbPerson> byId) {
        JaxbModel.ChildrenSection s = new JaxbModel.ChildrenSection();
        s.declaredCount = p.childrenNumberExpected;

        for (String id : sorted(p.sonsIds)) {
            s.sons.add(linkByIdOrName(byId, id));
        }
        for (String id : sorted(p.daughtersIds)) {
            s.daughters.add(linkByIdOrName(byId, id));
        }

        Set<String> generic = new LinkedHashSet<>(p.childrenIds);
        generic.removeAll(p.sonsIds);
        generic.removeAll(p.daughtersIds);

        for (String id : sorted(generic)) {
            s.children.add(linkByIdOrName(byId, id));
        }

        return s;
    }

    private JaxbModel.SiblingsSection buildSiblings(Person p, Map<String, JaxbModel.JaxbPerson> byId) {
        JaxbModel.SiblingsSection s = new JaxbModel.SiblingsSection();
        s.declaredCount = p.siblingsNumberExpected;

        for (String id : sorted(p.brothersIds)) {
            s.brothers.add(linkByIdOrName(byId, id));
        }
        for (String id : sorted(p.sistersIds)) {
            s.sisters.add(linkByIdOrName(byId, id));
        }

        for (String id : sorted(p.unknownSiblingIds)) {
            s.siblings.add(linkByIdOrName(byId, id));
        }

        for (String name : sorted(p.siblingBrotherNames)) {
            s.brothers.add(JaxbModel.PersonLink.byName(name));
        }
        for (String name : sorted(p.siblingSisterNames)) {
            s.sisters.add(JaxbModel.PersonLink.byName(name));
        }

        return s;
    }

    private JaxbModel.SpousesSection buildSpouses(Person p, Map<String, JaxbModel.JaxbPerson> byId) {
        JaxbModel.SpousesSection s = new JaxbModel.SpousesSection();

        for (String id : sorted(p.wifeIds)) {
            s.wives.add(linkByIdOrName(byId, id));
        }
        for (String id : sorted(p.husbandIds)) {
            s.husbands.add(linkByIdOrName(byId, id));
        }

        Set<String> generic = new LinkedHashSet<>(p.spouseIds);
        generic.removeAll(p.wifeIds);
        generic.removeAll(p.husbandIds);

        for (String id : sorted(generic)) {
            s.spouses.add(linkByIdOrName(byId, id));
        }

        for (String name : sorted(p.wifeNames)) {
            s.wives.add(JaxbModel.PersonLink.byName(name));
        }
        for (String name : sorted(p.husbandNames)) {
            s.husbands.add(JaxbModel.PersonLink.byName(name));
        }
        for (String name : sorted(p.spouseNames)) {
            s.spouses.add(JaxbModel.PersonLink.byName(name));
        }

        return s;
    }

    private JaxbModel.PersonLink linkById(Map<String, JaxbModel.JaxbPerson> byId, String id) {
        JaxbModel.JaxbPerson target = byId.get(id);
        return target != null ? JaxbModel.PersonLink.byRef(target) : JaxbModel.PersonLink.byName(id);
    }

    private JaxbModel.PersonLink linkByIdOrName(Map<String, JaxbModel.JaxbPerson> byId, String maybeId) {
        if (maybeId == null || maybeId.isBlank()) return JaxbModel.PersonLink.byName(null);
        JaxbModel.JaxbPerson target = byId.get(maybeId);
        return target != null ? JaxbModel.PersonLink.byRef(target) : JaxbModel.PersonLink.byName(maybeId);
    }

    private static List<String> sorted(Collection<String> c) {
        List<String> out = new ArrayList<>();
        for (String s : c) {
            if (s != null && !s.isBlank()) out.add(s);
        }
        Collections.sort(out);
        return out;
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private static JaxbModel.JaxbGender mapGender(Gender g) {
        if (g == null) return JaxbModel.JaxbGender.UNKNOWN;
        return switch (g) {
            case MALE -> JaxbModel.JaxbGender.MALE;
            case FEMALE -> JaxbModel.JaxbGender.FEMALE;
            default -> JaxbModel.JaxbGender.UNKNOWN;
        };
    }

    private static String normalizeXmlId(String id) {
        if (id == null) return null;
        String s = id.trim();
        if (s.isEmpty()) return null;
        s = s.replaceAll("[^A-Za-z0-9._-]", "_");
        if (!s.matches("[A-Za-z_].*")) s = "P_" + s;
        return s;
    }
}
