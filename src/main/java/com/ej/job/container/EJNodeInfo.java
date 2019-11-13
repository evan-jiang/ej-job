package com.ej.job.container;

import java.util.UUID;


public class EJNodeInfo {
    public static final int intervalTotal = 1024;

    public static final String nodeName = UUID.randomUUID().toString().replace("-", "");

    public static volatile Boolean master = Boolean.FALSE;

}
