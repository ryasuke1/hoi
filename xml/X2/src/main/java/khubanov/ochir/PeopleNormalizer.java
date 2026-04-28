package khubanov.ochir;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class PeopleNormalizer {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java PeopleNormalizer <input.xml> <output.xml>");
            return;
        }

        Path input = Path.of(args[0]);
        Path output = Path.of(args[1]);

        PeopleParseResult parseResult;
        try (InputStream in = Files.newInputStream(input)) {
            parseResult = new PeopleParser().parse(in);
        }

        int headerCount = parseResult.headerCount();
        int personElementsSeen = parseResult.personElementsSeen();
        Map<String, Person> people = parseResult.people();

        System.out.println("Header count: " + headerCount);
        System.out.println("Person elements seen: " + personElementsSeen);
        System.out.println("Unique persons after merge: " + people.size());

        if (headerCount != -1 && headerCount != personElementsSeen) {
            System.err.printf("Person element count mismatch: header=%d, actual=%d%n",
                    headerCount, personElementsSeen);
        }

        PeopleConsistency consistency = new PeopleConsistency();
        consistency.validate(people);
        consistency.resolveSiblingNamesByPersons(people);
        consistency.buildSiblingGraph(people);
        consistency.splitSiblingsByGender(people);
        consistency.resolveParentsByGender(people);
        consistency.resolveSpousesByName(people);
        consistency.buildSpouseGraph(people);

        try (OutputStream out = Files.newOutputStream(output)) {
            new PeopleXmlWriter().write(people, out);
        }

        System.out.println("Done. Wrote normalized XML to " + output);
    }
}
