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

    var appCtx, log, jaxb, formService, batchSize = 300;

    /**
     * Convenience function for looking up application objects.
     */
    function getBean(o) {
        return appCtx.getBean(o);
    }

    /**
     * Called by server. Allows us to lookup and interact with application services.
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
    };

    /**
     * This processing entry point is called periodically by the server (every 30s).
     */
    function processForms() {
        var forms = formService.getUnprocessed(batchSize), failures = 0;
        for (var f = 0; f < forms.length; f++) {
            var submission = forms[f], processedOk = false;
            try {
                process(submission);
                processedOk = true;
            } catch (e) {
                log.error("failed to process submission", e);
                failures++;
            }
            formService.markProcessed(submission, processedOk);
        }
        if (failures > 0) {
            log.warn('processing completed with {} failures', failures.toFixed());
        }
        return f;
    }

    /**
     * Deduplicates a list while maintaining initial ordering.
     */
    function unique(list) {
        return list.filter(
            function(element, index, array) {
                return array.indexOf(element) === index;
            }
        );
    }

    /**
     * Processes a single form submission.
     */
    function process(submission) {
        var instanceId = submission.getInstanceId(),
            possibleBindingNames = unique([submission.getFormBinding(), submission.getFormId()]);
        for (var bn=0; bn<possibleBindingNames.length; bn++) {
            var bindingName = possibleBindingNames[bn], binding = bindings[bindingName];
            if (binding) {
                log.info('processing {} (binding: {})', instanceId, bindingName);
                var form = JSON.parse(submission.json);
                (binding.process || defaultProcess)(binding, form);
                return;
            }
        }
        log.info('processing {} (binding: none), tried [{}]', instanceId, possibleBindingNames.join());
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
    function defaultProcess(binding, form) {
        getBean(binding.endpoint).processForm(mapForm(binding.mapData, form.data));
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
