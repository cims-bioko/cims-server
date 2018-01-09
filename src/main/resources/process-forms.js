/**
 * The core form processing logic. The following script defines how submitted
 * forms will be processed by the server.
 */

var imports = new JavaImporter(
    java.io,
    javax.xml.transform.stream,
    com.github.cimsbioko.server.service,
    com.github.cimsbioko.server.formproc.forms
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
        jaxb = getBean('formMarshaller');
    }

    /**
     * Called by server. Allows us to send messages to server logs.
     */
    function setLogger(logger) {
        log = logger;
    }

    /**
     * Metadata for adapting raw odk form submissions to form processors.
     */
    var bindings = {
        spraying: {
            endpoint: SprayingFormProcessor.class
        },
        location: {
            endpoint: LocationFormProcessor.class,
            mapData: function(data) {
                var result = defaultDataMapper(bindings.location)(data);
                if (data.location) {
                    var form = result.locationForm, gps = toGPS(data.location);
                    form.latitude = gps.latitude;
                    form.longitude = gps.longitude;
                }
                return result;
            }
        },
        duplicate_location: {
            endpoint: DuplicateLocationFormProcessor.class,
            mapData: function(data) {
                var result = defaultDataMapper(bindings.duplicate_location)(data);
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
            endpoint: IndividualFormProcessor.class
        },
        create_map: {
            endpoint: CreateMapFormProcessor.class
        },
        create_sector: {
            endpoint: CreateSectorFormProcessor.class
        }
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
        return xmlToForm(objectToXml(mapFn(data)));
    }

    /**
     * Returns the conventional form name for the specified endpoint classes.
     */
    function formNameForEndpoint(endpoint) {
        return endpoint.getSimpleName().replace(
            /^([A-Z])(.*)Processor$/,
            function(match, initial, remaining) {
                return initial.toLowerCase() + remaining;
            });
    }

    /**
     * Returns a conventional data mapping function for the given binding.
     */
    function defaultDataMapper(binding) {
        return function (data) {
            var result = {};
            result[formNameForEndpoint(binding.endpoint)] = data;
            return result;
        }
    }

    /**
     * The default form processing method, used unless a specific one is specified by the binding.
     */
    function defaultProcess(binding, form) {
        var processor = getBean(binding.endpoint);
        var mappedForm = mapForm(binding.mapData || defaultDataMapper(binding), form.data);
        processor.processForm(mappedForm);
    }

    /**
     * Converts an xml document to an endpoint form using jaxb.
     */
    function xmlToForm(xml) {
        var reader = new StringReader(xml), source = new StreamSource(reader);
        return jaxb.unmarshal(source);
    }

    /**
     * Converts a javascript object to an xml string.
     */
    function objectToXml(data) {
        function validElementName(name) {
            return name.match(/^[a-zA-Z_]/) && !name.match(/xml/i) && name.match(/^[a-zA-Z0-9_.-]+$/);
        }
        var result = '';
        for (var key in data) {
            if (data.hasOwnProperty(key) && validElementName(key)) {
                var value = data[key];
                if (typeof(value) !== 'undefined') {
                    result += '<' + key + '>';
                    if (typeof(value) === 'object') {
                        result += objectToXml(value);
                    } else {
                        result += escapeXml('' + value);
                    }
                    result += '</' + key + '>';
                }
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