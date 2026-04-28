package khubanov.ochir;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class PeopleXmlWriter {
    public void write(Map<String, Person> people, OutputStream out) throws XMLStreamException {
        XMLOutputFactory outFactory = XMLOutputFactory.newFactory();
        XMLStreamWriter w = outFactory.createXMLStreamWriter(
                new OutputStreamWriter(out, StandardCharsets.UTF_8));

        Map<String, Person> byId = new HashMap<>();
        for (Person p : people.values()) {
            if (p.id != null && !p.id.isBlank()) {
                byId.put(p.id, p);
            }
        }

        w.writeStartDocument("UTF-8", "1.0");
        w.writeCharacters("\n");
        w.writeStartElement("people");
        w.writeAttribute("count", String.valueOf(people.size()));
        w.writeCharacters("\n");

        for (Person p : people.values()) {
            w.writeCharacters("  ");
            w.writeStartElement("person");
            if (p.id != null && !p.id.isBlank()) {
                w.writeAttribute("id", p.id);
            }
            w.writeCharacters("\n");

            if (p.firstName != null) {
                w.writeCharacters("    ");
                w.writeStartElement("firstname");
                w.writeCharacters(p.firstName);
                w.writeEndElement();
                w.writeCharacters("\n");
            }
            if (p.lastName != null) {
                w.writeCharacters("    ");
                w.writeStartElement("surname");
                w.writeCharacters(p.lastName);
                w.writeEndElement();
                w.writeCharacters("\n");
            }

            if (p.gender != Gender.UNKNOWN) {
                w.writeCharacters("    ");
                w.writeStartElement("gender");
                w.writeCharacters(p.gender == Gender.MALE ? "male" : "female");
                w.writeEndElement();
                w.writeCharacters("\n");
            }

            for (String sid : p.spouseIds) {
                String tagName;
                if (p.wifeIds.contains(sid)) {
                    tagName = "wife";
                } else if (p.husbandIds.contains(sid)) {
                    tagName = "husband";
                } else {
                    Person partner = byId.get(sid);
                    if (partner != null) {
                        if (p.gender == Gender.MALE && partner.gender == Gender.FEMALE) {
                            tagName = "wife";
                        } else if (p.gender == Gender.FEMALE && partner.gender == Gender.MALE) {
                            tagName = "husband";
                        } else {
                            tagName = "spouse";
                        }
                    } else {
                        tagName = "spouse";
                    }
                }
                w.writeCharacters("    ");
                w.writeEmptyElement(tagName);
                w.writeAttribute("id", sid);
                w.writeCharacters("\n");
            }

            for (String name : p.wifeNames) {
                w.writeCharacters("    ");
                w.writeStartElement("wife");
                w.writeCharacters(name);
                w.writeEndElement();
                w.writeCharacters("\n");
            }
            for (String name : p.husbandNames) {
                w.writeCharacters("    ");
                w.writeStartElement("husband");
                w.writeCharacters(name);
                w.writeEndElement();
                w.writeCharacters("\n");
            }
            for (String name : p.spouseNames) {
                w.writeCharacters("    ");
                w.writeStartElement("spouse");
                w.writeCharacters(name);
                w.writeEndElement();
                w.writeCharacters("\n");
            }

            if (p.fatherId != null || p.motherId != null ||
                    !p.unknownParentIds.isEmpty() ||
                    p.fatherName != null || p.motherName != null) {

                w.writeCharacters("    ");
                w.writeStartElement("parents");
                w.writeCharacters("\n");

                if (p.fatherId != null) {
                    w.writeCharacters("      ");
                    w.writeStartElement("father");
                    w.writeAttribute("id", p.fatherId);
                    if (p.fatherName != null) {
                        w.writeCharacters(p.fatherName);
                    }
                    w.writeEndElement();
                    w.writeCharacters("\n");
                } else if (p.fatherName != null) {
                    w.writeCharacters("      ");
                    w.writeStartElement("father");
                    w.writeCharacters(p.fatherName);
                    w.writeEndElement();
                    w.writeCharacters("\n");
                }

                if (p.motherId != null) {
                    w.writeCharacters("      ");
                    w.writeStartElement("mother");
                    w.writeAttribute("id", p.motherId);
                    if (p.motherName != null) {
                        w.writeCharacters(p.motherName);
                    }
                    w.writeEndElement();
                    w.writeCharacters("\n");
                } else if (p.motherName != null) {
                    w.writeCharacters("      ");
                    w.writeStartElement("mother");
                    w.writeCharacters(p.motherName);
                    w.writeEndElement();
                    w.writeCharacters("\n");
                }

                for (String pid : p.unknownParentIds) {
                    w.writeCharacters("      ");
                    w.writeEmptyElement("parent");
                    w.writeAttribute("id", pid);
                    w.writeCharacters("\n");
                }

                w.writeCharacters("    ");
                w.writeEndElement();
                w.writeCharacters("\n");
            }

            if (!p.childrenIds.isEmpty()) {
                w.writeCharacters("    ");
                w.writeStartElement("children");
                w.writeCharacters("\n");

                for (String cid : p.childrenIds) {
                    String childTag;
                    if (p.sonsIds.contains(cid)) {
                        childTag = "son";
                    } else if (p.daughtersIds.contains(cid)) {
                        childTag = "daughter";
                    } else {
                        childTag = "child";
                    }
                    w.writeCharacters("      ");
                    w.writeEmptyElement(childTag);
                    w.writeAttribute("id", cid);
                    w.writeCharacters("\n");
                }

                w.writeCharacters("    ");
                w.writeEndElement();
                w.writeCharacters("\n");
            }

            if (!p.brothersIds.isEmpty() || !p.sistersIds.isEmpty() ||
                    !p.unknownSiblingIds.isEmpty() ||
                    !p.siblingBrotherNames.isEmpty() || !p.siblingSisterNames.isEmpty()) {

                w.writeCharacters("    ");
                w.writeStartElement("siblings");
                w.writeCharacters("\n");

                for (String bid : p.brothersIds) {
                    w.writeCharacters("      ");
                    w.writeEmptyElement("brother");
                    w.writeAttribute("id", bid);
                    w.writeCharacters("\n");
                }
                for (String sid : p.sistersIds) {
                    w.writeCharacters("      ");
                    w.writeEmptyElement("sister");
                    w.writeAttribute("id", sid);
                    w.writeCharacters("\n");
                }
                for (String uid : p.unknownSiblingIds) {
                    w.writeCharacters("      ");
                    w.writeEmptyElement("sibling");
                    w.writeAttribute("id", uid);
                    w.writeCharacters("\n");
                }
                for (String name : p.siblingBrotherNames) {
                    w.writeCharacters("      ");
                    w.writeStartElement("brother");
                    w.writeCharacters(name);
                    w.writeEndElement();
                    w.writeCharacters("\n");
                }
                for (String name : p.siblingSisterNames) {
                    w.writeCharacters("      ");
                    w.writeStartElement("sister");
                    w.writeCharacters(name);
                    w.writeEndElement();
                    w.writeCharacters("\n");
                }

                w.writeCharacters("    ");
                w.writeEndElement();
                w.writeCharacters("\n");
            }

            w.writeCharacters("  ");
            w.writeEndElement();
            w.writeCharacters("\n");
        }

        w.writeEndElement();
        w.writeCharacters("\n");
        w.writeEndDocument();
        w.flush();
        w.close();
    }
}
