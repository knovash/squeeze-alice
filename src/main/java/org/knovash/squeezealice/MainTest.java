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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Log4j2
public class MainTest {


    public static void main(String[] args) throws NoTokenFoundException, IOException, ExecutionException, InterruptedException {
        log.info("  ---+++===[ START ]===+++---");

        Runtime.getRuntime().exec("mkdir fff");

        Runtime.getRuntime().exec("sh test.sh");

//        Process p = new ProcessBuilder("mkdir sss").start();

//        ProcessBuilder pb = new ProcessBuilder("sh test.sh", "myArg1", "myArg2");
//        Map<String, String> env = pb.environment();
//        env.put("VAR1", "myValue");
//        env.remove("OTHERVAR");
//        env.put("VAR2", env.get("VAR1") + "suffix");
//        pb.directory(new File("/home/konstantin/IdeaProjects/squeeze-alice/"));
//        Process p = pb.start();

//        CompletableFuture<org.json.JSONObject> track = Playlist.getTrack("33497347","track","1","5");


    }

}