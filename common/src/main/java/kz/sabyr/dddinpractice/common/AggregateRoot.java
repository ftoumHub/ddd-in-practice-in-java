package kz.sabyr.dddinpractice.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public abstract class AggregateRoot extends Entity {

    private List<DomainEvent> eventList = new ArrayList<>();

    protected void addDomainEvent(DomainEvent ev) {
        eventList.add(ev);
    }

    public void clearEvents() {
        eventList.clear();
    }

    public List<DomainEvent> getEventList() {
        return unmodifiableList(eventList);
    }
}
