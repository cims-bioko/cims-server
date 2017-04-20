/**
 * The core form processing logic. The following script defines how submitted
 * forms will be processed by the server.
 */

var imports = new JavaImporter(
    java.io,
    javax.xml.transform.stream,
    com.github.cimsbioko.server.controller.service,
    com.github.cimsbioko.server.webapi
);

with (imports) {

    var appCtx, log, jaxb, formService;

    /**
     * Convenience function for looking up application objects.
     */
    function getBean(o) {
        return appCtx.getBean(o);
    }

    /**
     * Called by the server upon initialization.
     */
    function setApplicationContext(ctx) {
        appCtx = ctx;
        formService = getBean(FormSubmissionService.class);
        jaxb = getBean('jaxbMarshaller');
    }

    /**
     * Called by server. Allows us to send messages to server logs.
     */
    function setLogger(logger) {
        log = logger;
    }

    /**
     * Metadata for adapting raw odk form submissions to existing form endpoints.
     */
    var bindings = {
        spraying: {
            endpoint: SprayingFormResource.class,
            mapData: function(data) {
                return {
                    sprayingForm: {
                        entity_uuid: data.entityUuid,
                        evaluation: data.evaluation
                    }
                };
            }
        },
        location: {
            endpoint: LocationFormResource.class,
            mapData: function(data) {
                var result = {
                    locationForm: {
                        entity_uuid: data.entityUuid,
                        entity_ext_id: data.entityExtId,
                        field_worker_uuid: data.fieldWorkerUuid,
                        field_worker_ext_id: data.fieldWorkerExtId,
                        collection_date_time: data.collectionDateTime,
                        hierarchy_ext_id: data.hierarchyExtId,
                        hierarchy_uuid: data.hierarchyUuid,
                        hierarchy_parent_uuid: data.hierarchyParentUuid,
                        location_ext_id: data.locationExtId,
                        location_name: data.locationName,
                        location_type: data.locationType,
                        community_name: data.communityName,
                        community_code: data.communityCode,
                        map_area_name: data.mapAreaName,
                        locality_name: data.localityName,
                        sector_name: data.sectorName,
                        location_building_number: data.locationBuildingNumber,
                        location_floor_number: data.locationFloorNumber,
                        description: data.description
                    }
                };
                if (data.location) {
                    var form = result.locationForm, gps = toGPS(data.location);
                    form.latitude = gps.latitude;
                    form.longitude = gps.longitude;
                }
                return result;
            }
        },
        duplicate_location: {
            endpoint: DuplicateLocationFormResource.class,
            mapData: function(data) {
                var result = {
                    duplicateLocationForm: {
                        entity_uuid: data.entityUuid,
                        action: data.action,
                        description: data.description
                    }
                };
                if (data.globalPosition) {
                    var form = result.duplicateLocationForm, gps = toGPS(data.globalPosition);
                    form.global_position_lat = gps.latitude;
                    form.global_position_lng = gps.longitude;
                    form.global_position_acc = gps.accuracy;
                }
                return result;
            }
        },
        death: {
            endpoint: DeathFormResource.class,
            mapData: function(data) {
                return {
                    deathForm: {
                        field_worker_uuid: data.fieldWorkerUuid,
                        individual_uuid: data.individualUuid,
                        visit_uuid: data.visitUuid,
                        date_of_death: data.dateOfDeath,
                        place_of_death: data.placeOfDeath,
                        cause_of_death: data.causeOfDeath
                    }
                };
            }
        },
        individual: {
            endpoint: IndividualFormResource.class,
            mapData: function(data) {
                return {
                    individualForm: {
                        entity_uuid: data.entityUuid,
                        field_worker_uuid: data.fieldWorkerUuid,
                        collection_date_time: data.collectionDateTime,
                        household_ext_id: data.householdExtId,
                        household_uuid: data.householdUuid,
                        membership_uuid: data.membershipUuid,
                        relationship_uuid: data.relationshipUuid,
                        socialgroup_uuid: data.socialgroupUuid,
                        individual_ext_id: data.individualExtId,
                        individual_first_name: data.individualFirstName,
                        individual_last_name: data.individualLastName,
                        individual_other_names: data.individualOtherNames,
                        individual_age: data.individualAge,
                        individual_age_units: data.individualAgeUnits,
                        individual_date_of_birth: data.individualDateOfBirth,
                        individual_gender: data.individualGender,
                        individual_relationship_to_head_of_household: data.individualRelationshipToHeadOfHousehold,
                        individual_phone_number: data.individualPhoneNumber,
                        individual_other_phone_number: data.individualOtherPhoneNumber,
                        individual_language_preference: data.individualLanguagePreference,
                        individual_point_of_contact_name: data.individualPointOfContactName,
                        individual_point_of_contact_phone_number: data.individualPointOfContactPhoneNumber,
                        individual_dip: data.individualDip,
                        individual_member_status: data.individualMemberStatus,
                        individual_nationality: data.individualNationality
                    }
                };
            }
        },
        in_migration: {
            endpoint: InMigrationFormResource.class,
            mapData: function(data) {
                return {
                    inMigrationForm: {
                        field_worker_uuid: data.fieldWorkerUuid,
                        visit_uuid: data.visitUuid,
                        location_uuid: data.locationUuid,
                        individual_uuid: data.individualUuid,
                        migration_type: data.migrationType,
                        migration_date: data.migrationDate,
                        migration_origin: data.migrationOrigin,
                        migration_reason: data.migrationReason
                    }
                };
            }
        },
        visit: {
            endpoint: VisitFormResource.class,
            mapData: function(data) {
                return {
                    visitForm: {
                        entity_uuid: data.entityUuid,
                        field_worker_uuid: data.fieldWorkerUuid,
                        visit_ext_id: data.visitExtId,
                        visit_uuid: data.visitUuid,
                        location_ext_id: data.locationExtId,
                        location_uuid: data.locationUuid,
                        visit_date: data.visitDate
                    }
                };
            }
        },
        out_migration: {
            endpoint: OutMigrationFormResource.class,
            mapData: function(data) {
                return {
                    outMigrationForm: {
                        field_worker_uuid: data.fieldWorkerUuid,
                        individual_uuid: data.individualUuid,
                        visit_uuid: data.visitUuid,
                        out_migration_date: data.outMigrationDate,
                        out_migration_name_of_destination: data.outMigrationNameOfDestination,
                        out_migration_reason: data.outMigrationReason
                    }
                };
            }
        },
        pregnancy_observation: {
            endpoint: PregnancyObservationFormResource.class,
            mapData: function(data) {
                return {
                    pregnancyObservationForm: {
                        field_worker_uuid: data.fieldWorkerUuid,
                        individual_uuid: data.individualUuid,
                        visit_uuid: data.visitUuid,
                        expected_delivery_date: data.expectedDeliveryDate,
                        recorded_date: data.recordedDate
                    }
                };
            }
        },
        pregnancy_outcome: {
            endpoint: PregnancyOutcomeFormResource.class,
            mapCoreData: function(data) {
                return {
                    pregnancyOutcomeCoreForm: {
                        pregnancy_outcome_uuid: data.pregnancyOutcomeUuid,
                        field_worker_uuid: data.fieldWorkerUuid,
                        visit_uuid: data.visitUuid,
                        mother_uuid: data.motherUuid,
                        father_uuid: data.fatherUuid,
                        delivery_date: data.deliveryDate
                    }
                };
            },
            mapOutcomeData: function(outcome) {
                return {
                    pregnancyOutcomeOutcomesForm: {
                        pregnancy_outcome_uuid: outcome.pregnancyOutcomeUuid,
                        collection_date_time: outcome.collectionDateTime,
                        socialgroup_uuid: outcome.socialgroupUuid,
                        outcome_type: outcome.outcomeType,
                        child_uuid: outcome.childUuid,
                        child_first_name: outcome.childFirstName,
                        child_middle_name: outcome.childMiddleName,
                        child_last_name: outcome.childLastName,
                        child_gender: outcome.childGender,
                        child_relationship_to_group_head: outcome.childRelationshipToGroupHead,
                        child_nationality: outcome.childNationality
                    }
                };
            },
            process: function(binding, data) {

                var endpoint = getBean(binding.endpoint);

                function processCore(d) {
                    print('processing core');
                    endpoint.processCoreForm(mapForm(binding.mapCoreData, d));
                }

                function processOutcomes(outcomes) {
                    if (outcomes) {
                        print('processing ' + outcomes.length + ' outcomes');
                        for (var o=0; o<outcomes.length; o++) {
                            endpoint.processOutcomesForm(mapForm(binding.mapOutcomeData, outcomes[o]));
                        }
                    }
                }

                processCore(data);
                processOutcomes(data.outcomes);
            }
        }
    };

    /**
     * This processing entry point is called periodically by the server (every 30s).
     */
    function processForms() {
        var batchSize = 300, forms = formService.getUnprocessed(batchSize), processed = 0, failures = 0;
        for (var f = 0; f < forms.length; f++) {
            var submission = forms[f];
            try {
                process(submission);
            } catch (error) {
                error.printStackTrace();
                failures += 1;
            }
            formService.markProcessed(submission);
            processed += 1;
        }
        print('processing completed with ' + failures + ' failures');
        return processed;
    }

    /**
     * Processes a single form submission.
     */
    function process(submission) {
        var form = JSON.parse(submission.json),
            possibleBindings = [submission.getFormBinding(), submission.getFormId()],
            instanceId = submission.getInstanceId(),
            data = form.data;
        for (var b=0; b<possibleBindings.length; b++) {
            var bindingName = possibleBindings[b], binding = bindings[bindingName];
            if (binding) {
                print('processing form ' + instanceId + ' with binding ' + bindingName);
                (binding.process || defaultProcess)(binding, data);
                return;
            }
        }
        print('not processing form ' + instanceId + ', no binding found');
    }

    /**
     * Generic form mapping function.
     */
    function mapForm(mapFn, data) {
        return toForm(toXml(mapFn(data)));
    }

    /**
     * The default form processing method, used unless a specific one is specified by the binding.
     */
    function defaultProcess(binding, data) {
        getBean(binding.endpoint).processForm(mapForm(binding.mapData, data));
    }

    /**
     * Converts an xml document to an endpoint form using jaxb.
     */
    function toForm(xml) {
        var reader = new StringReader(xml), source = new StreamSource(reader);
        return jaxb.unmarshal(source);
    }

    /**
     * Converts a javascript object to an xml string.
     */
    function toXml(data) {
        var result = '';
        for (var field in data) {
            var value = data[field];
            if (typeof(value) !== 'undefined') {
                result += '<' + field + '>';
                if (typeof(value) === 'object') {
                    result += toXml(value);
                } else {
                    result += escapeXml('' + value);
                }
                result += '</' + field + '>';
            }
        }
        return result;
    }

    /**
     * Escapes a string value so it can be safely inserted into an XML document.
     */
    function escapeXml(unsafe) {
        return unsafe.replace(/[<>&'"]/g, function (c) {
            switch (c) {
                case '<': return '&lt;';
                case '>': return '&gt;';
                case '&': return '&amp;';
                case '\'': return '&apos;';
                case '"': return '&quot;';
            }
        });
    }

    /**
     * Converts ODK gps string values into objects.
     */
    function toGPS(gpsString) {
        var gps = gpsString.split(' ');
        return {
            latitude: gps[0],
            longitude: gps[1],
            altitude: gps[2],
            accuracy: gps[3]
        };
    }
}
