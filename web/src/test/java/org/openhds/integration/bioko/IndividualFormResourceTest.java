package org.openhds.integration.bioko;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.model.*;
import org.openhds.domain.util.CalendarAdapter;
import org.openhds.errorhandling.constants.ErrorConstants;
import org.openhds.integration.AbstractResourceTest;
import org.openhds.integration.util.WebContextLoader;
import org.openhds.webservice.resources.bioko.IndividualFormResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.web.server.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(loader = WebContextLoader.class, locations = { "/testContext.xml" })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class })
@DatabaseSetup(value = "/individualFormResourceDb.xml", type = DatabaseOperation.REFRESH)
public class IndividualFormResourceTest extends AbstractResourceTest {

    private static final String A_DATE = "2000-01-01T00:00:00-05:00";
    private static final String FORMATTED_DATE = "2013-06-13";
    private static final String FORMATTED_DATETIME = "2013-06-13 12:12:12";

    private static final String HEAD_OF_HOUSEHOLD_FORM_XML = "<individualForm>"
            + "<processed_by_mirth>false</processed_by_mirth>"
            + "<field_worker_ext_id>FWEK1D</field_worker_ext_id>" + "<collection_date_time>"
            + A_DATE
            + "</collection_date_time>"
            + "<field_worker_uuid>FWEK1D-UUID</field_worker_uuid>"
            + "<entity_uuid>32145678901234935890123456789012</entity_uuid>"
            + "<household_ext_id>newHouse_id</household_ext_id>"
            + "<household_uuid>NONHOUSEHOLDLOCATION</household_uuid>"
            + "<individual_ext_id>newHouse_id-001</individual_ext_id>"
            + "<individual_first_name>Test HoH First</individual_first_name>"
            + "<individual_last_name>Test HoH Last</individual_last_name>"
            + "<individual_other_names>Test HoH Other</individual_other_names>"
            + "<individual_age>100</individual_age>"
            + "<individual_age_units>years</individual_age_units>"
            + "<individual_date_of_birth>"
            + A_DATE
            + "</individual_date_of_birth>"
            + "<individual_gender>MALE</individual_gender>"
            + "<individual_relationship_to_head_of_household>1</individual_relationship_to_head_of_household>"
            + "<relationship_uuid>HEADSELFRELATIONSHIP</relationship_uuid>"
            + "<socialgroup_uuid>NEWSOCIALGROUP</socialgroup_uuid>"
            + "<membership_uuid>NEWMEMBERSHIP</membership_uuid>"
            + "<individual_phone_number>12345678890</individual_phone_number>"
            + "<individual_other_phone_number>0987654321</individual_other_phone_number>"
            + "<individual_language_preference>English</individual_language_preference>"
            + "<individual_point_of_contact_name></individual_point_of_contact_name>"
            + "<individual_point_of_contact_phone_number></individual_point_of_contact_phone_number>"
            + "<individual_dip>12345</individual_dip>"
            + "<individual_member_status>permanent</individual_member_status>"
            + "</individualForm>";
    private static final String MEMBER_OF_HOUSEHOLD_FORM_XML = "<individualForm>"
            + "<processed_by_mirth>false</processed_by_mirth>"
            + "<field_worker_ext_id>FWEK1D</field_worker_ext_id>" + "<collection_date_time>"
            + A_DATE
            + "</collection_date_time>"
            + "<field_worker_uuid>FWEK1D-UUID</field_worker_uuid>"
            + "<entity_uuid>12345678901234935890123456789012</entity_uuid>"
            + "<household_ext_id>existing_id</household_ext_id>"
            + "<household_uuid>HOUSEHOLDLOCATION</household_uuid>"
            + "<individual_ext_id>existing_id-002</individual_ext_id>"
            + "<individual_first_name>Test Member First</individual_first_name>"
            + "<individual_last_name>Test Member Last</individual_last_name>"
            + "<individual_other_names>Test Member Other</individual_other_names>"
            + "<individual_age>100</individual_age>"
            + "<individual_age_units>years</individual_age_units>"
            + "<individual_date_of_birth>"
            + A_DATE
            + "</individual_date_of_birth>"
            + "<individual_gender>FEMALE</individual_gender>"
            + "<individual_relationship_to_head_of_household>2</individual_relationship_to_head_of_household>"
            + "<relationship_uuid>MEMBERRELATIONSHIP</relationship_uuid>"
            + "<socialgroup_uuid>HOUSEHOLDSOCIALGROUP</socialgroup_uuid>"
            + "<membership_uuid>ANOTHERMEMBERSHIP</membership_uuid>"
            + "<individual_phone_number>12345678890</individual_phone_number>"
            + "<individual_other_phone_number>0987654321</individual_other_phone_number>"
            + "<individual_language_preference>English</individual_language_preference>"
            + "<individual_point_of_contact_name></individual_point_of_contact_name>"
            + "<individual_point_of_contact_phone_number></individual_point_of_contact_phone_number>"
            + "<individual_dip>12345</individual_dip>"
            + "<individual_member_status>permanent</individual_member_status>"
            + "</individualForm>";
    private static final String DUPLICATE_EXTID_MEMBER_OF_HOUSEHOLD_FORM_XML = "<individualForm>"
            + "<processed_by_mirth>false</processed_by_mirth>"
            + "<field_worker_ext_id>FWEK1D</field_worker_ext_id>" + "<collection_date_time>"
            + A_DATE
            + "</collection_date_time>"
            + "<field_worker_uuid>FWEK1D-UUID</field_worker_uuid>"
            + "<entity_uuid>1234567890133335890123456789012</entity_uuid>"
            + "<household_ext_id>existing_id</household_ext_id>"
            + "<household_uuid>HOUSEHOLDLOCATION</household_uuid>"
            + "<individual_ext_id>existing_id-002</individual_ext_id>"
            + "<individual_first_name>Test Member First</individual_first_name>"
            + "<individual_last_name>Test Member Last</individual_last_name>"
            + "<individual_other_names>Test Member Other</individual_other_names>"
            + "<individual_age>100</individual_age>"
            + "<individual_age_units>years</individual_age_units>"
            + "<individual_date_of_birth>"
            + A_DATE
            + "</individual_date_of_birth>"
            + "<individual_gender>FEMALE</individual_gender>"
            + "<individual_relationship_to_head_of_household>2</individual_relationship_to_head_of_household>"
            + "<relationship_uuid>123MEMBERRELATIONSHIP</relationship_uuid>"
            + "<socialgroup_uuid>HOUSEHOLDSOCIALGROUP</socialgroup_uuid>"
            + "<membership_uuid>123ANOTHERMEMBERSHIP</membership_uuid>"
            + "<individual_phone_number>12345678890</individual_phone_number>"
            + "<individual_other_phone_number>0987654321</individual_other_phone_number>"
            + "<individual_language_preference>English</individual_language_preference>"
            + "<individual_point_of_contact_name></individual_point_of_contact_name>"
            + "<individual_point_of_contact_phone_number></individual_point_of_contact_phone_number>"
            + "<individual_dip>12345</individual_dip>"
            + "<individual_member_status>permanent</individual_member_status>"
            + "</individualForm>";
    private static final String INDIVIDUAL_FORM_INCOMPLETE = "<individualForm>"
            + "<individual_first_name>Test First</individual_first_name>"
            + "<individual_last_name>Test Last</individual_last_name>"
            + "<individual_other_names>Test Other</individual_other_names>"
            + "<individual_age>10</individual_age>"
            + "<individual_age_units>years</individual_age_units>" + "</individualForm>";
    private static final String MEMBER_OF_HOUSEHOLD_OUTDATED_EXTID_FORM_XML = "<individualForm>"
            + "<processed_by_mirth>false</processed_by_mirth>"
            + "<field_worker_ext_id>FWEK1D</field_worker_ext_id>" + "<collection_date_time>"
            + A_DATE
            + "</collection_date_time>"
            + "<field_worker_uuid>FWEK1D-UUID</field_worker_uuid>"
            + "<entity_uuid>12345678901234935890123456789012</entity_uuid>"
            + "<household_ext_id>existing_idOUTDATED</household_ext_id>"
            + "<household_uuid>HOUSEHOLDLOCATION</household_uuid>"
            + "<individual_ext_id>existing_idOUTDATED-002</individual_ext_id>"
            + "<individual_first_name>First</individual_first_name>"
            + "<individual_last_name>Last</individual_last_name>"
            + "<individual_other_names>Other</individual_other_names>"
            + "<individual_age>66</individual_age>"
            + "<individual_age_units>years</individual_age_units>"
            + "<individual_date_of_birth>"
            + A_DATE
            + "</individual_date_of_birth>"
            + "<individual_gender>FEMALE</individual_gender>"
            + "<individual_relationship_to_head_of_household>2</individual_relationship_to_head_of_household>"
            + "<relationship_uuid>MEMBERRELATIONSHIP</relationship_uuid>"
            + "<socialgroup_uuid>HOUSEHOLDSOCIALGROUP</socialgroup_uuid>"
            + "<membership_uuid>ANOTHERMEMBERSHIP</membership_uuid>"
            + "<individual_phone_number>12345678890</individual_phone_number>"
            + "<individual_other_phone_number>0987654321</individual_other_phone_number>"
            + "<individual_language_preference>English</individual_language_preference>"
            + "<individual_point_of_contact_name></individual_point_of_contact_name>"
            + "<individual_point_of_contact_phone_number></individual_point_of_contact_phone_number>"
            + "<individual_dip>12345</individual_dip>"
            + "<individual_member_status>permanent</individual_member_status>"
            + "</individualForm>";

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private CalendarAdapter adapter;

    private MockHttpSession session;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        mockMvc = buildMockMvc();
        session = getMockHttpSession("admin", "test", mockMvc);
    }

    @Test
    public void testUnmarshalXml() throws Exception {
        JAXBContext context = JAXBContext.newInstance(IndividualFormResource.Form.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        unmarshaller.setAdapter(adapter);
        Object object = unmarshaller.unmarshal(new ByteArrayInputStream(HEAD_OF_HOUSEHOLD_FORM_XML.getBytes(StandardCharsets.UTF_8)));
        IndividualFormResource.Form form = (IndividualFormResource.Form) object;
    }

    @Test
    public void testPostDuplicateExtId() throws Exception {

        mockMvc.perform(
                post("/individualForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(MEMBER_OF_HOUSEHOLD_FORM_XML.getBytes()))
                .andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        mockMvc.perform(
                post("/individualForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(DUPLICATE_EXTID_MEMBER_OF_HOUSEHOLD_FORM_XML.getBytes()))
                .andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        verifyEntityCrud("12345678901234935890123456789012", "existing_id", "Individual2", "2");

        ErrorLog error = genericDao.findByProperty(ErrorLog.class, "entityType", IndividualFormResource.Form.LOG_NAME);
        assertNotNull(error);
        assertEquals(ErrorConstants.DUPLICATE_EXTID, error.getResolutionStatus());
    }

    @Test
    public void testReusingExtIdAllowed() throws Exception {

        mockMvc.perform(
                post("/individualForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(MEMBER_OF_HOUSEHOLD_FORM_XML.getBytes()))
                .andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        ErrorLog error = genericDao.findByProperty(ErrorLog.class, "entityType", IndividualFormResource.Form.LOG_NAME);
        assertNull("no error should exist after processing first form", error);

        verifyEntityCrud("12345678901234935890123456789012", "existing_id", "Individual2", "2");

        Individual original = genericDao.findByProperty(Individual.class, "extId", "existing_id-002", true);
        assertNotNull("original individual should exist", original);

        // Mark the original individual as deleted
        original.setDeleted(true);
        original = genericDao.findByProperty(Individual.class, "extId", "existing_id-002", true);
        assertNull("original individual should be deleted", original);

        // Attempt to create a new individual with the same extid
        mockMvc.perform(
                post("/individualForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(DUPLICATE_EXTID_MEMBER_OF_HOUSEHOLD_FORM_XML.getBytes()))
                .andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        error = genericDao.findByProperty(ErrorLog.class, "entityType", IndividualFormResource.Form.LOG_NAME);
        assertNull("no error should exist after processing second form", error);

        verifyEntityCrud("1234567890133335890123456789012", "existing_id", "Individual2", "2");

        Individual second = genericDao.findByProperty(Individual.class, "extId", "existing_id-002", true);
        assertNotNull("second individual should exist with previously deleted extId", second);
        assertEquals("second individual uuid should match submitted uuid", "1234567890133335890123456789012", second.getUuid());
    }

    @Test
    public void testPostHeadOfHouseholdFormXml() throws Exception {
        mockMvc.perform(
                post("/individualForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(HEAD_OF_HOUSEHOLD_FORM_XML.getBytes()))
                .andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        verifyEntityCrud("32145678901234935890123456789012", "newHouse_id", "32145678901234935890123456789012", "1");
    }

    @Test
    public void testPostMemberOfHouseholdFormXml() throws Exception {
        mockMvc.perform(
                post("/individualForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(MEMBER_OF_HOUSEHOLD_FORM_XML.getBytes()))
                .andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        verifyEntityCrud("12345678901234935890123456789012", "existing_id", "Individual2", "2");
    }

    @Test
    public void testRepeatPostHeadOfHouseholdFormXml() throws Exception {
        mockMvc.perform(
                post("/individualForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(HEAD_OF_HOUSEHOLD_FORM_XML.getBytes()))
                .andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        mockMvc.perform(
                post("/individualForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(HEAD_OF_HOUSEHOLD_FORM_XML.getBytes()))
                .andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        verifyEntityCrud("32145678901234935890123456789012", "newHouse_id", "32145678901234935890123456789012", "1");
    }

    @Test
    public void testRepeatPostMemberOfHouseholdFormXml() throws Exception {
        mockMvc.perform(
                post("/individualForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(MEMBER_OF_HOUSEHOLD_FORM_XML.getBytes()))
                .andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        mockMvc.perform(
                post("/individualForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(MEMBER_OF_HOUSEHOLD_FORM_XML.getBytes()))
                .andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        verifyEntityCrud("12345678901234935890123456789012", "existing_id", "Individual2", "2");
    }

    @Test
    public void testPostIncompleteIndividualFormXml() throws Exception {
        mockMvc.perform(
                post("/individualForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(INDIVIDUAL_FORM_INCOMPLETE.getBytes()))
                .andExpect(status().isBadRequest())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));
    }

    @Test
    public void testPostOutdatedIndividualExtIdFormXml() throws Exception {
        mockMvc.perform(
                post("/individualForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(MEMBER_OF_HOUSEHOLD_OUTDATED_EXTID_FORM_XML.getBytes()))
                .andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        verifyEntityCrud("12345678901234935890123456789012", "existing_id", "Individual2", "2");
    }

    private void verifyEntityCrud(String individualUuid, String householdExtId, String headUuid,
                                  String membershipType) {

        // individual exists
        Individual individual = genericDao.findByProperty(Individual.class, "uuid", individualUuid);
        assertNotNull(individual);

        // location exists
        Location location = genericDao.findByProperty(Location.class, "extId", householdExtId);
        assertNotNull(location);

        // residency at location
        Residency residency = null;
        for (Residency r : individual.getAllResidencies()) {
            if (r.getLocation().equals(location)) {
                residency = r;
                break;
            }
        }
        assertNotNull(residency);

        // socialGroup exists
        SocialGroup socialGroup = genericDao.findByProperty(SocialGroup.class, "extId", householdExtId);
        assertNotNull(socialGroup);

        // membership in social group
        Membership membership = null;
        for (Membership m : individual.getAllMemberships()) {
            if (m.getSocialGroup().equals(socialGroup)) {
                membership = m;
                break;
            }
        }
        assertNotNull(membership);
        assertEquals(membershipType, membership.getbIsToA());

        // head of household exists
        Individual head = genericDao.findByProperty(Individual.class, "uuid", headUuid);
        assertNotNull(head);

        // relationship to head
        // membership in social group
        Relationship relationship = null;
        for (Relationship r : individual.getAllRelationships1()) {
            if (r.getIndividualB().equals(head)) {
                relationship = r;
                break;
            }
        }
        assertNotNull(relationship);
        assertEquals(membershipType, relationship.getaIsToB());
    }

}
