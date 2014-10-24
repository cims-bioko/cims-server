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

public class ShallowCopier {

    private static final Logger logger = LoggerFactory
            .getLogger(ShallowCopier.class);

    public static FieldWorker shallowCopyFieldWorker(FieldWorker fieldWorker) {
        FieldWorker copy = new FieldWorker();
        copy.setIdPrefix(fieldWorker.getIdPrefix());
        copy.setExtId(fieldWorker.getExtId());
        copy.setFirstName(fieldWorker.getFirstName());
        copy.setLastName(fieldWorker.getLastName());
        copy.setPasswordHash(fieldWorker.getPasswordHash());
        return copy;
    }

    public static Residency shallowCopyResidency(Residency residency) {
        Residency copy = new Residency();
        copy.setCollectedBy(residency.getCollectedBy());
        FieldWorker fieldWorkerStub = FieldWorker.makeStub(residency.getCollectedBy().getExtId());
        copy.setCollectedBy(fieldWorkerStub);
        Individual individualStub = Individual.makeStub(residency.getIndividual().getExtId());
        copy.setIndividual(individualStub);
        Location locationStub = Location.makeStub(residency.getLocation().getExtId());
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
            copy.setDob(individual.getDob());
            copy.setExtId(individual.getExtId());

            copy.setFather(copyExtId(individual.getFather()));
            copy.setFirstName(individual.getFirstName());
            copy.setGender(individual.getGender());
            copy.setLastName(individual.getLastName());
            String middleName = individual.getMiddleName() == null ? ""
                    : individual.getMiddleName();
            copy.setMiddleName(middleName);
            copy.setMother(copyExtId(individual.getMother()));

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

            Individual individualStub = Individual.makeStub(individual.getExtId());

            for (Membership membership : individual.getAllMemberships()) {
                SocialGroup socialGroupStub = SocialGroup.makeStub(membership
                        .getSocialGroup().getExtId());
                Membership membershipStub = Membership
                        .makeStub(socialGroupStub, individualStub,
                                membership.getbIsToA());
                copy.getAllMemberships().add(membershipStub);
            }

            Residency currentResidency = individual.getCurrentResidency();
            if (null != currentResidency) {
                Location locationStub = Location.makeStub(currentResidency.getLocation().getExtId());
                locationStub.setLocationHierarchy(null);
                Residency residencyStub = Residency.makeStub(locationStub,individualStub);
                residencyStub.setEndType(currentResidency.getEndType());
                copy.getAllResidencies().add(residencyStub);
            }
        } catch (Exception e) {
            System.out.println(copy.getExtId());
        }
        return copy;
    }

    private static Individual copyExtId(Individual individual) {
        Individual copy = new Individual();
        if (individual == null) {
            logger.warn("Individual had a null father or mother - using UNK as default value");
            copy.setExtId("UNK");
        } else {
            copy.setExtId(individual.getExtId());
        }

        return copy;
    }

    public static Location shallowCopyLocation(Location loc) {
        Location copy = new Location();

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

        LocationHierarchy hierarchy = new LocationHierarchy();
        hierarchy.setExtId(loc.getLocationHierarchy().getExtId());
        copy.setLocationHierarchy(hierarchy);

        copy.setExtId(loc.getExtId());
        copy.setLocationName(loc.getLocationName());
        copy.setLocationType(loc.getLocationType());

        FieldWorker fieldworkerStub = FieldWorker.makeStub(loc.getCollectedBy().getExtId());
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
        copy.setaIsToB(original.getaIsToB());

        Individual individualStub = Individual.makeStub(original.getIndividualA().getExtId());
        copy.setIndividualA(individualStub);

        individualStub = Individual.makeStub(original.getIndividualB().getExtId());
        copy.setIndividualB(individualStub);

        copy.setStartDate(original.getStartDate());
        copy.setEndDate(original.getEndDate());
        copy.setEndType(original.getEndType());
        return copy;
    }

    public static SocialGroup shallowCopySocialGroup(SocialGroup original) {
        Individual groupHeadStub = Individual.makeStub(original.getGroupHead().getExtId());
        SocialGroup copy = new SocialGroup();
        copy.setExtId(original.getExtId());
        copy.setGroupHead(groupHeadStub);
        copy.setGroupName(original.getGroupName());
        return copy;
    }

    public static Visit shallowCopyVisit(Visit original) {
        FieldWorker fieldworkerStub = FieldWorker.makeStub(original.getCollectedBy().getExtId());
        Location locationStub = Location.makeStub(original.getVisitLocation().getExtId());
        locationStub.setLocationHierarchy(null);

        Visit copy = new Visit();
        copy.setCollectedBy(fieldworkerStub);
        copy.setVisitLocation(locationStub);
        copy.setExtId(original.getExtId());
        copy.setRoundNumber(original.getRoundNumber());
        copy.setVisitDate(original.getVisitDate());

        return copy;
    }

    public static Membership shallowCopyMembership(Membership membership) {
    	
        Membership copy = new Membership();
        copy.setbIsToA(membership.getbIsToA());
        copy.setStartDate(membership.getStartDate());
        copy.setStartType(membership.getStartType());
        copy.setEndDate(membership.getEndDate());
        copy.setEndType(membership.getEndType());
        FieldWorker fieldworkerStub = FieldWorker.makeStub(membership.getCollectedBy().getExtId());
        copy.setCollectedBy(fieldworkerStub);
        Individual individualStub = Individual.makeStub(membership.getIndividual().getExtId());
        copy.setIndividual(individualStub);
        SocialGroup sgStub = SocialGroup.makeStub(membership.getSocialGroup().getExtId());
        copy.setSocialGroup(sgStub);
        return copy;
    }

    public static PregnancyOutcome shallowCopyPregnancyOutcome(PregnancyOutcome outcome) {
        PregnancyOutcome copy = new PregnancyOutcome();
        copy.setOutcomeDate(outcome.getOutcomeDate());
        copy.setChildEverBorn(outcome.getChildEverBorn());
        copy.setNumberOfLiveBirths(outcome.getNumberOfLiveBirths());
        copy.setFather(Individual.makeStub(outcome.getFather().getExtId()));
        copy.setMother(Individual.makeStub(outcome.getMother().getExtId()));
        copy.setVisit(Visit.makeStub(outcome.getVisit().getExtId()));
        return copy;
    }
}
