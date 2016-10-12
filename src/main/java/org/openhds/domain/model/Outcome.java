package org.openhds.domain.model;

import org.openhds.domain.annotations.Description;
import org.openhds.domain.constraint.CheckEntityNotVoided;
import org.openhds.domain.constraint.CheckFieldNotBlank;
import org.openhds.domain.constraint.CheckIndividualNotUnknown;

import javax.persistence.*;
import java.io.Serializable;

@Description(description = "An Outcome represents a result from a Pregnancy. " +
        "The Outcome contains information about the child and Memberships " +
        "in which the child belongs. When a child is born, the Memberships " +
        "are obtained from the mother.")
@Entity
@Table(name = "outcome")
public class Outcome implements Serializable {

    private static final long serialVersionUID = -1667849707971051732L;

    @Id
    @Column(length = 32)
    private String uuid;

    @CheckFieldNotBlank
    @Description(description = "Pregnancy outcome type.")
    private String type;

    @OneToOne(cascade = {CascadeType.ALL})
    @CheckEntityNotVoided(allowNull = true)
    @CheckIndividualNotUnknown
    @Description(description = "The child that of the pregnancy, identified by external id.")
    private Individual child;

    @OneToOne
    @Description(description = "Membership of the child, which is obtained from the mother at birth.")
    private Membership childMembership;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = PregnancyOutcome.class)
    private PregnancyOutcome pregnancyOutcome;

    public PregnancyOutcome getPregnancyOutcome() {
        return pregnancyOutcome;
    }

    public void setPregnancyOutcome(PregnancyOutcome outcome) {
        this.pregnancyOutcome = outcome;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Individual getChild() {
        return child;
    }

    public void setChild(Individual child) {
        this.child = child;
    }

    public Membership getChildMembership() {
        return childMembership;
    }

    public void setChildMembership(Membership childMembership) {
        this.childMembership = childMembership;
    }
}
