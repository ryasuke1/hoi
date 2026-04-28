package khubanov.ochir;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class PeopleNormalizerX2 {
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: java -jar app.jar <input.xml> <output.xml> <schema.xsd>");
            return;
        }

        Path input = Path.of(args[0]);
        Path output = Path.of(args[1]);
        Path schema = Path.of(args[2]);

        PeopleParseResult parseResult;
        try (InputStream in = Files.newInputStream(input)) {
            parseResult = new PeopleParser().parse(in);
        }

        Map<String, Person> people = parseResult.people();

        System.out.println("Header count: " + parseResult.headerCount());
        System.out.println("Person elements seen: " + parseResult.personElementsSeen());
        System.out.println("Unique persons after merge: " + people.size());

        PeopleConsistency consistency = new PeopleConsistency();
        consistency.validate(people);
        consistency.resolveSiblingNamesByPersons(people);
        consistency.buildSiblingGraph(people);
        consistency.splitSiblingsByGender(people);
        consistency.resolveParentsByGender(people);
        consistency.resolveSpousesByName(people);
        consistency.buildSpouseGraph(people);

        JaxbModel.JaxbPeople root = new PeopleJaxbAdapter().adapt(people);

        try (OutputStream out = Files.newOutputStream(output)) {
            new PeopleJaxbWriter().write(root, out, schema);
        }

        System.out.println("Done. Wrote JAXB XML (validated by XSD) to " + output);
    }
}
