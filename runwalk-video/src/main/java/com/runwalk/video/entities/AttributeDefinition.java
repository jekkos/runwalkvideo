package com.runwalk.video.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "ospos_attribute_definitions")
public class AttributeDefinition implements Serializable {

    @Id
    @Column(name = "definition_id")
    private Long id;

    @Column(name = "definition_name")
    private String name;

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }
}
