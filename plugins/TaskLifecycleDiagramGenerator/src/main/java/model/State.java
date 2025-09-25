package model;

import java.util.List;

public record State(
        String stateName,
        List<Transition> transitions
) {
}
