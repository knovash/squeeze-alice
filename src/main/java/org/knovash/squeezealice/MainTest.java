package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.utils.Levenstein;
import org.knovash.squeezealice.utils.Utils;
import org.ktilis.yandexmusiclib.*;
import org.ktilis.yandexmusiclib.exeptions.NoTokenFoundException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Log4j2
public class MainTest {


    public static void main(String[] args) throws NoTokenFoundException, IOException, ExecutionException, InterruptedException {
        log.info("  ---+++===[ START ]===+++---");


        log.info("REBOOT SERVICE LMS");
        ProcessBuilder pb = new ProcessBuilder("ls");
        pb.inheritIO();
        pb.directory(new File("/home/konstantin/"));
        try {
            pb.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("REBOOT OK");


//        CompletableFuture<org.json.JSONObject> track = Playlist.getTrack("33497347","track","1","5");


    }

}