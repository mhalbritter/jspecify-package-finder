package jpf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author Moritz Halbritter
 */
public class JSpecifyPackageFinder {

    private static final Pattern PACKAGE_NAME = Pattern.compile("package ([\\w.]+);");

    private final Path root;

    public JSpecifyPackageFinder(Path root) {
        this.root = root;
    }

    private void run() throws IOException {
        System.out.printf("= Nullness report for `%s`%n", this.root);
        List<Path> packageInfoFiles = findPackageInfoFiles();
        List<PackageInfo> packageInfos = readPackageInfos(packageInfoFiles);
        printMarkedPackages(packageInfos);
        printUnmarkedPackages(packageInfos);
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: java -jar jpf.JSpecifyPackageFinder.jar <spring boot project path> [<module>]");
            System.exit(1);
            return;
        }
        Path root = Path.of(args[0]).toAbsolutePath();
        new JSpecifyPackageFinder(root).run();
    }

    private void printMarkedPackages(List<PackageInfo> packageInfos) {
        System.out.println("== Marked packages");
        for (PackageInfo packageInfo : packageInfos) {
            if (!packageInfo.nullMarked()) {
                continue;
            }
            System.out.println("* `" + packageInfo.name() + "`");
        }
    }

    private void printUnmarkedPackages(List<PackageInfo> packageInfos) {
        System.out.println("== Unmarked packages");
        for (PackageInfo packageInfo : packageInfos) {
            if (packageInfo.nullMarked()) {
                continue;
            }
            System.out.println("* `" + packageInfo.name() + "`");
        }
    }

    private List<PackageInfo> readPackageInfos(List<Path> packageInfoFiles) throws IOException {
        List<PackageInfo> result = new ArrayList<>(packageInfoFiles.size());
        for (Path packageInfoFile : packageInfoFiles) {
            String content = Files.readString(packageInfoFile);
            boolean nullMarked = content.contains("import org.jspecify.annotations.NullMarked;");
            Matcher matcher = PACKAGE_NAME.matcher(content);
            if (!matcher.find()) {
                throw new IllegalStateException("Unable to find package for file '%s'".formatted(packageInfoFile));
            }
            String name = matcher.group(1);
            result.add(new PackageInfo(name, nullMarked));
        }
        return result;
    }

    private List<Path> findPackageInfoFiles() throws IOException {
        try (Stream<Path> files = Files.find(this.root, Integer.MAX_VALUE,
                (file, attributes) -> isPackageInfoFile(file, attributes) && isMainSourceFile(file, attributes))) {
            return files.toList();
        }
    }

    private boolean isPackageInfoFile(Path path, BasicFileAttributes attributes) {
        return attributes.isRegularFile() && path.getFileName().toString().equals("package-info.java");
    }

    private boolean isMainSourceFile(Path path, BasicFileAttributes attributes) {
        return attributes.isRegularFile() && path.toString().contains("src/main/java/");
    }

    private record PackageInfo(String name, boolean nullMarked) {
    }

}
