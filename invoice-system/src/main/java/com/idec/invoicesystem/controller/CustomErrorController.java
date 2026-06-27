package com.idec.invoicesystem.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.boot.webmvc.error.ErrorController;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model, Authentication auth) {
        Object statusObj  = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message    = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception  = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        int status = statusObj != null ? Integer.parseInt(statusObj.toString()) : 500;
        HttpStatus httpStatus = HttpStatus.resolve(status);
        String title = httpStatus != null ? httpStatus.getReasonPhrase() : "Error";

        String desc;
        if (exception != null && exception.toString().contains("Mongo")) {
            desc = "The database is temporarily unreachable. Please check your MongoDB Atlas connection and ensure your IP is whitelisted.";
        } else if (status == 404) {
            desc = "The page you are looking for does not exist.";
        } else if (status == 403) {
            desc = "You do not have permission to access this page.";
        } else {
            desc = (message != null && !message.toString().isBlank())
                    ? message.toString()
                    : "An unexpected error occurred. Please try again.";
        }

        model.addAttribute("status",   status);
        model.addAttribute("title",    title);
        model.addAttribute("desc",     desc);
        model.addAttribute("username", auth != null ? auth.getName() : null);
        return "error";
    }
}
