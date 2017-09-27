package com.github.cimsbioko.server.webapi;

import com.github.cimsbioko.server.controller.service.RoundService;
import com.github.cimsbioko.server.domain.model.Round;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.github.cimsbioko.server.webapi.RoundResource.ROUND_FORM_PATH;

@Controller
@RequestMapping(ROUND_FORM_PATH)
public class RoundResource {

    public static final String ROUND_FORM_PATH = "/rest/rounds";

    private RoundService roundService;

    @Autowired
    public RoundResource(RoundService roundService) {
        this.roundService = roundService;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Round.Rounds getAllRounds() {
        Round.Rounds rounds = new Round.Rounds();
        rounds.setRounds(roundService.getAllRounds());
        return rounds;
    }

}
