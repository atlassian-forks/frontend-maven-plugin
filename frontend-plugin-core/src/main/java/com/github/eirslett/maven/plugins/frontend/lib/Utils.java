package com.github.eirslett.maven.plugins.frontend.lib;

import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.isNull;

public final class Utils {
    /**
     * Deletes a corrupted cached archive so the next build downloads it again. Logs a warning when that
     * fails, for example because the archive is in a read-only chained local repository.
     *
     * @return whether the archive is gone
     */
    static boolean deleteCorruptedArchive(File archive, Logger logger) {
        if (archive.delete() || !archive.exists()) {
            return true;
        }
        logger.warn("Could not delete the corrupted archive file {}. It may be in a read-only repository. "
                + "Delete it manually, then try the build again.", archive.getPath());
        return false;
    }

    public static List<String> merge(List<String> first, List<String> second) {
        ArrayList<String> result = new ArrayList<String>(first);
        result.addAll(second);
        return result;
    }

    public static List<String> prepend(String first, List<String> list){
        return merge(Arrays.asList(first), list);
    }

    public static String normalize(String path){
        return path.replace("/", File.separator);
    }

    public static String implode(String separator, List<String> elements){
        StringBuffer s = new StringBuffer();
        for(int i = 0; i < elements.size(); i++){
            if(i > 0){
                s.append(" ");
            }
            s.append(elements.get(i));
        }
        return s.toString();
    }

    public static boolean isRelative(String path) {
        return !path.startsWith("/") && !path.startsWith("file:") && !path.matches("^[a-zA-Z]:\\\\.*");
    }

    public static boolean isBlank(@Nullable String string) {
        return isNull(string) || string.trim().isEmpty();
    }
}
