package khubanov.ochir;

import java.util.Map;

public record PeopleParseResult(int headerCount, int personElementsSeen, Map<String, Person> people) {
}
