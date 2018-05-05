package com.codedrinker.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by codedrinker on 07/07/2017.
 */
@Controller
public class EmbedController {
    private final Logger logger = LoggerFactory.getLogger(EmbedController.class);
    private LuckyMoneyCache luckyMoneyCache = new LuckyMoneyCache();

    @RequestMapping(value = "/embed", method = RequestMethod.GET)
    public String embed(Model model, @RequestParam() String url) {
        logger.info("access url -> {}", url);
        luckyMoneyCache.incCount(url);
        model.addAttribute("src", url);
        return "embed";
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Integer> count() {
        Map<String, Integer> currentCache = luckyMoneyCache.getCurrentCache();
        return currentCache;
    }
}
