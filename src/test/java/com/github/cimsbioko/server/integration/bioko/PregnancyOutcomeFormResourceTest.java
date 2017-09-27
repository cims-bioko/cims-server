package com.github.cimsbioko.server.integration.bioko;

import com.github.cimsbioko.server.dao.GenericDao;
import com.github.cimsbioko.server.domain.model.PregnancyOutcome;
import com.github.cimsbioko.server.domain.util.CalendarAdapter;
import com.github.cimsbioko.server.integration.AbstractResourceTest;
import com.github.cimsbioko.server.integration.util.WebContextLoader;
import com.github.cimsbioko.server.webapi.PregnancyOutcomeFormResource;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.github.cimsbioko.server.domain.model.Individual;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.util.List;

import static com.github.cimsbioko.server.webapi.PregnancyOutcomeFormResource.CORE_FORM_PATH;
import static com.github.cimsbioko.server.webapi.PregnancyOutcomeFormResource.OUTCOMES_FORM_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(loader = WebContextLoader.class, locations = {"/testContext.xml"})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup(value = "/formResourceTestDb.xml", type = DatabaseOperation.REFRESH)
@ActiveProfiles("test")
public class PregnancyOutcomeFormResourceTest extends AbstractResourceTest {

    private static final String A_DATE = "2000-01-01T00:00:00-05:00";

    private static final String OUTCOME_CORE_FORM_XML =
            "<pregnancyOutcomeCoreForm>" +
                    "    <pregnancy_outcome_uuid>123123123123123123</pregnancy_outcome_uuid>" +
                    "    <collection_date_time>2015-04-09 14:49:46</collection_date_time>" +
                    "    <field_worker_ext_id>FWEK1D</field_worker_ext_id>" +
                    "    <field_worker_uuid>FWEK1D</field_worker_uuid>" +
                    "    <visit_uuid>TestVisit</visit_uuid>" +
                    "    <mother_uuid>PregnantIndividual</mother_uuid>" +
                    "    <father_uuid>PregnantMale</father_uuid>" +
                    "    <processed_by_mirth>null</processed_by_mirth>" +
                    "    <number_of_outcomes>1</number_of_outcomes>" +
                    "    <delivery_date>2000-01-01T00:00:00-05:00</delivery_date>" +
                    "</pregnancyOutcomeCoreForm>";

    private static final String OUTCOME_CHILD_FORM_XML =
            "<pregnancyOutcomeOutcomesForm>" +
                    "<pregnancy_outcome_uuid>123123123123123123</pregnancy_outcome_uuid>" +
                    "    <child_last_name>Last</child_last_name>" +
                    "    <outcome_type>LBR</outcome_type>" +
                    "    <child_gender>MALE</child_gender>" +
                    "    <collection_date_time>2015-04-09 14:49:46</collection_date_time>" +
                    "    <socialgroup_uuid>SocialGroup3</socialgroup_uuid>" +
                    "    <child_nationality>Other African Country</child_nationality>" +
                    "    <child_relationship_to_group_head>3</child_relationship_to_group_head>" +
                    "    <child_uuid>childUuid</child_uuid>" +
                    "    <child_middle_name>Middle</child_middle_name>" +
                    "    <processed_by_mirth>null</processed_by_mirth>" +
                    "    <child_first_name>First</child_first_name>" +
                    "</pregnancyOutcomeOutcomesForm>";

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
    public void testUnmarshalCoreXml() throws Exception {
        JAXBContext context = JAXBContext.newInstance(PregnancyOutcomeFormResource.CoreForm.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        unmarshaller.setAdapter(adapter);
        Object object = unmarshaller.unmarshal(new ByteArrayInputStream(OUTCOME_CORE_FORM_XML.getBytes()));
        PregnancyOutcomeFormResource.CoreForm form = (PregnancyOutcomeFormResource.CoreForm) object;
    }

    @Test
    public void testUnmarshalChildXml() throws Exception {
        JAXBContext context = JAXBContext.newInstance(PregnancyOutcomeFormResource.OutcomesForm.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        unmarshaller.setAdapter(adapter);
        Object object = unmarshaller.unmarshal(new ByteArrayInputStream(OUTCOME_CHILD_FORM_XML.getBytes()));
        PregnancyOutcomeFormResource.OutcomesForm form = (PregnancyOutcomeFormResource.OutcomesForm) object;
    }

    @Test
    public void testPostPregnancyOutcomeParentForm() throws Exception {
        mockMvc.perform(
                post(CORE_FORM_PATH).session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .content(OUTCOME_CORE_FORM_XML))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));

        verifyParentCrud("123123123123123123");
    }

    private void verifyParentCrud(String pregnancyOutcomeUuid) {

        PregnancyOutcome pregnancyOutcome = genericDao.findByProperty(PregnancyOutcome.class, "uuid", pregnancyOutcomeUuid);
        assertEquals(0, pregnancyOutcome.getOutcomes().size());

    }

    @Test
    public void testPostPregnancyOutcomeForm() throws Exception {
        mockMvc.perform(
                post(CORE_FORM_PATH).session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .content(OUTCOME_CORE_FORM_XML))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));

        mockMvc.perform(
                post(OUTCOMES_FORM_PATH).session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .content(OUTCOME_CHILD_FORM_XML))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));

        verifyEntityCrud("childUuid");
    }


    private void verifyEntityCrud(String childUuid) {

        List<PregnancyOutcome> pregnancyOutcome = genericDao.findListByProperty(PregnancyOutcome.class, "uuid", "123123123123123123");
        assertNotNull(pregnancyOutcome);
        assertEquals(1, pregnancyOutcome.get(0).getOutcomes().size());
        Individual child = genericDao.findByProperty(Individual.class, "uuid", childUuid);
        assertNotNull(child);
        assertEquals("First", child.getFirstName());
        assertEquals("Mother-004", child.getExtId());

    }

}
