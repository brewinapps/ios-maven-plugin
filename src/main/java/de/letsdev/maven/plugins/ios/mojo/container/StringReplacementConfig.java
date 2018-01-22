package de.letsdev.maven.plugins.ios.mojo.container;

import java.util.List;

public class StringReplacementConfig {
    /**
     * defining all files and directories to replace
     *
     * @parameter property="ios.stringReplacementList"
     */
    public List<StringReplacement> stringReplacementList;

    /**
     * defines if the build should fail, if a string is not found
     * defaults to false
     *
     * @parameter property="ios.failWhenNotFound"
     */
    public boolean failWhenNotFound = false;
}
