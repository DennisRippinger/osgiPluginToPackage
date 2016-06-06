package de.msg.osgiPluginToPackage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Converter {

	private static final String PLUGIN = "<YOURPLUGIN>";
	public static final String PATH = "<YOURPATH>";

	public SearchManifestFiles searchManifestFiles = new SearchManifestFiles();

	@Test
	public void movePluginsToAdditionalBundles() throws IOException {

		List<Path> manifestFiles = searchManifestFiles.searchForFiles(Paths.get(PATH));

		for (Path manifestFile : manifestFiles) {
			Manifest manifest = new Manifest(new FileInputStream(manifestFile.toFile()));

			List<String> requiredPlugins = filterRequiredPlugins(manifest);

			Path buildPropertiesPath = createBuildPropertiesPath(manifestFile);
			if (!buildPropertiesPath.toFile().exists() || requiredPlugins.isEmpty())
				continue;

			StringBuilder buildProperties = new StringBuilder(new String(Files.readAllBytes(buildPropertiesPath)));
			buildProperties.append(String.format("\nadditional.bundles = %s", requiredPlugins.get(0)));
			requiredPlugins.remove(0);

			if (!requiredPlugins.isEmpty()) {
				buildProperties.append(", \\\n");
			}

			requiredPlugins.forEach(x -> buildProperties.append("               " + x + ",\\\n"));

			buildProperties.replace(buildProperties.lastIndexOf("\\"), buildProperties.lastIndexOf("\\") + 1, "");

			Files.write(Paths.get(buildPropertiesPath + "2"), buildProperties.toString().getBytes());

		}

	}

	/**
	 * Basically ../build.properties
	 *
	 * @param manifestFile location of the manifest file.
	 * @return path to build.properties.
	 */
	private Path createBuildPropertiesPath(Path manifestFile) {
		return Paths.get(manifestFile.getParent().getParent() + File.separator + "build.properties");
	}

	private List<String> filterRequiredPlugins(Manifest manifest) {

		String exportPackage = manifest.getMainAttributes().getValue("Require-Bundle");

		if (exportPackage == null)
			return Collections.emptyList();

		String[] packages = exportPackage.split(",");

		return Arrays.asList(packages)//
				.stream()//
				.filter(x -> x.contains(PLUGIN))//
				.collect(Collectors.toList());
	}
}
