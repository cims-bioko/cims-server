package org.openhds.controller.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.EntityService;
import org.openhds.controller.service.IndividualService;
import org.openhds.controller.service.PregnancyService;
import org.openhds.dao.service.GenericDao;
import org.openhds.dao.service.GenericDao.ValueProperty;
import org.openhds.domain.model.Individual;
import org.openhds.domain.model.Location;
import org.openhds.domain.model.Outcome;
import org.openhds.domain.model.PregnancyObservation;
import org.openhds.domain.model.PregnancyOutcome;
import org.openhds.domain.model.Residency;
import org.openhds.domain.service.SitePropertiesService;
import org.openhds.domain.util.CalendarUtil;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the pregnancy service interface
 * 
 * @author Dave
 *
 */
public class PregnancyServiceImpl implements PregnancyService {
	
	private EntityService entityService;
	private IndividualService individualService;
	private GenericDao genericDao;
	private SitePropertiesService siteProperties;
	private CalendarUtil calendarUtil;
	
	public PregnancyServiceImpl(EntityService entityService, IndividualService individualService, GenericDao genericDao, SitePropertiesService siteProperties, CalendarUtil calUtil) {
		this.entityService = entityService;
		this.individualService = individualService;
		this.genericDao = genericDao;
		this.siteProperties = siteProperties;
		calendarUtil = calUtil;
	}
	
	public PregnancyObservation evaluatePregnancyObservation(PregnancyObservation entityItem) throws ConstraintViolations {
    	
		int age = (int) (calendarUtil.daysBetween(entityItem.getMother().getDob(), entityItem.getExpectedDeliveryDate()) / 365.25);
		if (age  < siteProperties.getMinimumAgeOfPregnancy())
			throw new ConstraintViolations("The Mother specified is younger than the minimum age required to have a Pregnancy Observation.");	
		if (!checkDuplicatePregnancyObservation(entityItem.getMother())) 
    		throw new ConstraintViolations("The Mother specified already has a pending Pregnancy Observation.");	
    	if (individualService.getLatestEvent(entityItem.getMother()).equals("Death"))
    		throw new ConstraintViolations("A Pregnancy Observation cannot be created for a Mother who has a Death event.");	
    	
    	return entityItem;
	}
	
	public void validateGeneralPregnancyObservation(PregnancyObservation entityItem) throws ConstraintViolations {
		List<PregnancyObservation> list = genericDao.findListByMultiProperty(PregnancyObservation.class, getValueProperty("mother", entityItem.getMother()),																   									 getValueProperty("status", siteProperties.getDataStatusPendingCode()));
		if (list.size() > 1) 
    		throw new ConstraintViolations("The Mother specified already has a pending Pregnancy Observation.");	
	}
	
	@Transactional(rollbackFor=Exception.class)
	public void closePregnancyObservation(Individual mother) {
		List<PregnancyObservation> obs = genericDao.findListByProperty(PregnancyObservation.class, "mother", mother);
		
		for(PregnancyObservation ob : obs) {
			if (ob.getStatus().equals(siteProperties.getDataStatusPendingCode())) {
				// found the corresponding pregnancy observation
				// now close it
				ob.setStatus(siteProperties.getDataStatusClosedCode());
				genericDao.update(ob);
				break;
			}
		}
	}
	
	public boolean checkDuplicatePregnancyObservation(Individual mother) {
		List<PregnancyObservation> list = genericDao.findListByProperty(PregnancyObservation.class, "mother", mother);		
		for (PregnancyObservation item : list) {
			if (item.getStatus().equals(siteProperties.getDataStatusPendingCode()))
				return false;
		}
		return true;		
	}

	public PregnancyOutcome evaluatePregnancyOutcome(PregnancyOutcome entityItem) throws ConstraintViolations {
		
		int age;
		   
		if (entityItem.getOutcomeDate()==null) {
			age =  (int) (calendarUtil.daysBetween(entityItem.getMother().getDob(), entityItem.getVisit().getVisitDate()) / 365.25);
		} else {
			age = (int) (calendarUtil.daysBetween(entityItem.getMother().getDob(), entityItem.getOutcomeDate()) / 365.25);
		}
		if (age < siteProperties.getMinimumAgeOfPregnancy())
			throw new ConstraintViolations("The Mother specified is younger than the minimum age required to have a Pregnancy Outcome.");	
    	if (individualService.getLatestEvent(entityItem.getMother()).equals("Death"))
    		throw new ConstraintViolations("A Pregnancy Outcome cannot be created for a Mother who has a Death event.");	
    	if (entityItem.getOutcomes().size() == 0) 
    		throw new ConstraintViolations("A Pregnancy Outcome cannot be created unless it has at least 1 outcome.");
		if (entityItem.getMother().getCurrentResidency() == null) 
			throw new ConstraintViolations("A Pregnancy Outcome cannot be created because a Residency record cannot be found for the mother.");

		return entityItem;
	}
	
	public List<PregnancyOutcome> getPregnancyOutcomesByIndividual(Individual individual) {
		return genericDao.findListByProperty(PregnancyOutcome.class, "mother", individual, true);
	}

	public List<PregnancyObservation> getPregnancyObservationByIndividual(Individual individual) {
		return genericDao.findListByProperty(PregnancyObservation.class, "mother", individual, true);
	}

    public PregnancyOutcome getPregnancyOutcomeByUuid(String uuid) {
        return genericDao.findByProperty(PregnancyOutcome.class, "uuid", uuid);
    }

    @Transactional(rollbackFor=Exception.class)
	public void createPregnancyOutcome(PregnancyOutcome pregOutcome) throws ConstraintViolations {	
		Location motherLocation = pregOutcome.getMother().getCurrentResidency().getLocation();

		int totalEverBorn = 0;
		int liveBirths = 0;

        int numberOfOutcomes = pregOutcome.getOutcomes().size();
		for(int i = 0; i < numberOfOutcomes; i++) {

            Outcome outcome = pregOutcome.getOutcomes().get(i);

			totalEverBorn++;
			if (!outcome.getType().equals(siteProperties.getLiveBirthCode())) {
				// not a live birth so individual, residency and membership not needed
				continue;
			}
			
			liveBirths++;
			// create individual
			try {
			    outcome.getChild().setDob(pregOutcome.getOutcomeDate());
                entityService.create(outcome.getChild());
            } catch (IllegalArgumentException e) {
				throw new ConstraintViolations("IllegalArgumentException creating child individual in the database");
			} catch (SQLException e) {
                throw new ConstraintViolations("SQLException creating child individual in the database");
            }
			
			// use mothers location for the residency
			Residency residency = new Residency();
			residency.setStartDate(pregOutcome.getOutcomeDate());
			residency.setIndividual(outcome.getChild());
			residency.setStartType(siteProperties.getBirthCode());
			residency.setLocation( motherLocation );
			residency.setCollectedBy(pregOutcome.getCollectedBy());
			residency.setEndType(siteProperties.getNotApplicableCode());
			
			try {
                entityService.create(residency);
            } catch (IllegalArgumentException e) {
				throw new ConstraintViolations("IllegalArgumentException creating residency for child in database");
			} catch (SQLException e) {
                throw new ConstraintViolations("SQLException creating residency for child in database");
            }

			// persist membership
			try {
                entityService.create(outcome.getChildMembership());
            } catch (IllegalArgumentException e) {
				throw new ConstraintViolations("IllegalArgumentException creating membership for child in database");
			} catch (SQLException e) {
                throw new ConstraintViolations("SQLException creating membership for child in database");
            }
		}
		
		pregOutcome.setChildEverBorn(totalEverBorn);
		pregOutcome.setNumberOfLiveBirths(liveBirths);
		
		// close any pregnancy observation
		closePregnancyObservation(pregOutcome.getMother());

        PregnancyOutcome persistedPregnancyOutcome = getPregnancyOutcomeByUuid(pregOutcome.getUuid());

        if (null == persistedPregnancyOutcome) {
            try {
                entityService.create(pregOutcome);
            } catch (SQLException e) {
                throw new ConstraintViolations("Problem creating pregnancy outcome to database");
            }
        } else {
            try {
                entityService.save(pregOutcome);
            } catch (IllegalArgumentException e) {
				throw new ConstraintViolations("IllegalArgumentException saving pregnancy outcome in the database");
			} catch (SQLException e) {
                throw new ConstraintViolations("SQLException saving pregnancy outcome in the database");
            }
        }

	}
		
	public List<PregnancyOutcome> findAllLiveBirthsBetweenInterval(Calendar startDate, Calendar endDate) {
		
		List<PregnancyOutcome> output = new ArrayList<>();
		List<PregnancyOutcome> outcomes = genericDao.findAll(PregnancyOutcome.class, true);
		
		for (PregnancyOutcome outcome : outcomes) {			
			Calendar outcomeDate = outcome.getOutcomeDate();
			if ((outcomeDate.after(startDate) || outcomeDate.equals(startDate)) && 
					(outcomeDate.before(endDate))) {
				
				List<Outcome> allOutcomes = outcome.getOutcomes();
				for (Outcome o : allOutcomes) 
					if (o.getType().equals(siteProperties.getLiveBirthCode())) {
						output.add(outcome);
				}
			}
		}
		return output;
	}
	
	public int findAllBirthsBetweenIntervalByGender(Calendar startDate, Calendar endDate, int flag) {
		
		int count = 0;
		List<PregnancyOutcome> outcomes = genericDao.findAll(PregnancyOutcome.class, true);
		
		for (PregnancyOutcome outcome : outcomes) {			
			Calendar outcomeDate = outcome.getOutcomeDate();
			if ((outcomeDate.after(startDate) || outcomeDate.equals(startDate)) && 
					(outcomeDate.before(endDate))) {
				
				List<Outcome> allOutcomes = outcome.getOutcomes();
				for (Outcome o : allOutcomes) {		
					if (o.getType().equals(siteProperties.getLiveBirthCode())) {
						// male
						if (flag == 0) {
							if (o.getChild().getGender().equals(siteProperties.getMaleCode())) {
								if (o.getType().equals(siteProperties.getLiveBirthCode())) {
									count++;
								}
							}
						}
						// female
						else {
							if (o.getChild().getGender().equals(siteProperties.getFemaleCode())) {
								if (o.getType().equals(siteProperties.getLiveBirthCode())) {
									count++;
								}
							}
						}
					}
				}
			}
		}
		return count;
	}
		
	private ValueProperty getValueProperty(final String propertyName, final Object propertyValue) {
		return new ValueProperty() {

            public String getPropertyName() {
                return propertyName;
            }

            public Object getValue() {
                return propertyValue;
            }
        };		
	}

    @Override
    @Transactional(rollbackFor=Exception.class)
    public void createPregnancyObservation(PregnancyObservation pregObs) throws ConstraintViolations {
        evaluatePregnancyObservation(pregObs);
        
        try {
            entityService.create(pregObs);
        } catch (IllegalArgumentException e) {
        } catch (SQLException e) {
            throw new ConstraintViolations("There was a problem saving the pregnancy observation to the database");
        }
    }
}