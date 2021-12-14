package com.kimcompay.projectjb.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class restController {
    private Logger logger=LoggerFactory.getLogger(restController.class);

    @RequestMapping(value = "/test/**",method = RequestMethod.GET)
    public void name() {
        logger.info("test");
    }
}
