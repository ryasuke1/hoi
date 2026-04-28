package khubanov.ochir;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

public class PeopleParser {
    public PeopleParseResult parse(InputStream in) throws XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        XMLStreamReader reader = factory.createXMLStreamReader(in);

        Map<String, Person> people = new LinkedHashMap<>();
        Person current = null;
        String currentTextElement = null;
        Deque<String> elementStack = new ArrayDeque<>();

        int headerCount = -1;
        int personElementsSeen = 0;

        while (reader.hasNext()) {
            int event = reader.next();

            switch (event) {
                case XMLStreamConstants.START_ELEMENT -> {
                    String name = reader.getLocalName();
                    elementStack.push(name);

                    if ("people".equals(name)) {
                        String c = reader.getAttributeValue(null, "count");
                        if (c != null && !c.isBlank()) {
                            headerCount = PeopleUtils.parseIntSafe(c.trim());
                        }
                    } else if ("person".equals(name)) {
                        personElementsSeen++;
                        current = new Person();
                        String idAttr = reader.getAttributeValue(null, "id");
                        if (idAttr != null && !idAttr.isBlank()) {
                            current.id = idAttr.trim();
                        }
                        String nameAttr = reader.getAttributeValue(null, "name");
                        if (nameAttr != null && !nameAttr.isBlank()) {
                            current.nameAttr = PeopleUtils.normalizeName(nameAttr);
                        }
                    } else if (current != null) {
                        switch (name) {
                            case "firstname" -> {
                                String v = reader.getAttributeValue(null, "value");
                                if (v != null) {
                                    current.firstName = PeopleUtils.prefer(current.firstName, PeopleUtils.normalizeName(v));
                                    currentTextElement = null;
                                } else {
                                    currentTextElement = "firstname";
                                }
                            }
                            case "surname" -> {
                                String v = reader.getAttributeValue(null, "value");
                                if (v != null) {
                                    current.lastName = PeopleUtils.prefer(current.lastName, PeopleUtils.normalizeName(v));
                                    currentTextElement = null;
                                } else {
                                    currentTextElement = "surname";
                                }
                            }
                            case "family-name" -> {
                                String v = reader.getAttributeValue(null, "value");
                                if (v != null) {
                                    current.lastName = PeopleUtils.prefer(current.lastName, PeopleUtils.normalizeName(v));
                                    currentTextElement = null;
                                } else {
                                    currentTextElement = "family-name";
                                }
                            }
                            case "fullname" -> currentTextElement = null;
                            case "first" -> currentTextElement = "first";
                            case "family" -> currentTextElement = "family";
                            case "gender" -> {
                                String v = reader.getAttributeValue(null, "value");
                                if (v != null) {
                                    current.gender = PeopleUtils.parseGender(v.trim());
                                    currentTextElement = null;
                                } else {
                                    currentTextElement = "gender";
                                }
                            }
                            case "id" -> {
                                String v = reader.getAttributeValue(null, "value");
                                if (v != null && (current.id == null || current.id.isBlank())) {
                                    current.id = v.trim();
                                }
                            }
                            case "wife" -> {
                                String v = reader.getAttributeValue(null, "value");
                                if (v != null && !v.isBlank()) {
                                    String t = v.trim();
                                    if (!PeopleUtils.isNoneLike(t)) {
                                        if (PeopleUtils.isIdLike(t)) {
                                            current.spouseIds.add(t);
                                            current.wifeIds.add(t);
                                        } else {
                                            String n = PeopleUtils.normalizeName(t);
                                            current.wifeNames.add(n);
                                        }
                                    }
                                    currentTextElement = null;
                                } else {
                                    currentTextElement = "wife";
                                }
                            }
                            case "husband" -> {
                                String v = reader.getAttributeValue(null, "value");
                                if (v != null && !v.isBlank()) {
                                    String t = v.trim();
                                    if (!PeopleUtils.isNoneLike(t)) {
                                        if (PeopleUtils.isIdLike(t)) {
                                            current.spouseIds.add(t);
                                            current.husbandIds.add(t);
                                        } else {
                                            String n = PeopleUtils.normalizeName(t);
                                            current.husbandNames.add(n);
                                        }
                                    }
                                    currentTextElement = null;
                                } else {
                                    currentTextElement = "husband";
                                }
                            }
                            case "spouce", "spouse" -> {
                                String v = reader.getAttributeValue(null, "value");
                                if (v != null && !v.isBlank()) {
                                    String t = v.trim();
                                    if (!PeopleUtils.isNoneLike(t)) {
                                        if (PeopleUtils.isIdLike(t)) {
                                            current.spouseIds.add(t);
                                        } else {
                                            String n = PeopleUtils.normalizeName(t);
                                            current.spouseNames.add(n);
                                        }
                                    }
                                    currentTextElement = null;
                                } else {
                                    currentTextElement = "spouse";
                                }
                            }
                            case "parent" -> {
                                String v = reader.getAttributeValue(null, "value");
                                if (v != null) {
                                    String t = v.trim();
                                    if (!PeopleUtils.isNoneLike(t)) {
                                        current.parentIds.add(t);
                                    }
                                    currentTextElement = null;
                                } else {
                                    currentTextElement = "parent";
                                }
                            }
                            case "father" -> currentTextElement = "father";
                            case "mother" -> currentTextElement = "mother";
                            case "children-number" -> {
                                String v = reader.getAttributeValue(null, "value");
                                if (v != null && !v.isBlank()) {
                                    current.childrenNumberExpected = PeopleUtils.parseIntSafe(v.trim());
                                }
                            }
                            case "siblings-number" -> {
                                String v = reader.getAttributeValue(null, "value");
                                if (v != null && !v.isBlank()) {
                                    current.siblingsNumberExpected = PeopleUtils.parseIntSafe(v.trim());
                                }
                            }
                            case "siblings" -> {
                                String v = reader.getAttributeValue(null, "val");
                                if (v != null && !v.isBlank()) {
                                    for (String id : v.trim().split("\\s+")) {
                                        current.siblingIds.add(id.trim());
                                    }
                                }
                                currentTextElement = null;
                            }
                            case "brother" -> currentTextElement = "brother";
                            case "sister" -> currentTextElement = "sister";
                            case "children" -> currentTextElement = null;
                            case "son" -> {
                                String id = reader.getAttributeValue(null, "id");
                                if (id != null && !id.isBlank()) {
                                    String t = id.trim();
                                    current.childrenIds.add(t);
                                    current.sonsIds.add(t);
                                }
                                currentTextElement = null;
                            }
                            case "daughter" -> {
                                String id = reader.getAttributeValue(null, "id");
                                if (id != null && !id.isBlank()) {
                                    String t = id.trim();
                                    current.childrenIds.add(t);
                                    current.daughtersIds.add(t);
                                }
                                currentTextElement = null;
                            }
                            default -> currentTextElement = null;
                        }
                    }
                }

                case XMLStreamConstants.CHARACTERS -> {
                    if (current != null && currentTextElement != null && !reader.isWhiteSpace()) {
                        String text = reader.getText().trim();
                        if (text.isEmpty()) break;
                        switch (currentTextElement) {
                            case "firstname" -> current.firstName = PeopleUtils.prefer(current.firstName, PeopleUtils.normalizeName(text));
                            case "surname" -> current.lastName = PeopleUtils.prefer(current.lastName, PeopleUtils.normalizeName(text));
                            case "family-name" -> current.lastName = PeopleUtils.prefer(current.lastName, PeopleUtils.normalizeName(text));
                            case "first" -> current.firstName = PeopleUtils.prefer(current.firstName, PeopleUtils.normalizeName(text));
                            case "family" -> current.lastName = PeopleUtils.prefer(current.lastName, PeopleUtils.normalizeName(text));
                            case "gender" -> current.gender = PeopleUtils.parseGender(text);
                            case "father" -> current.fatherName = PeopleUtils.prefer(current.fatherName, PeopleUtils.normalizeName(text));
                            case "mother" -> current.motherName = PeopleUtils.prefer(current.motherName, PeopleUtils.normalizeName(text));
                            case "parent" -> {
                                String t = text.trim();
                                if (!PeopleUtils.isNoneLike(t)) {
                                    current.parentIds.add(t);
                                }
                            }
                            case "brother" -> current.siblingBrotherNames.add(PeopleUtils.normalizeName(text));
                            case "sister" -> current.siblingSisterNames.add(PeopleUtils.normalizeName(text));
                            case "wife" -> current.wifeNames.add(PeopleUtils.normalizeName(text));
                            case "husband" -> current.husbandNames.add(PeopleUtils.normalizeName(text));
                            case "spouse" -> current.spouseNames.add(PeopleUtils.normalizeName(text));
                        }
                    }
                }

                case XMLStreamConstants.END_ELEMENT -> {
                    String name = reader.getLocalName();
                    elementStack.pop();

                    if ("person".equals(name) && current != null) {
                        String key = current.id;
                        if (key == null || key.isBlank()) {
                            key = PeopleUtils.buildNameKey(current);
                        }
                        current.key = key;

                        if (key != null) {
                            Person existing = people.get(key);
                            if (existing == null) {
                                people.put(key, current);
                            } else {
                                PeopleUtils.mergePerson(existing, current);
                            }
                        }

                        current = null;
                        currentTextElement = null;
                    }

                    if (current != null && ("firstname".equals(name) || "surname".equals(name) ||
                            "family-name".equals(name) || "first".equals(name) || "family".equals(name) ||
                            "gender".equals(name) || "father".equals(name) || "mother".equals(name) ||
                            "parent".equals(name) || "brother".equals(name) || "sister".equals(name) ||
                            "wife".equals(name) || "husband".equals(name) || "spouse".equals(name))) {
                        currentTextElement = null;
                    }
                }

                default -> {
                }
            }
        }

        reader.close();
        return new PeopleParseResult(headerCount, personElementsSeen, people);
    }
}
