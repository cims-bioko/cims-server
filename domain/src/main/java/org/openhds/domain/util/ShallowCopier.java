package org.openhds.domain.util;

import org.openhds.domain.model.FieldWorker;
import org.openhds.domain.model.Individual;
import org.openhds.domain.model.Location;
import org.openhds.domain.model.LocationHierarchy;
import org.openhds.domain.model.Membership;
import org.openhds.domain.model.PregnancyOutcome;
import org.openhds.domain.model.Relationship;
import org.openhds.domain.model.Residency;
import org.openhds.domain.model.SocialGroup;
import org.openhds.domain.model.Visit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ShallowCopier {

    private static final Logger logger = LoggerFactory
            .getLogger(ShallowCopier.class);



    public static FieldWorker shallowCopyFieldWorker(FieldWorker fieldWorker) {
        FieldWorker copy = new FieldWorker();
        copy.setUuid(fieldWorker.getUuid());
        copy.setIdPrefix(fieldWorker.getIdPrefix());
        copy.setExtId(fieldWorker.getExtId());
        copy.setFirstName(fieldWorker.getFirstName());
        copy.setLastName(fieldWorker.getLastName());
        copy.setPasswordHash(fieldWorker.getPasswordHash());
        return copy;
    }

    public static Residency shallowCopyResidency(Residency residency) {
        Residency copy = new Residency();
        copy.setUuid(residency.getUuid());
        copy.setCollectedBy(residency.getCollectedBy());
        FieldWorker fieldWorkerStub = FieldWorker.makeStub(residency.getCollectedBy().getUuid(), residency.getCollectedBy().getExtId());
        copy.setCollectedBy(fieldWorkerStub);
        Individual individualStub = Individual.makeStub(residency.getIndividual().getUuid(), residency.getIndividual().getExtId());
        copy.setIndividual(individualStub);
        Location locationStub = Location.makeStub(residency.getLocation().getUuid(), residency.getLocation().getExtId());
        copy.setLocation(locationStub);
        copy.setStartDate(residency.getStartDate());
        copy.setStartType(residency.getStartType());
        copy.setEndDate(residency.getEndDate());
        copy.setEndType(residency.getEndType());
        return copy;
    }

    public static Individual shallowCopyIndividual(Individual individual) {

        Individual copy = new Individual();
        try {
            copy.setUuid(individual.getUuid());
            copy.setDob(individual.getDob());
            copy.setExtId(individual.getExtId());
            copy.setFather(copyIds(individual.getFather()));
            copy.setFirstName(individual.getFirstName());
            copy.setGender(individual.getGender());
            copy.setLastName(individual.getLastName());
            String middleName = individual.getMiddleName() == null ? ""
                    : individual.getMiddleName();
            copy.setMiddleName(middleName);
            copy.setMother(copyIds(individual.getMother()));

            copy.setAge(individual.getAge());
            copy.setAgeUnits(individual.getAgeUnits());
            copy.setPhoneNumber(individual.getPhoneNumber());
            copy.setOtherPhoneNumber(individual.getOtherPhoneNumber());
            copy.setLanguagePreference(individual.getLanguagePreference());
            copy.setPointOfContactName(individual.getPointOfContactName());
            copy.setPointOfContactPhoneNumber(individual
                    .getPointOfContactPhoneNumber());
            copy.setDip(individual.getDip());
            copy.setMemberStatus(individual.getMemberStatus());
            copy.setNationality(individual.getNationality());

            Individual individualStub = Individual.makeStub(individual.getUuid(), individual.getExtId());

            for (Membership membership : individual.getAllMemberships()) {
                SocialGroup socialGroupStub = SocialGroup.makeStub(membership
                        .getSocialGroup().getUuid(), membership.getSocialGroup().getExtId());
                Membership membershipStub = Membership.makeStub(membership.getUuid(), socialGroupStub, individualStub, membership.getbIsToA());
                copy.getAllMemberships().add(membershipStub);
            }

            Residency currentResidency = individual.getCurrentResidency();
            if (null != currentResidency) {
                Location locationStub = Location.makeStub(currentResidency.getLocation().getUuid(), currentResidency.getLocation().getExtId());
                locationStub.setLocationHierarchy(null);
                Residency residencyStub = Residency.makeStub(currentResidency.getUuid(), locationStub,individualStub);
                residencyStub.setEndType(currentResidency.getEndType());
                copy.getAllResidencies().add(residencyStub);
            }
        } catch (Exception e) {
            System.out.println(copy.getExtId());
        }
        return copy;
    }

    private static Individual copyIds(Individual individual) {
        Individual copy = new Individual();
        if (individual == null) {
            logger.warn("Individual had a null father or mother - using UNK as default value");
            copy.setExtId("UNK");
            copy.setUuid("UNK-UUID");
        } else {
            copy.setExtId(individual.getExtId());
            copy.setUuid(individual.getUuid());
        }

        return copy;
    }

    public static Location shallowCopyLocation(Location loc) {
        Location copy = new Location();

        copy.setUuid(loc.getUuid());
        copy.setExtId(loc.getExtId());
        copy.setAccuracy(getEmptyStringIfBlank(loc.getAccuracy()));
        copy.setAltitude(getEmptyStringIfBlank(loc.getAltitude()));
        copy.setLatitude(getEmptyStringIfBlank(loc.getLatitude()));
        copy.setLongitude(getEmptyStringIfBlank(loc.getLongitude()));

        // extensions for bioko island project
        copy.setRegionName(getEmptyStringIfBlank(loc.getRegionName()));
        copy.setProvinceName(getEmptyStringIfBlank(loc.getProvinceName()));
        copy.setDistrictName(getEmptyStringIfBlank(loc.getDistrictName()));
        copy.setSubDistrictName(getEmptyStringIfBlank(loc.getSubDistrictName()));
        copy.setSectorName(getEmptyStringIfBlank(loc.getSectorName()));
        copy.setLocalityName(getEmptyStringIfBlank(loc.getLocalityName()));
        copy.setCommunityName(getEmptyStringIfBlank(loc.getCommunityName()));
        copy.setCommunityCode(getEmptyStringIfBlank(loc.getCommunityCode()));
        copy.setMapAreaName(getEmptyStringIfBlank(loc.getMapAreaName()));

        copy.setBuildingNumber(getEmptyStringIfBlank(loc.getBuildingNumber()));
        copy.setFloorNumber(getEmptyStringIfBlank(loc.getFloorNumber()));
        copy.setProvinceName(getEmptyStringIfBlank(loc.getProvinceName()));
        copy.setRegionName(getEmptyStringIfBlank(loc.getRegionName()));
        copy.setSubDistrictName(getEmptyStringIfBlank(loc.getSubDistrictName()));
        copy.setDistrictName(getEmptyStringIfBlank(loc.getDistrictName()));

        copy.setDescription(getEmptyStringIfBlank(loc.getDescription()));

        LocationHierarchy hierarchy = new LocationHierarchy();
        hierarchy.setUuid(loc.getLocationHierarchy().getUuid());
        hierarchy.setExtId(loc.getLocationHierarchy().getExtId());
        copy.setLocationHierarchy(hierarchy);

        copy.setLocationName(loc.getLocationName());
        copy.setLocationType(loc.getLocationType());

        FieldWorker fieldworkerStub = FieldWorker.makeStub(loc.getCollectedBy().getUuid(), loc.getCollectedBy().getExtId());
        copy.setCollectedBy(fieldworkerStub);
        return copy;
    }

    private static String getEmptyStringIfBlank(String accuracy) {
        if (accuracy == null || accuracy.trim().isEmpty()) {
            return "";
        }

        return accuracy;
    }

    public static Relationship shallowCopyRelationship(Relationship original) {
        Relationship copy = new Relationship();
        copy.setUuid(original.getUuid());
        copy.setaIsToB(original.getaIsToB());

        Individual individualStub = Individual.makeStub(original.getIndividualA().getUuid(), original.getIndividualA().getExtId());
        copy.setIndividualA(individualStub);

        individualStub = Individual.makeStub(original.getIndividualB().getUuid(), original.getIndividualB().getExtId());
        copy.setIndividualB(individualStub);

        copy.setStartDate(original.getStartDate());
        copy.setEndDate(original.getEndDate());
        copy.setEndType(original.getEndType());
        return copy;
    }

    public static SocialGroup shallowCopySocialGroup(SocialGroup original) {
        Individual groupHeadStub = Individual.makeStub(original.getGroupHead().getUuid(), original.getGroupHead().getExtId());
        Location locationStub = Location.makeStub(original.getLocation().getUuid(), original.getLocation().getExtId());

        SocialGroup copy = new SocialGroup();
        copy.setUuid(original.getUuid());
        copy.setExtId(original.getExtId());
        copy.setGroupHead(groupHeadStub);
        copy.setGroupName(original.getGroupName());
        copy.setLocation(locationStub);
        return copy;
    }

    public static Visit shallowCopyVisit(Visit original) {
        FieldWorker fieldworkerStub = FieldWorker.makeStub(original.getCollectedBy().getUuid(), original.getCollectedBy().getExtId());
        Location locationStub = Location.makeStub(original.getVisitLocation().getUuid(), original.getVisitLocation().getExtId());
        locationStub.setLocationHierarchy(null);

        Visit copy = new Visit();
        copy.setUuid(original.getUuid());
        copy.setCollectedBy(fieldworkerStub);
        copy.setVisitLocation(locationStub);
        copy.setExtId(original.getExtId());
        copy.setRoundNumber(original.getRoundNumber());
        copy.setVisitDate(original.getVisitDate());

        return copy;
    }

    public static Membership shallowCopyMembership(Membership membership) {
    	
        Membership copy = new Membership();
        copy.setUuid(membership.getUuid());
        copy.setbIsToA(membership.getbIsToA());
        copy.setStartDate(membership.getStartDate());
        copy.setStartType(membership.getStartType());
        copy.setEndDate(membership.getEndDate());
        copy.setEndType(membership.getEndType());
        FieldWorker fieldworkerStub = FieldWorker.makeStub(membership.getCollectedBy().getUuid(), membership.getCollectedBy().getExtId());
        copy.setCollectedBy(fieldworkerStub);
        Individual individualStub = Individual.makeStub(membership.getIndividual().getUuid(), membership.getIndividual().getExtId());
        copy.setIndividual(individualStub);
        SocialGroup sgStub = SocialGroup.makeStub(membership.getSocialGroup().getUuid(), membership.getSocialGroup().getExtId());
        copy.setSocialGroup(sgStub);
        return copy;
    }

    public static PregnancyOutcome shallowCopyPregnancyOutcome(PregnancyOutcome outcome) {
        PregnancyOutcome copy = new PregnancyOutcome();
        copy.setUuid(outcome.getUuid());
        copy.setOutcomeDate(outcome.getOutcomeDate());
        copy.setChildEverBorn(outcome.getChildEverBorn());
        copy.setNumberOfLiveBirths(outcome.getNumberOfLiveBirths());
        copy.setFather(Individual.makeStub(outcome.getFather().getUuid(), outcome.getFather().getExtId()));
        copy.setMother(Individual.makeStub(outcome.getMother().getUuid(), outcome.getMother().getExtId()));
        copy.setVisit(Visit.makeStub(outcome.getVisit().getUuid()));
        return copy;
    }
}
