package com.enotary.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Forwards client-side SPA routes to root so React Router can handle page navigation.
 */
@Controller
public class SpaForwardingController {

    @RequestMapping(value = {
            "/login",
            "/register",
            "/slots",
            "/slots/**",
            "/bookings",
            "/bookings/**",
            "/dashboard",
            "/dashboard/**"
    })
    public String forwardSpaRoutes() {
        return "forward:/";
    }
}
