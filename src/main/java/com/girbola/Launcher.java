package com.girbola;

public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }

//    public static void main(String[] args) {
//        System.out.println("Starting MDIR");
//        String version = System.getProperty("java.version");
//        if (version.startsWith("1.")) {
//            version = version.substring(2);
//        }
//        double parsedVersion = Double.parseDouble(version.substring(0, version.indexOf(".", 2)));
//        if (parsedVersion < 24) {
//            System.err.println("This application requires Java 24 or higher. Current version: " + version);
//            System.exit(1);
//        }
//        Main.main(args);
//    }

}
