package info.kgeorgiy.ja.vysotin.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

public class Walk {
    public static void main(String[] args) {
        // Arguments checking
        if (args == null || args.length != 2) {
            System.out.println("Number of arguments must be 2");
            return;
        }
        if (args[0] == null) {
            System.out.println("First argument is null");
            return;
        }
        if (args[1] == null) {
            System.out.println("Second argument is null");
            return;
        }
        String inPath = args[0];
        String outPath = args[1];

        // Paths validation
        if (isInvalidPath(args[0])) {
            System.out.println("Input filepath \"" + args[0] + "\" is invalid");
            return;
        } else if (isInvalidPath(args[1])) {
            System.out.println("Output filepath \"" + args[1] + "\" is invalid");
            return;
        }

        // Creating directory or output file
        File directory = new File(outPath).getParentFile();
        if (directory == null) {
            System.out.println("Can't recognize directory of output file name \"" + outPath + "\"");
            return;
        }
        try {
            if (!directory.exists() && !directory.mkdirs()) {
                System.out.println("Can't create a directory \"" + directory.getName() + "\"");
                return;
            }
        } catch (SecurityException e) {
            System.out.println("Permission denied to create directory \"" + directory.getName() + "\"");
            return;
        }

        // Writing hashes and paths in the file
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(outPath), StandardCharsets.UTF_8))) {
            // Reading paths
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(inPath), StandardCharsets.UTF_8))) {
                String path = reader.readLine();
                while (path != null) {
                    writer.write(String.format("%08x %s" + System.lineSeparator(), hashJenkins(path), path));
                    path = reader.readLine();
                }
            } catch (SecurityException e) {
                System.out.println("Permission denied to read file \"" + inPath + "\"");
            } catch (FileNotFoundException e) {
                System.out.println("Can't find file \"" + inPath + "\"");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } catch (SecurityException e) {
            System.out.println("Permission denied to read file \"" + outPath + "\"");
        } catch (FileNotFoundException e) {
            System.out.println("Can't find file \"" + outPath + "\"");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    // Hash function
    public static int hashJenkins(String path) {
        int hash = 0x0;
        if (isInvalidPath(path)) {
            System.out.println("Invalid path \"" + path + "\"");
            return hash;
        }
        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(path), 256)) {
            int value;
            while ((value = stream.read()) != -1) {
                hash += value;
                hash += hash << 10;
                hash ^= hash >>> 6;
            }
            hash += hash << 3;
            hash ^= hash >>> 11;
            hash += hash << 15;
        } catch (SecurityException e) {
            System.out.println("Permission denied to read file \"" + path + "\"");
        } catch (FileNotFoundException e) {
            System.out.println("Can't find file \"" + path + "\"");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return hash;
    }

    // Paths Validator
    public static boolean isInvalidPath(String path) {
        try {
            Paths.get(path);
        } catch (InvalidPathException | NullPointerException ex) {
            return true;
        }
        return false;
    }
}