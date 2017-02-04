package com.github.cimsbioko.server.webservice.resources;

import com.github.cimsbioko.server.controller.service.RoundService;
import com.github.cimsbioko.server.domain.model.Round;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/rounds")
public class RoundResource {

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
