package com.github.cimsbioko.server.webapi;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.github.cimsbioko.server.webapi.RoundResource.ROUND_FORM_PATH;
import static java.util.Arrays.asList;

@Controller
@RequestMapping(ROUND_FORM_PATH)
public class RoundResource {

    public static final String ROUND_FORM_PATH = "/rest/rounds";

    private final Rounds result;

    public RoundResource() {
        result = new Rounds();
        result.setRounds(asList(new Round("d96120960c3f11e49059b2227cce2b54", 1)));
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Rounds getAllRounds() {
        return result;
    }

    public static class Round {

        String uuid;
        Integer roundNumber;

        Round(String uuid, Integer roundNumber) {
            this.uuid = uuid;
            this.roundNumber = roundNumber;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public Integer getRoundNumber() {
            return roundNumber;
        }

        public void setRoundNumber(Integer roundNumber) {
            this.roundNumber = roundNumber;
        }
    }

    @XmlRootElement
    public static class Rounds {
        private List<Round> rounds;
        @XmlElement(name = "round")
        public List<Round> getRounds() {
            return rounds;
        }
        public void setRounds(List<Round> rounds) {
            this.rounds = rounds;
        }
    }
}
