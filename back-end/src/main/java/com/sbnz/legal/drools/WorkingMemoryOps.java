package com.sbnz.legal.drools;

import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.KieSession;

import java.util.ArrayList;
import java.util.List;

public final class WorkingMemoryOps {

    private WorkingMemoryOps() {}

    public static <T> List<T> getFacts(KieSession session, Class<T> type) {
        List<T> out = new ArrayList<>();
        for (Object o : session.getObjects(new ClassObjectFilter(type))) {
            out.add(type.cast(o));
        }
        return out;
    }
}
