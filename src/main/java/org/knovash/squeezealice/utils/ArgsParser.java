package org.knovash.squeezealice.utils;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.cli.*;
import org.knovash.squeezealice.Main;

@Log4j2
public class ArgsParser {

    public static void parse(String[] arg) {
        log.info("READ CONFIG FROM ARGS");
//        CommandLine commandLine;
//        Options options = new Options();
//        CommandLineParser parser = new DefaultParser();
//        options.addOption("lmsip", "lmsip", true, "lmsip");
//        options.addOption("lmsport", "lmsport", true, "lmsport");
//        options.addOption("port", "port", true, "port");
//        try {
//            commandLine = parser.parse(options, arg);
//        } catch (ParseException e) {
//            throw new RuntimeException(e);
//        }
//        log.info("lmsip " + commandLine.getOptionValue("lmsip"));
//        log.info("lmsport " + commandLine.getOptionValue("lmsport"));
//        log.info("port " + commandLine.getOptionValue("port"));
//        if (commandLine.getOptionValue("lmsip") != null) Main.lmsIp = commandLine.getOptionValue("lmsip");
//        if (commandLine.getOptionValue("lmsport") != null) Main.lmsPort = commandLine.getOptionValue("lmsport");
//        if (commandLine.getOptionValue("port") != null) Main.port = Integer.parseInt(commandLine.getOptionValue("port"));
//        Main.lmsUrl = "http://" + Main.lmsIp + ":"+Main.lmsPort+"/jsonrpc.js";
//        log.info("LMS URL: " +  Main.lmsUrl);
//        log.info("THIS PORT: " +  Main.port);
    }
}