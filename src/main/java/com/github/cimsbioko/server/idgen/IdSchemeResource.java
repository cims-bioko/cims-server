package com.github.cimsbioko.server.idgen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

@Component
public class IdSchemeResource {

    @Autowired
    private List<IdScheme> idScheme;

    public List<IdScheme> getIdScheme() {
        return idScheme;
    }

    @PostConstruct
    public void sort() {
        Collections.sort(idScheme);
    }
}
