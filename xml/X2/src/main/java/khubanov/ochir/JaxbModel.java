package khubanov.ochir;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

public final class JaxbModel {
    private JaxbModel() {}

    @XmlRootElement(name = "people")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class JaxbPeople {
        @XmlAttribute(name = "count", required = true)
        public int count;

        @XmlElement(name = "person")
        public List<JaxbPerson> persons = new ArrayList<>();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class JaxbPerson {
        @XmlAttribute(name = "id", required = true)
        @XmlID
        public String id;

        @XmlAttribute(name = "sourceKey")
        public String sourceKey;

        @XmlAttribute(name = "rawName")
        public String rawName;

        @XmlElement(name = "firstName")
        public String firstName;

        @XmlElement(name = "lastName")
        public String lastName;

        @XmlElement(name = "gender")
        public JaxbGender gender;

        @XmlElement(name = "spouses")
        public SpousesSection spouses;

        @XmlElement(name = "parents")
        public ParentsSection parents;

        @XmlElement(name = "children")
        public ChildrenSection children;

        @XmlElement(name = "siblings")
        public SiblingsSection siblings;
    }

    @XmlType(name = "genderType")
    @XmlEnum()
    public enum JaxbGender {
        @XmlEnumValue("male") MALE,
        @XmlEnumValue("female") FEMALE,
        @XmlEnumValue("unknown") UNKNOWN
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PersonLink {
        @XmlAttribute(name = "ref")
        @XmlIDREF
        public JaxbPerson ref;

        @XmlAttribute(name = "name")
        public String name;

        public static PersonLink byRef(JaxbPerson p) {
            PersonLink l = new PersonLink();
            l.ref = p;
            return l;
        }

        public static PersonLink byName(String n) {
            PersonLink l = new PersonLink();
            l.name = (n == null || n.isBlank()) ? null : n;
            return l;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ParentsSection {
        @XmlElement(name = "father")
        public PersonLink father;

        @XmlElement(name = "mother")
        public PersonLink mother;

        @XmlElement(name = "parent")
        public List<PersonLink> parents = new ArrayList<>();

        public boolean isEmpty() {
            return father == null && mother == null && (parents == null || parents.isEmpty());
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ChildrenSection {
        @XmlAttribute(name = "declaredCount")
        public Integer declaredCount;

        @XmlElement(name = "son")
        public List<PersonLink> sons = new ArrayList<>();

        @XmlElement(name = "daughter")
        public List<PersonLink> daughters = new ArrayList<>();

        @XmlElement(name = "child")
        public List<PersonLink> children = new ArrayList<>();

        public boolean isEmpty() {
            return declaredCount == null && sons.isEmpty() && daughters.isEmpty() && children.isEmpty();
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class SiblingsSection {
        @XmlAttribute(name = "declaredCount")
        public Integer declaredCount;

        @XmlElement(name = "brother")
        public List<PersonLink> brothers = new ArrayList<>();

        @XmlElement(name = "sister")
        public List<PersonLink> sisters = new ArrayList<>();

        @XmlElement(name = "sibling")
        public List<PersonLink> siblings = new ArrayList<>();

        public boolean isEmpty() {
            return declaredCount == null && brothers.isEmpty() && sisters.isEmpty() && siblings.isEmpty();
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class SpousesSection {
        @XmlElement(name = "wife")
        public List<PersonLink> wives = new ArrayList<>();

        @XmlElement(name = "husband")
        public List<PersonLink> husbands = new ArrayList<>();

        @XmlElement(name = "spouse")
        public List<PersonLink> spouses = new ArrayList<>();

        public boolean isEmpty() {
            return wives.isEmpty() && husbands.isEmpty() && spouses.isEmpty();
        }
    }
}
