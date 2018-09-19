package com.github.cimsbioko.server.idgen;

import com.github.cimsbioko.server.dao.GenericDao;
import com.github.cimsbioko.server.exception.ConstraintViolations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Brian
 *         <p>
 *         An abstract class that's used as a template for generating id's.
 *         The idea is that generators for Location, Visit, Individual, etc
 *         will extend this class in order to provide more specific
 *         implementations for generating the id.
 *         <p>
 *         generateId(T entityItem) will build the external id for the entityItem provided.
 */
@Component("idGenerator")
public abstract class Generator<T> extends LuhnValidator {

    @Autowired
    protected IdSchemeResource resource;
    @Autowired
    protected GenericDao genericDao;

    // temp variable for storing parts of the id
    protected String extId;

    // if set to true, the system will automatically generate the id based on the id scheme
    // if set to false, the system will not manage id generation and the user will be responsible
    public boolean generated;

    public abstract String generateId(T entityItem) throws ConstraintViolations;

    public abstract String buildNumberWithBound(T entityItem, IdScheme scheme) throws ConstraintViolations;

    /**
     * Builds the number portion of the id and ensures that it's unique.
     *
     * @throws ConstraintViolations
     */
    public String buildNumber(Class<T> classType, String prefix, boolean checkDigit) {

        Integer suffixInt = 0;
        String numberGen;
        List<T> list;
        do {
            suffixInt++;
            numberGen = suffixInt.toString();
            String temp = prefix + numberGen;

            if (checkDigit) {
                String resultChar = generateCheckCharacter(temp).toString();
                temp = temp.concat(resultChar);
                numberGen = numberGen.concat(resultChar);
            }

            list = genericDao.findListByProperty(classType, "extId", temp);
        } while (list.size() > 0);

        return numberGen;
    }

    /**
     * Formats the id and ensures that it's valid.
     */
    final String formatProperString(String string, Integer filter) throws ConstraintViolations {

        String substring = removeAccents(string.substring(0, filter));

        Pattern pattern = Pattern.compile("[a-zA-Z0-9]+");
        Matcher matcher = pattern.matcher(substring);

        if (!matcher.matches())
            throw new ConstraintViolations(substring + " contains invalid characters that " +
                    "cannot be specified as part of the Id.");

        return substring.trim().toUpperCase();
    }

    private String removeAccents(String substring) {
        return Normalizer.normalize(substring, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    /**
     * Validate the id length against the idScheme.
     * This should be used especially if the id is being entered manually.
     */
    public final boolean validateIdLength(String id, IdScheme scheme) throws ConstraintViolations {
        if (scheme.length > 0 && id.length() != scheme.length)
            throw new ConstraintViolations(id + " is not of the required length as specified in the IdScheme. " +
                    "It must be " + scheme.length + " characters long.");
        return true;
    }

    public GenericDao getGenericDao() {
        return genericDao;
    }

    public void setGenericDao(GenericDao genericDao) {
        this.genericDao = genericDao;
    }

    public IdSchemeResource getResource() {
        return resource;
    }

    public void setResource(IdSchemeResource resource) {
        this.resource = resource;
    }

    public boolean isGenerated() {
        return generated;
    }

    public void setGenerated(boolean generated) {
        this.generated = generated;
    }
}
